package com.example.pllrunwatch.presentation.health

// wear/src/main/java/.../health/HealthServicesManager.kt

// Dans HealthServicesManager.kt

import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import android.content.Context
import android.util.Log
import androidx.activity.result.launch
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.unregisterMeasureCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

class HealthServicesManager(context: Context) {

    private val dataLayerSender = DataLayerSender(context)
    private val healthServicesClient = HealthServices.getClient(context)
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    // 1. On récupère le client de mesure active
    private val measureClient = healthServicesClient.measureClient

    suspend fun registerForPassiveData() {
        // On configure ce qu'on veut écouter
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM, DataType.STEPS_DAILY))
            .build()

        // On s'abonne. Le système enverra les données à PassiveDataService::class.java
        // On sépare l'appel pour être clair
        val task = passiveMonitoringClient.setPassiveListenerServiceAsync(
            PassiveDataService::class.java,
            config
        )

        // On appelle await() sans type générique sur la variable
        task.await()
    }

    // 2. Nouvelle fonction pour écouter en temps réel (Active Monitoring)
    // On utilise un Flow pour pouvoir facilement démarrer/arrêter l'écoute depuis l'UI
    fun observeActiveHeartRate() = callbackFlow {

        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Gérer les changements de disponibilité (capteur mal positionné, etc.)
            }

            override fun onDataReceived(data: DataPointContainer) {
                // Réception des données en temps réel !
                val heartRateSamples = data.getData(DataType.HEART_RATE_BPM)
                heartRateSamples.lastOrNull()?.let { point ->
                    //Log.d("ActiveMonitoring", "⚡ FC Temps réel : ${point.value}")
                    // On envoie dans le channel du Flow
                    trySend(point.value)
                    val bpm = point.value.toInt()

                    // Lancer une coroutine pour envoyer la donnée
                    CoroutineScope(Dispatchers.IO).launch {
                        dataLayerSender.sendSmallDelta(lastHr = bpm, stepsToday = null)
                        Log.d("ActiveMonitoring", "⚡ FC envoyé : ${point.value}")

                    }
                }
            }
        }

        Log.d("ActiveMonitoring", "Démarrage de la mesure active...")
        // Enregistrement du callback pour la fréquence cardiaque
        measureClient.registerMeasureCallback( DataType.HEART_RATE_BPM, callback)

        // Cette partie est appelée quand le Flow est annulé (quand l'UI est fermée)
        awaitClose {
            Log.d("ActiveMonitoring", "Arrêt de la mesure active.")
            runBlocking {
                measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, callback)
            }
        }
    }

    //3. Nouvelle fonction pour écouter les PAS en temps réel
    fun observeActiveStepCount() = callbackFlow {
        var sessionTotalSteps = 0L
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Gérer la disponibilité si nécessaire
            }

            override fun onDataReceived(data: DataPointContainer) {
                // Réception des données de pas (STEPS_DAILY donne le total journalier)
                val stepDelta = data.getData(DataType.STEPS).lastOrNull()?.value
                if (stepDelta!= null) {
                    // On additionne ces nouveaux pas à notre total
                    sessionTotalSteps += stepDelta
                    Log.d(
                        "ActiveMonitoring",
                        "👣 Pas (Delta): $stepDelta | Total: $sessionTotalSteps"
                    )
                }
                    val stepSamples = data.getData(DataType.STEPS)
                stepSamples.lastOrNull()?.let { point ->
                    Log.d("ActiveMonitoring", "👣 Pas Temps réel : ${point.value}")
                    // point.value est un Long pour les pas
                    trySend(point.value)
                }
            }
        }

        Log.d("ActiveMonitoring", "Démarrage de la mesure active des pas...")

        // Enregistrement du callback (Utilisation de la version Async avec await)
        measureClient.registerMeasureCallback(DataType.STEPS, callback)

        awaitClose {
            Log.d("ActiveMonitoring", "Arrêt de la mesure active des pas.")
            runBlocking {
                measureClient.unregisterMeasureCallback(DataType.STEPS, callback)
            }
        }
    }

    suspend fun unregisterPassiveData() {
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}

