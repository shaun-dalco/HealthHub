package com.hotmail.shaundalco.healthhub.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

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
                val output2 = view?.findViewById<TextView>(R.id.output2)
                if (output2 != null) {
                    output2.text = uploadLast30DaysSteps(healthConnectClient, "http://192.168.1.11:3001")
                }
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

    /**
     * Reads daily steps totals for the last 30 days and uploads them to:
     * POST {baseUrl}/api/steps/addbulk
     *
     * Returns server response body as string (or throws on error).
     */
    suspend fun uploadLast30DaysSteps(
        healthConnectClient: HealthConnectClient,
        baseUrl: String,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = withContext(Dispatchers.IO) {
        try {
            // Basic sanity
            val trimmedBase = baseUrl.trim()
            if (trimmedBase.isEmpty()) return@withContext "Error: baseUrl is empty"

            // Time range: start-of-day 30 days ago -> start of tomorrow (exclusive)
            val endDateExclusive = LocalDate.now(zoneId).plusDays(1)
            val startDateInclusive = endDateExclusive.minusDays(30)

            val startTime: LocalDateTime = startDateInclusive.atStartOfDay()
            val endTime: LocalDateTime = endDateExclusive.atStartOfDay()

            val buckets = healthConnectClient.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Period.ofDays(1)
                )
            )

            // Bucket results -> map(day -> steps)
            val byDay = HashMap<LocalDate, Long>()
            for (b in buckets) {
                val day = b.startTime.toLocalDate()
                val steps = b.result[StepsRecord.COUNT_TOTAL] ?: 0L
                byDay[day] = steps
            }

            // Build a full 30-day list (fill missing days with 0)
            val itemsArray = JSONArray()
            var d = startDateInclusive
            while (d.isBefore(endDateExclusive)) {
                val steps = byDay[d] ?: 0L
                itemsArray.put(
                    JSONObject()
                        .put("day", d.toString())     // YYYY-MM-DD
                        .put("steps", steps)          // keep as Long to avoid overflow edge cases
                )
                d = d.plusDays(1)
            }

            val payload = JSONObject().put("items", itemsArray).toString()

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val url = trimmedBase.trimEnd('/') + "/api/steps/addbulk"

            val req = Request.Builder()
                .url(url)
                .post(payload.toRequestBody(mediaType))
                .build()

            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty()

                if (!resp.isSuccessful) {
                    // Don't crash; return useful info
                    return@withContext buildString {
                        append("Upload failed\n")
                        append("URL: ").append(url).append('\n')
                        append("HTTP ").append(resp.code).append('\n')
                        if (body.isNotBlank()) {
                            append("Body:\n").append(body.take(2000)) // avoid dumping megabytes
                        }
                    }
                }

                // Success
                return@withContext buildString {
                    append("Upload OK\n")
                    append("URL: ").append(url).append('\n')
                    append("Days: 30\n")
                    append("Server response:\n")
                    append(body.ifBlank { "(empty response)" }.take(2000))
                }
            }
        } catch (e: IOException) {
            Log.e("StepsUpload", "Network error", e)
            "Network error: ${e.message ?: e.javaClass.simpleName}"
        } catch (e: Exception) {
            Log.e("StepsUpload", "Unexpected error", e)
            "Unexpected error: ${e.message ?: e.javaClass.simpleName}"
        }
    }


    private fun openHealthConnectUI() {
        val intent = HealthConnectClient.getHealthConnectManageDataIntent(requireContext())
        startActivity(intent)
    }


}
