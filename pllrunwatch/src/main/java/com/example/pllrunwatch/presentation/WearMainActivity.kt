/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.pllrunwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.pllrunwatch.R
import com.example.pllrunwatch.presentation.health.HealthConnectWriter
import com.example.pllrunwatch.presentation.health.MetricSample
import com.example.pllrunwatch.presentation.health.MetricType
import com.example.pllrunwatch.presentation.health.PassiveMonitorCoordinator
import com.example.pllrunwatch.presentation.theme.PllRunTheme
import java.time.Instant


import android.util.Log

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class WearMainActivity : ComponentActivity() {
    // CHANGÉ : On utilise lateinit pour les deux, car leur création dépend d'une condition.
    private lateinit var hcWriter: HealthConnectWriter
    private lateinit var coordinator: PassiveMonitorCoordinator

    // Pour gérer l'état de la disponibilité de Health Connect
    private var healthConnectAvailable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // On vérifie la disponibilité de Health Connect
        checkHealthConnectAvailability()

        setContent {
            // L'UI peut maintenant réagir à la disponibilité de Health Connect
            if (healthConnectAvailable) {
                // Initialise les composants qui en dépendent
                initialiseHealthConnectDependentComponents()

                // On peut démarrer le coordinateur ici ou dans un LaunchedEffect
                LaunchedEffect(Unit) {
                    coordinator.start()
                }

                WearApp("Connecté !")
            } else {
                WearApp("Health Connect non disponible")
            }
        }
    }

    private fun checkHealthConnectAvailability() {
        val availability = HealthConnectClient.getSdkStatus(this, "com.google.android.apps.healthdata")
        if (availability == HealthConnectClient.SDK_UNAVAILABLE) {
            Log.d("WearMainActivity", "Health Connect n'est pas disponible sur cet appareil.")
            healthConnectAvailable = false
            return // Ne rien faire de plus
        }
        if (availability == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            Log.d("WearMainActivity", "Une mise à jour de Health Connect est requise.")
            healthConnectAvailable = false
            // Ici, vous pourriez déclencher une action pour demander à l'utilisateur de mettre à jour
            return
        }
        Log.d("WearMainActivity", "Health Connect est disponible.")
        healthConnectAvailable = true
    }

    private fun initialiseHealthConnectDependentComponents() {
        // S'assure de n'initialiser qu'une seule fois
        if (!::hcWriter.isInitialized) {
            // --- CORRECTION ICI ---
            // On utilise la méthode 'getOrCreate' pour obtenir une instance du client.
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            hcWriter = HealthConnectWriter(healthConnectClient)
            coordinator = PassiveMonitorCoordinator(hcWriter, lifecycleScope)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // S'assure que le coordinateur a été initialisé avant de l'arrêter
        if (::coordinator.isInitialized) {
            coordinator.stop()
        }
    }

    // Fonction d'exemple (reste inchangée)
    private fun onPassiveSampleFromSystem(type: MetricType, value: Double) {
        if (::coordinator.isInitialized) {
            val now = Instant.now()
            coordinator.onNewSample(MetricSample(type, value, now, now))
        }
    }
}

// Les autres @Composables (WearApp, Greeting, DefaultPreview) restent inchangés.


@Composable
fun WearApp(greetingName: String) {
    PllRunTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}