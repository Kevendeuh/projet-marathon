package com.example.pllrunwatch.presentation.health

// wear/src/main/java/com/yourapp/health/DataLayerSender.kt

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DataLayerSender(private val context: Context) {
    private val dataClient = Wearable.getDataClient(context)
    private var lastSendTs = 0L
    private val minIntervalMs = 15_000L   // throttle: at most once every 15s

    suspend fun sendSmallDelta(lastHr: Int?, stepsToday: Long?) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (now - lastSendTs < minIntervalMs) return@withContext
        lastSendTs = now

        val req = PutDataMapRequest.create("/health/delta")
        val map = req.dataMap
        lastHr?.let { map.putInt("last_hr", it) }
        stepsToday?.let { map.putLong("steps_today", it) }
        map.putLong("ts", now)

        val putReq = req.asPutDataRequest()
        // Use Tasks.await to make this sync for reliability, but keep the payload small.
        Tasks.await(dataClient.putDataItem(putReq), 5, TimeUnit.SECONDS)
    }
}
