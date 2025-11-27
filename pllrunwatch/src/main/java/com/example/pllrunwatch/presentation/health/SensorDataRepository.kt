package com.example.pllrunwatch.presentation.health

import kotlinx.coroutines.flow.MutableStateFlow

// Un objet simple pour stocker les dernières valeurs reçues en mémoire
object SensorDataRepository {
    // StateFlow permet à l'UI de se mettre à jour automatiquement quand la valeur change
    val latestHeartRate = MutableStateFlow(0.0)
    val latestSteps = MutableStateFlow(0L)
}
