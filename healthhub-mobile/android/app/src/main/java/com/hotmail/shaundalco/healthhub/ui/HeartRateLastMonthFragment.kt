package com.hotmail.shaundalco.healthhub.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import com.hotmail.shaundalco.healthhub.AppUtils
import com.hotmail.shaundalco.healthhub.R
import kotlinx.coroutines.launch
import java.time.ZoneId

class HeartRateLastMonthFragment : Fragment(R.layout.fragment_heart_rate_last_month) {

    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var permissionLauncher:
            androidx.activity.result.ActivityResultLauncher<Set<String>>

    // Heart rate permission (NOT steps)
    private val requiredPermissions = setOf(
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

        val btnGrant = view.findViewById<Button>(R.id.btnGrantHealthConnect)
        val btnLoad = view.findViewById<Button>(R.id.btnLoadSteps) // id can stay; it’s just a button

        btnGrant.setOnClickListener {
            view.findViewById<TextView>(R.id.output)?.text =
                "Grant clicked. Launching permission UI… (sdk=${HealthConnectClient.getSdkStatus(requireContext())})"

            try {
                permissionLauncher.launch(requiredPermissions)
            } catch (e: Exception) {
                view.findViewById<TextView>(R.id.output)?.text =
                    "Permission UI failed: ${e::class.java.simpleName}: ${e.message}\nOpening Health Connect…"
                openHealthConnectUI()
            }
        }

        val status = HealthConnectClient.getSdkStatus(requireContext())
        view.findViewById<TextView>(R.id.output).text = "Health Connect SDK status = $status"

        btnLoad.setOnClickListener {
            ensurePermissionsThenReadAndUpload()
        }

        // Initial state
        lifecycleScope.launch {
            val hasAll = AppUtils.hasAllPermissions(healthConnectClient, requiredPermissions)
            updateButtons(hasAll)
        }
    }

    private fun updateButtons(hasAllPermissions: Boolean) {
        view?.findViewById<Button>(R.id.btnGrantHealthConnect)?.isEnabled = !hasAllPermissions
        view?.findViewById<Button>(R.id.btnLoadSteps)?.isEnabled = hasAllPermissions
    }

    private fun ensurePermissionsThenReadAndUpload() {
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
            val output2 = view?.findViewById<TextView>(R.id.output2)
            output2?.text = AppUtils.uploadLastNDaysDailyLongMetric(
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

                output?.text = sb.toString()
            } catch (e: Exception) {
                output?.text = "Error reading heart rate: ${e.message}"
            }
        }
    }

    private fun openHealthConnectUI() {
        val intent = HealthConnectClient.getHealthConnectManageDataIntent(requireContext())
        startActivity(intent)
    }
}
