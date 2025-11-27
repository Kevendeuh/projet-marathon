package com.example.pllrunwatch.presentation.health

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.health.connect.client.HealthConnectClient
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import java.time.Instant

// Ce service est réveillé par le système quand de nouvelles données sont disponibles
class PassiveDataService : PassiveListenerService() {

    // On crée un scope pour lancer des coroutines dans ce service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var hcWriter: HealthConnectWriter

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onCreate() {
        super.onCreate()
        // Initialisation du writer.
        // Note: Idéalement, utilisez l'injection de dépendances (Hilt) ici,
        // mais pour l'instant, on l'initialise manuellement.
        try {
            val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
            hcWriter = HealthConnectWriter(healthConnectClient)
        } catch (e: Exception) {
            Log.e("PassiveDataService", "Erreur init HealthConnect (normal si absent sur montre)", e)
        }
    }

    // Cette méthode est appelée quand de nouvelles données arrivent (ex: FC, Pas)
    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        serviceScope.launch {
            /*
            // 0. Log pour voir si on entre ici
            Log.d("PassiveDataService", "DONNÉES REÇUES DU SYSTÈME !")

            val samples = mutableListOf<MetricSample>()
            // Calcul du temps de démarrage pour convertir les timestamps relatifs en absolus
            val bootInstant = Instant.now().minusNanos(SystemClock.elapsedRealtimeNanos())

            // --- 1. TRAITEMENT FRÉQUENCE CARDIAQUE ---
            val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
            hrPoints.forEach { point ->
                val value = point.value

                // A. Logique de Debug / UI
                Log.d("PassiveDataService", "❤️ FC Reçue : $value")
                SensorDataRepository.latestHeartRate.value = value

                // B. Logique de Stockage (Conversion en MetricSample)
                val pointTime = point.getTimeInstant(bootInstant)
                samples.add(
                    MetricSample(
                        MetricType.HEART_RATE,
                        value,
                        pointTime,
                        pointTime
                    )
                )
            }

            // --- 2. TRAITEMENT DES PAS ---
            val stepPoints = dataPoints.getData(DataType.STEPS_DAILY)
            stepPoints.forEach { point ->
                val value = point.value

                // A. Logique de Debug / UI
                Log.d("PassiveDataService", "👣 Pas Reçus : $value")
                SensorDataRepository.latestSteps.value = value

                // B. Logique de Stockage (Conversion en MetricSample)
                samples.add(
                    MetricSample(
                        MetricType.STEP_COUNT,
                        value.toDouble(),
                        point.getStartInstant(bootInstant),
                        point.getEndInstant(bootInstant)
                    )
                )
            }



            // --- 3. ÉCRITURE / ENVOI ---
            if (samples.isNotEmpty()) {
                // On vérifie si le writer est initialisé (pour éviter crash sur émulateur sans HC)
                if (::hcWriter.isInitialized) {
                    try {
                        hcWriter.writeBatch(samples)
                        Log.d("PassiveDataService", "Données envoyées au writer")
                    } catch (e: Exception) {
                        Log.e("PassiveDataService", "Erreur lors de l'écriture", e)
                    }
                } else {
                    Log.w("PassiveDataService", "Writer non initialisé, données ignorées pour le stockage.")
                }
            }*/

            // Variables pour stocker les dernières valeurs trouvées dans ce lot
            var lastHr: Double? = null
            var lastSteps: Long? = null

            // --- 1. TRAITEMENT FRÉQUENCE CARDIAQUE ---
            val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
            hrPoints.lastOrNull()?.let { point ->
                lastHr = point.value
                // Mise à jour UI Montre (Debug)
                Log.d("PassiveDataService", "❤️ FC : $lastHr")
                SensorDataRepository.latestHeartRate.value = lastHr!!
            }

            // --- 2. TRAITEMENT DES PAS ---
            val stepPoints = dataPoints.getData(DataType.STEPS_DAILY)
            stepPoints.lastOrNull()?.let { point ->
                lastSteps = point.value
                // Mise à jour UI Montre (Debug)
                Log.d("PassiveDataService", "👣 Pas : $lastSteps")
                SensorDataRepository.latestSteps.value = lastSteps!!
            }

            // --- 3. ENVOI AU TÉLÉPHONE (Data Layer) ---
            if (lastHr != null || lastSteps != null) {
                sendToPhone(lastHr, lastSteps)
            }
        }
    }





private suspend fun sendToPhone(hr: Double?, steps: Long?) {
    try {
        // On crée un chemin unique avec le timestamp pour forcer la synchro
        val timestamp = System.currentTimeMillis()
        val request = PutDataMapRequest.create("/health_data/$timestamp")

        request.dataMap.apply {
            putLong("timestamp", timestamp)
            if (hr != null) putDouble("heart_rate", hr)
            if (steps != null) putLong("steps", steps)
        }

        val putRequest = request.asPutDataRequest().setUrgent()

        // Envoi effectif
        Wearable.getDataClient(this).putDataItem(putRequest).await()

        Log.d("PassiveDataService", "📤 Données envoyées au téléphone -> HR:$hr, Steps:$steps")

    } catch (e: Exception) {
        Log.e("PassiveDataService", "Erreur d'envoi au téléphone", e)
    }
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
