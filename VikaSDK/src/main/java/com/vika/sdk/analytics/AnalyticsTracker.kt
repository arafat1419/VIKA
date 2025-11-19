package com.vika.sdk.analytics

import android.content.Context
import android.util.Log
import com.vika.sdk.callbacks.NavigationError
import com.vika.sdk.models.NavigationEvent
import com.vika.sdk.models.NavigationResult
import com.vika.sdk.models.SDKConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Tracks analytics for navigation events
 */
internal class AnalyticsTracker(
    private val context: Context,
    private val config: SDKConfig
) {
    companion object {
        private const val TAG = "AnalyticsTracker"
        private const val BATCH_SIZE = 50
        private const val FLUSH_INTERVAL_MS = 30000L
    }

    private val eventQueue = ConcurrentLinkedQueue<NavigationEvent>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var flushJob: Job? = null

    init {
        startPeriodicFlush()
    }

    fun trackQueryAttempt(query: String) {
        if (!config.analyticsEnabled) return

        val event = NavigationEvent(
            userQuery = query,
            screenId = null,
            success = false
        )
        addEvent(event)
    }

    fun trackNavigationSuccess(query: String, result: NavigationResult) {
        if (!config.analyticsEnabled) return

        val event = NavigationEvent(
            userQuery = query,
            screenId = result.screenId,
            success = true,
            confidence = result.confidence
        )
        addEvent(event)
    }

    fun trackNavigationError(query: String, error: NavigationError) {
        if (!config.analyticsEnabled) return

        val event = NavigationEvent(
            userQuery = query,
            screenId = null,
            success = false,
            errorType = error.javaClass.simpleName
        )
        addEvent(event)
    }

    fun getData(): AnalyticsData {
        val events = eventQueue.toList()
        val successCount = events.count { it.success }
        val errorCount = events.count { !it.success }
        val avgConfidence = events
            .mapNotNull { it.confidence }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat() ?: 0f

        return AnalyticsData(
            totalEvents = events.size,
            successCount = successCount,
            errorCount = errorCount,
            successRate = if (events.isNotEmpty())
                successCount.toFloat() / events.size else 0f,
            averageConfidence = avgConfidence,
            topScreens = events
                .groupBy { it.screenId }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
        )
    }

    fun flush() {
        if (eventQueue.isEmpty()) return

        val events = mutableListOf<NavigationEvent>()
        while (eventQueue.isNotEmpty() && events.size < BATCH_SIZE) {
            eventQueue.poll()?.let { events.add(it) }
        }

        if (events.isNotEmpty()) {
            sendEvents(events)
        }
    }

    private fun addEvent(event: NavigationEvent) {
        eventQueue.offer(event)

        if (eventQueue.size >= BATCH_SIZE) {
            scope.launch {
                flush()
            }
        }

        Log.d(TAG, "Event tracked: ${event.eventId}")
    }

    private fun sendEvents(events: List<NavigationEvent>) {
        // Send to analytics server
        Log.d(TAG, "Sending ${events.size} events to analytics")
        // Implementation would send to actual analytics endpoint
    }

    private fun startPeriodicFlush() {
        flushJob = scope.launch {
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                flush()
            }
        }
    }

    fun cleanup() {
        flush()
        flushJob?.cancel()
        scope.cancel()
    }
}

/**
 * Analytics data summary
 */
internal data class AnalyticsData(
    val totalEvents: Int,
    val successCount: Int,
    val errorCount: Int,
    val successRate: Float,
    val averageConfidence: Float,
    val topScreens: List<Pair<String?, Int>>
)