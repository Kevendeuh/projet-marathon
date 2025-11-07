package com.example.pllrunwatch.presentation.health

// wear/src/main/java/com/yourapp/health/PassiveMonitorCoordinator.kt

import kotlinx.coroutines.*
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Very small in-memory buffer that batches samples and flushes them periodically.
 * Keep the buffer size limited to avoid memory pressure on watch.
 */
class PassiveMonitorCoordinator(
    private val writer: HealthConnectWriter,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val buffer = ConcurrentLinkedQueue<MetricSample>()
    private var flushJob: Job? = null

    // Configurable low-power parameters (see strategy below)
    private val flushIntervalMs = 60_000L      // flush every 60s (configurable)
    private val maxBatchSize = 200             // flush early if too many samples

    fun start() {
        if (flushJob?.isActive == true) return
        flushJob = scope.launch {
            while (isActive) {
                delay(flushIntervalMs)
                flush()
            }
        }
    }

    fun stop() {
        flushJob?.cancel()
        scope.launch { flush() } // flush remaining
    }

    // Called by the passive callback with normalized sample
    fun onNewSample(sample: MetricSample) {
        buffer.add(sample)
        if (buffer.size >= maxBatchSize) {
            scope.launch { flush() }
        }
    }

    private suspend fun flush() {
        val batch = mutableListOf<MetricSample>()
        while (true) {
            val s = buffer.poll() ?: break
            batch.add(s)
            if (batch.size >= maxBatchSize) break
        }
        if (batch.isEmpty()) return

        // Convert batch into HealthConnect records and write in a single insertRecords call
        writer.writeBatch(batch)
    }
}

/** Minimal sample model used by the coordinator */
data class MetricSample(
    val type: MetricType,
    val value: Double,
    val startTime: Instant,
    val endTime: Instant
)

enum class MetricType { HEART_RATE, STEP_COUNT, DISTANCE, CALORIES, SLEEP }
