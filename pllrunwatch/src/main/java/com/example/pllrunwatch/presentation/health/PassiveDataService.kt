package com.example.pllrunwatch.presentation.health

import android.os.SystemClock
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.health.connect.client.HealthConnectClient
import java.time.Instant

// Ce service est réveillé par le système quand de nouvelles données sont disponibles
class PassiveDataService : PassiveListenerService() {

    // On crée un scope pour lancer des coroutines dans ce service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var hcWriter: HealthConnectWriter

    override fun onCreate() {
        super.onCreate()
        // Initialisation du writer.
        // Note: Idéalement, utilisez l'injection de dépendances (Hilt) ici,
        // mais pour l'instant, on l'initialise manuellement.
        val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
        hcWriter = HealthConnectWriter(healthConnectClient)
    }

    // Cette méthode est appelée quand de nouvelles données arrivent (ex: FC, Pas)
    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        serviceScope.launch {
            val samples = mutableListOf<MetricSample>()
            val bootInstant = Instant.now().minusNanos(SystemClock.elapsedRealtimeNanos())

            // Récupération des points de Fréquence Cardiaque
            dataPoints.getData(DataType.HEART_RATE_BPM).forEach { point ->
                val pointTime = point.getTimeInstant(bootInstant)
                samples.add(
                    MetricSample(
                        MetricType.HEART_RATE,
                        point.value,
                        pointTime,
                        pointTime
                    )
                )
            }

            // Récupération des pas
            dataPoints.getData(DataType.STEPS_DAILY).forEach { point ->
                // STEPS_DAILY renvoie le total depuis minuit.
                // Si vous voulez juste l'incrément, la logique est plus complexe,
                // mais HealthConnect gère souvent bien les cumuls.

                samples.add(
                    MetricSample(
                        MetricType.STEP_COUNT,
                        point.value.toDouble(),
                        point.getStartInstant( bootInstant),
                        point.getEndInstant( bootInstant)

                    )
                )
            }

            // On écrit tout dans Health Connect via votre writer existant
            if (samples.isNotEmpty()) {
                hcWriter.writeBatch(samples)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

data class MetricSample(
    val type: MetricType,
    val value: Double,
    val startTime: Instant,
    val endTime: Instant
)

enum class MetricType {
    HEART_RATE,
    STEP_COUNT,
    DISTANCE,
    CALORIES,
    SLEEP
}
