package com.example.pllrun.util

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import android.util.Log
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.key.type

class DataLayerListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/health/delta") {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val heartRate = dataMapItem.dataMap.getInt("last_hr")
                    val timestamp = dataMapItem.dataMap.getLong("ts")

                    Log.d("PhoneReceiver", "Reçu FC: $heartRate bpm à $timestamp")

                    // TODO: Diffuser cette info à votre UI (via LocalBroadcastManager, SharedFlow, ou EventBus)
                }
            }
        }
    }
}
