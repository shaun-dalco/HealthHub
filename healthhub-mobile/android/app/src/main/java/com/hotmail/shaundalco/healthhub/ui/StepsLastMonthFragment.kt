package com.hotmail.shaundalco.healthhub.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hotmail.shaundalco.healthhub.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import kotlinx.coroutines.launch

class StepsLastMonthFragment : Fragment(R.layout.fragment_steps_last_month) {

    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var permissionLauncher:
            androidx.activity.result.ActivityResultLauncher<Set<String>>

    private val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
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

                // If SDK is available but user never saw UI (common “silent return” symptom),
                // open Health Connect’s screen so you can grant permissions manually.
                if (!hasAll && HealthConnectClient.getSdkStatus(requireContext()) == HealthConnectClient.SDK_AVAILABLE) {
                    out?.append("\nNo permission UI? Opening Health Connect screen…")
                    openHealthConnectUI()
                }
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGrant = view.findViewById<Button>(R.id.btnGrantHealthConnect)
        val btnLoad = view.findViewById<Button>(R.id.btnLoadSteps)

        btnGrant.setOnClickListener {
            view.findViewById<TextView>(R.id.output)?.text =
                "Grant clicked. Launching permission UI… (sdk=${HealthConnectClient.getSdkStatus(requireContext())})"

            try {
                permissionLauncher.launch(requiredPermissions)
            } catch (e: Exception) {
                // If the permission UI can't be launched for any reason, open HC UI directly
                view.findViewById<TextView>(R.id.output)?.text =
                    "Permission UI failed to launch: ${e::class.java.simpleName}: ${e.message}\nOpening Health Connect…"
                openHealthConnectUI()
            }
        }


        val status = HealthConnectClient.getSdkStatus(requireContext())
        view.findViewById<TextView>(R.id.output)
            .text = "Health Connect SDK status = $status"



        btnLoad.setOnClickListener {
            ensurePermissionsThenRead()
        }

        // Initial state
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            updateButtons(granted.containsAll(requiredPermissions))
        }
    }

    private fun updateButtons(hasAllPermissions: Boolean) {
        view?.findViewById<Button>(R.id.btnGrantHealthConnect)?.isEnabled = !hasAllPermissions
        view?.findViewById<Button>(R.id.btnLoadSteps)?.isEnabled = hasAllPermissions
    }

    private fun ensurePermissionsThenRead() {
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(requiredPermissions)) {
                readStepsLast30Days()
            } else {
                updateButtons(false)
                view?.findViewById<TextView>(R.id.output)?.text =
                    "Missing permission. Tap “Grant Health Connect permission”."
            }
        }
    }

    private fun readStepsLast30Days() {
        lifecycleScope.launch {
            val output = view?.findViewById<TextView>(R.id.output)

            try {
                // Last 30 days (inclusive-ish): from start-of-day 29 days ago to start of tomorrow
                val endDateExclusive = LocalDate.now(zoneId).plusDays(1)
                val startDateInclusive = endDateExclusive.minusDays(30)

                val startTime: LocalDateTime = startDateInclusive.atStartOfDay()
                val endTime: LocalDateTime = endDateExclusive.atStartOfDay()

                val results = healthConnectClient.aggregateGroupByPeriod(
                    AggregateGroupByPeriodRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        timeRangeSlicer = Period.ofDays(1)
                    )
                )

                val byDay = linkedMapOf<LocalDate, Long>()
                for (bucket in results) {
                    val day = bucket.startTime.toLocalDate()
                    val totalSteps = bucket.result[StepsRecord.COUNT_TOTAL] ?: 0L
                    byDay[day] = totalSteps
                }

                val sb = StringBuilder()
                var d = startDateInclusive
                while (d.isBefore(endDateExclusive)) {
                    sb.append(d).append(" : ").append(byDay[d] ?: 0L).append('\n')
                    d = d.plusDays(1)
                }

                output?.text = sb.toString()

            } catch (e: Exception) {
                output?.text = "Error reading steps: ${e.message}"
            }
        }
    }

    private fun ensureHealthConnectAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(requireContext())
        val output = view?.findViewById<TextView>(R.id.output)

        when (status) {
            HealthConnectClient.SDK_AVAILABLE -> return true

            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                output?.text = "Health Connect needs to be installed/updated. Opening Play Store…"

                val providerPackageName = "com.google.android.apps.healthdata"
                val uriString =
                    "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(uriString)
                    putExtra("overlay", true)
                    putExtra("callerId", requireContext().packageName)
                }

                // If Play Store isn't available, fall back to https link
                runCatching { startActivity(intent) }.getOrElse {
                    val https = "https://play.google.com/store/apps/details?id=$providerPackageName"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(https)))
                }

                return false
            }

            HealthConnectClient.SDK_UNAVAILABLE -> {
                output?.text = "Health Connect isn’t available on this device."
                return false
            }

            else -> {
                output?.text = "Health Connect status: $status"
                return false
            }
        }
    }

    private fun openHealthConnectUI() {
        val intent = HealthConnectClient.getHealthConnectManageDataIntent(requireContext())
        startActivity(intent)
    }


}
