package com.hotmail.shaundalco.healthhub.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import com.hotmail.shaundalco.healthhub.AppUtils
import com.hotmail.shaundalco.healthhub.R
import kotlinx.coroutines.launch
import java.time.ZoneId

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var permissionLauncher:
            androidx.activity.result.ActivityResultLauncher<Set<String>>

    private val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    private val zoneId: ZoneId get() = ZoneId.systemDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())

        permissionLauncher =
            registerForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { granted ->
                val hasAll = granted.containsAll(requiredPermissions)
                updateButtons(hasAll)

                val out = view?.findViewById<TextView>(R.id.output)
                out?.text = "Permission result: granted=$granted"

                if (!hasAll &&
                    HealthConnectClient.getSdkStatus(requireContext()) == HealthConnectClient.SDK_AVAILABLE
                ) {
                    out?.append("\nNo permission UI? Opening Health Connect screen…")
                    openHealthConnectUI()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSync = view.findViewById<Button>(R.id.btnSync)

        val checkSteps = view.findViewById<AppCompatCheckBox>(R.id.checksteps)
        val checkHeart = view.findViewById<AppCompatCheckBox>(R.id.checkheart)

        val status = HealthConnectClient.getSdkStatus(requireContext())
        view.findViewById<TextView>(R.id.output).text = "Health Connect SDK status = $status"

        btnSync.setOnClickListener {
            if(checkSteps.isChecked) ensurePermissionsThenReadAndUploadSteps()
            if(checkHeart.isChecked) ensurePermissionsThenReadAndUploadHR()
        }

        // Initial button state
        lifecycleScope.launch {
            val hasAll = AppUtils.hasAllPermissions(healthConnectClient, requiredPermissions)
            updateButtons(hasAll)
        }
    }

    private fun updateButtons(hasAllPermissions: Boolean) {
        view?.findViewById<Button>(R.id.btnGrantHealthConnect)?.isEnabled = !hasAllPermissions
        view?.findViewById<Button>(R.id.btnLoadSteps)?.isEnabled = hasAllPermissions
    }

    private fun ensurePermissionsThenReadAndUploadSteps() {
        lifecycleScope.launch {
            val hasAll = AppUtils.hasAllPermissions(healthConnectClient, requiredPermissions)
            if (!hasAll) {
                updateButtons(false)
                view?.findViewById<TextView>(R.id.output)?.text =
                    "Missing permission. Tap “Grant Health Connect permission”."
                return@launch
            }

            // Debug read (prints daily totals)
            readStepsLast30Days()

            // Upload using shared AppUtils helper
            view?.findViewById<TextView>(R.id.output)?.text =
                AppUtils.uploadLastNDaysDailyLongMetric(
                    healthConnectClient = healthConnectClient,
                    baseUrl = "http://192.168.1.11:3001",
                    endpointPath = "/api/steps/addbulk",
                    metric = StepsRecord.COUNT_TOTAL,
                    valueKey = "steps",
                    days = 30,
                    zoneId = zoneId,
                    logTag = "StepsUpload",
                )
        }
    }

    private fun ensurePermissionsThenReadAndUploadHR() {
        lifecycleScope.launch {
            val hasAll = AppUtils.hasAllPermissions(healthConnectClient, requiredPermissions)
            if (!hasAll) {
                updateButtons(false)
                view?.findViewById<TextView>(R.id.output)?.text =
                    "Missing permission. Tap “Grant Health Connect permission”."
                return@launch
            }

            // Debug read: show last 30 days avg BPM
            readHeartRateAvgLast30Days()

            // Upload: reuse generic helper in AppUtils
            view?.findViewById<TextView>(R.id.output)?.text = AppUtils.uploadLastNDaysDailyLongMetric(
                healthConnectClient = healthConnectClient,
                baseUrl = "http://192.168.1.11:3001",
                endpointPath = "/api/heartrate/addbulk",
                metric = HeartRateRecord.BPM_AVG,
                valueKey = "hr",
                days = 30,
                zoneId = zoneId,
                logTag = "HeartRateUpload",
            )
        }
    }

    /*
      Debug only: read daily average BPM for the last 30 days and print it.
     */
    private fun readHeartRateAvgLast30Days() {
        lifecycleScope.launch {
            val output = view?.findViewById<TextView>(R.id.output)

            try {
                val range = AppUtils.lastNDaysRange(days = 30, zoneId = zoneId)
                val byDay = AppUtils.aggregateDailyLongMetric(
                    healthConnectClient = healthConnectClient,
                    metric = HeartRateRecord.BPM_AVG,
                    range = range
                )

                val sb = StringBuilder()
                var d = range.startDateInclusive
                while (d.isBefore(range.endDateExclusive)) {
                    val bpmAvg = byDay[d] ?: 0L
                    sb.append(d).append(" : ").append(bpmAvg).append(" bpm (avg)").append('\n')
                    d = d.plusDays(1)
                }

                //output?.text = sb.toString()
            } catch (e: Exception) {
                output?.text = "Error reading heart rate: ${e.message}"
            }
        }
    }

    /*
      Debug only: read daily steps totals for the last 30 days and print them.
     */
    private fun readStepsLast30Days() {
        lifecycleScope.launch {
            val output = view?.findViewById<TextView>(R.id.output)

            try {
                val range = AppUtils.lastNDaysRange(days = 30, zoneId = zoneId)

                val byDay = AppUtils.aggregateDailyLongMetric(
                    healthConnectClient = healthConnectClient,
                    metric = StepsRecord.COUNT_TOTAL,
                    range = range
                )

                val sb = StringBuilder()
                var d = range.startDateInclusive
                while (d.isBefore(range.endDateExclusive)) {
                    sb.append(d).append(" : ").append(byDay[d] ?: 0L).append('\n')
                    d = d.plusDays(1)
                }

                //output?.text = sb.toString()
            } catch (e: Exception) {
                output?.text = "Error reading steps: ${e.message}"
            }
        }
    }

    private fun openHealthConnectUI() {
        val intent = HealthConnectClient.getHealthConnectManageDataIntent(requireContext())
        startActivity(intent)
    }
}