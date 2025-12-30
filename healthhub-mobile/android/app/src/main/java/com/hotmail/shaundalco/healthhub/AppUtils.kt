package com.hotmail.shaundalco.healthhub

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId

object AppUtils {

    data class DayRange(
        val startDateInclusive: LocalDate,
        val endDateExclusive: LocalDate,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
    )

    private val httpClient by lazy { OkHttpClient() }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun lastNDaysRange(days: Long, zoneId: ZoneId = ZoneId.systemDefault()): DayRange {
        // End is start of tomorrow (exclusive), start is N days back
        val endDateExclusive = LocalDate.now(zoneId).plusDays(1)
        val startDateInclusive = endDateExclusive.minusDays(days)

        return DayRange(
            startDateInclusive = startDateInclusive,
            endDateExclusive = endDateExclusive,
            startTime = startDateInclusive.atStartOfDay(),
            endTime = endDateExclusive.atStartOfDay(),
        )
    }

    fun normalizeBaseUrl(baseUrl: String): String? {
        val trimmed = baseUrl.trim()
        if (trimmed.isEmpty()) return null
        return trimmed.trimEnd('/')
    }

    suspend fun hasAllPermissions(
        healthConnectClient: HealthConnectClient,
        requiredPermissions: Set<String>,
    ): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    /**
     * Generic: aggregates a single Long metric into daily buckets for the given range.
     * Missing days are omitted from the returned map (you can fill later).
     */
    suspend fun aggregateDailyLongMetric(
        healthConnectClient: HealthConnectClient,
        metric: AggregateMetric<Long>,
        range: DayRange,
    ): Map<LocalDate, Long> = withContext(Dispatchers.IO) {

        val results = healthConnectClient.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(metric),
                timeRangeFilter = TimeRangeFilter.between(range.startTime, range.endTime),
                timeRangeSlicer = Period.ofDays(1)
            )
        )

        val byDay = HashMap<LocalDate, Long>()
        for (bucket in results) {
            val day = bucket.startTime.toLocalDate()
            val value = bucket.result[metric] ?: 0L
            byDay[day] = value
        }
        byDay
    }

    /**
     * Builds: {"items":[{"day":"YYYY-MM-DD", "<valueKey>":123}, ...]}
     * Fills missing days with 0.
     */
    fun buildDailyItemsPayload(
        range: DayRange,
        byDay: Map<LocalDate, Long>,
        valueKey: String,
    ): String {
        val items = JSONArray()
        var d = range.startDateInclusive
        while (d.isBefore(range.endDateExclusive)) {
            val value = byDay[d] ?: 0L
            items.put(
                JSONObject()
                    .put("day", d.toString())
                    .put(valueKey, value)
            )
            d = d.plusDays(1)
        }
        return JSONObject().put("items", items).toString()
    }

    /**
     * Generic POST JSON with safe, non-crashy response text.
     */
    suspend fun postJson(url: String, payloadJson: String, tag: String = "HTTP"): String =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .post(payloadJson.toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        return@withContext buildString {
                            append("Upload failed\n")
                            append("URL: ").append(url).append('\n')
                            append("HTTP ").append(resp.code).append('\n')
                            if (body.isNotBlank()) append("Body:\n").append(body.take(2000))
                        }
                    }

                    return@withContext buildString {
                        append("Upload OK\n")
                        append("URL: ").append(url).append('\n')
                        append("Server response:\n")
                        append(body.ifBlank { "(empty response)" }.take(2000))
                    }
                }
            } catch (e: IOException) {
                Log.e(tag, "Network error", e)
                "Network error: ${e.message ?: e.javaClass.simpleName}"
            } catch (e: Exception) {
                Log.e(tag, "Unexpected error", e)
                "Unexpected error: ${e.message ?: e.javaClass.simpleName}"
            }
        }

    /**
     * One-liner helper: aggregate a daily Long metric for last N days and upload as bulk.
     */
    suspend fun uploadLastNDaysDailyLongMetric(
        healthConnectClient: HealthConnectClient,
        baseUrl: String,
        endpointPath: String,
        metric: AggregateMetric<Long>,
        valueKey: String,
        days: Long = 30,
        zoneId: ZoneId = ZoneId.systemDefault(),
        logTag: String = "MetricUpload",
    ): String {
        val normalized = normalizeBaseUrl(baseUrl) ?: return "Error: baseUrl is empty"

        val range = lastNDaysRange(days = days, zoneId = zoneId)
        val byDay = aggregateDailyLongMetric(healthConnectClient, metric, range)
        val payload = buildDailyItemsPayload(range, byDay, valueKey)

        val url = normalized + (if (endpointPath.startsWith("/")) endpointPath else "/$endpointPath")
        return postJson(url, payload, tag = logTag)
    }
}
