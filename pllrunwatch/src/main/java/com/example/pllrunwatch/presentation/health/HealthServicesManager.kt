package com.example.pllrunwatch.presentation.health

// wear/src/main/java/.../health/HealthServicesManager.kt

// Dans HealthServicesManager.kt

import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import android.content.Context
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.tasks.await

class HealthServicesManager(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient

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

    suspend fun unregisterPassiveData() {
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}

