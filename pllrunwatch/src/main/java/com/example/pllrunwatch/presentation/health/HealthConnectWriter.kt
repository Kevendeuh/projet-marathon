package com.example.pllrunwatch.presentation.health

// wear/src/main/java/.../health/HealthConnectWriter.kt

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.meters
import com.google.android.gms.nearby.messages.Distance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device

class HealthConnectWriter(private val hcClient: HealthConnectClient) {

    suspend fun writeBatch(batch: List<MetricSample>) {
        // 1. Crée une liste pour contenir les enregistrements Health Connect
        val records = mutableListOf<androidx.health.connect.client.records.Record>()


        // 2. Itère sur chaque échantillon dans le lot
        for (sample in batch) {
            // 3. Transforme chaque MetricSample en un Record Health Connect
            val record = when (sample.type) {
                MetricType.HEART_RATE -> HeartRateRecord(
                    startTime = sample.startTime,
                    startZoneOffset = ZoneOffset.UTC, // ou le bon fuseau horaire si vous le suivez
                    endTime = sample.endTime,
                    endZoneOffset = ZoneOffset.UTC,
                    samples = listOf(
                        HeartRateRecord.Sample(
                            time = sample.startTime, // Pour un seul point, l'heure est la même
                            beatsPerMinute = sample.value.toLong()
                        )
                    ),
                    metadata = Metadata.autoRecorded(
                        device = Device(type = Device.TYPE_WATCH)
                    )
                )
                MetricType.STEP_COUNT -> StepsRecord(
                    startTime = sample.startTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = sample.endTime,
                    endZoneOffset = ZoneOffset.UTC,
                    count = sample.value.toLong(),
                    metadata = Metadata.autoRecorded(
                        device = Device(type = Device.TYPE_WATCH)
                    )
                )
                MetricType.CALORIES -> TotalCaloriesBurnedRecord(
                    startTime = sample.startTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endTime = sample.endTime,
                    endZoneOffset = ZoneOffset.UTC,
                    energy = Energy.kilocalories(sample.value),
                    metadata = Metadata.autoRecorded(
                        device = Device(type = Device.TYPE_WATCH)
                    )
                )
                // Le type 'SLEEP' est plus complexe et se gère souvent séparément
                MetricType.SLEEP -> null
                else -> {}
            }

        }

        // 5. Si la liste de records n'est pas vide, insère tout en une seule fois
        if (records.isNotEmpty()) {
            try {
                hcClient.insertRecords(records)
                Log.d("HealthConnectWriter", "${records.size} records inserted successfully.")
            } catch (e: Exception) {
                Log.e("HealthConnectWriter", "Error inserting records: ", e)
            }
        }
    }

    // Add similar helpers for DistanceRecord, TotalCaloriesBurned, SleepRecord, etc.
}
