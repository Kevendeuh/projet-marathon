/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.pllrunwatch.presentation

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.pllrunwatch.presentation.health.PassiveDataService
import com.example.pllrunwatch.presentation.theme.PllRunTheme
import java.time.Instant

import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.values
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.wear.compose.material.Button
import com.example.pllrunwatch.presentation.health.HealthServicesManager
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit

class WearMainActivity : ComponentActivity() {
    // CHANGÉ : On utilise lateinit pour les deux, car leur création dépend d'une condition.
    private lateinit var hcWriter: HealthConnectWriter
    private lateinit var healthServicesManager: HealthServicesManager

    // Pour gérer l'état de la disponibilité de Health Connect
    private var healthConnectAvailable by mutableStateOf(false)
    // État pour savoir si on a les permissions (pour mettre à jour l'UI)
    private var permissionsGranted by mutableStateOf(false)
    // --- ÉTATS POUR LE DEBUG ---
    private var lastHeartRate by mutableStateOf("--")
    private var todaySteps by mutableStateOf("--")


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        healthServicesManager = HealthServicesManager(this)

        // On vérifie la disponibilité de Health Connect
        checkHealthConnectAvailability()

        setContent {
            // Liste des permissions Android ET Health Connect
            val permissions = arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                // Permissions Health Connect (nécessaires pour lire/écrire)
                "android.permission.health.READ_HEART_RATE",
                "android.permission.health.WRITE_HEART_RATE",
                "android.permission.health.READ_STEPS",
                "android.permission.health.WRITE_STEPS"
            )

            // Launcher pour demander les permissions
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                // Log des résultats
                result.forEach { (perm, granted) -> Log.d("DEBUG_PERM", "$perm -> $granted") }

                val allGranted = result.values.all { it }
                permissionsGranted = allGranted
                if (allGranted) {
                    Log.d("WearMainActivity", "Permissions accordées via Launcher.")
                    activatePassiveMonitoring()
                } else {
                    Log.e("WearMainActivity", "Permissions refusées via Launcher.")
                }
            }

            // L'UI peut maintenant réagir à la disponibilité de Health Connect
            if (healthConnectAvailable) {

                // Au démarrage de l'écran, on vérifie si on a déjà les permissions
                LaunchedEffect(Unit) {
                    if (hasPermissions(permissions)) {
                        permissionsGranted = true
                        activatePassiveMonitoring()
                        // Lecture initiale
                        readDebugData()
                    } else {
                        // Si non, on les demande
                        permissionLauncher.launch(permissions)
                    }
                }

                if (permissionsGranted) {
                    // Affiche l'écran de Debug avec les données
                    DebugScreen(
                        heartRate = lastHeartRate,
                        steps = todaySteps,
                        onRefresh = { readDebugData() }
                    )
                } else {
                    // Affiche un écran d'attente
                    WearApp("Permissions requises...")
                }

            } else {
                WearApp("Health Connect requis")
            }
        }
    }

    /**
     * Fonction simple pour lire les dernières données dans Health Connect
     * et vérifier que l'écriture s'est bien passée.
     */
    private fun readDebugData() {
        lifecycleScope.launch {
            try {
                val client = HealthConnectClient.getOrCreate(this@WearMainActivity)
                val now = Instant.now()
                val startTime = now.minus(1, ChronoUnit.DAYS) // On regarde les dernières 24h

                // 1. Lire la dernière FC
                val hrResponse = client.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, now),
                        ascendingOrder = false, // Le plus récent en premier
                        pageSize = 1
                    )
                )
                if (hrResponse.records.isNotEmpty()) {
                    val lastRecord = hrResponse.records.first()
                    // On prend le dernier échantillon de l'enregistrement
                    val lastBpm = lastRecord.samples.lastOrNull()?.beatsPerMinute ?: 0
                    lastHeartRate = "$lastBpm bpm"
                } else {
                    lastHeartRate = "Aucune donnée"
                }

                // 2. Lire les pas (total sur la période)
                // Note: Pour avoir le total exact du jour, il faut agréger, mais ici on lit juste les records bruts pour debug
                val stepsResponse = client.readRecords(
                    androidx.health.connect.client.request.ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, now),
                        ascendingOrder = false,
                        pageSize = 100
                    )
                )
                val totalSteps = stepsResponse.records.sumOf { it.count }
                todaySteps = "$totalSteps pas (24h)"

            } catch (e: Exception) {
                Log.e("Debug", "Erreur lecture", e)
                lastHeartRate = "Erreur"
                todaySteps = "Erreur"
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
    private fun activatePassiveMonitoring() {
        lifecycleScope.launch {
            // NOTE IMPORTANTE : Dans une vraie app, vous devez demander les permissions ici
            // (android.permission.BODY_SENSORS) avec un requestPermissions launcher
            // avant d'appeler registerForPassiveData.

            try {
                // C'est ici qu'on dit au système : "Réveille PassiveDataService quand tu as des données"
                healthServicesManager.registerForPassiveData()
                Log.d("WearMainActivity", "Monitoring passif activé avec succès !")
            } catch (e: Exception) {
                Log.e("WearMainActivity", "Erreur lors de l'activation du monitoring passif", e)
            }
        }
    }

    private fun stopPassiveMonitoring() {
        lifecycleScope.launch {
            healthServicesManager.unregisterPassiveData()
        }
    }

    // Fonction utilitaire pour vérifier si les permissions sont déjà là
    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onDestroy() {
        super.onDestroy()

    }

}

// Les autres @Composables (WearApp, Greeting, DefaultPreview) restent inchangés.

// --- NOUVEL ÉCRAN DE DEBUG ---
@Composable
fun DebugScreen(
    heartRate: String,
    steps: String,
    onRefresh: () -> Unit
) {
    PllRunTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(top = 20.dp), // Padding pour éviter le menton de la montre
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DEBUG HEALTH",
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.title3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "❤️ FC: $heartRate",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\uD83D\uDC5F Pas: $steps",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRefresh,
                modifier = Modifier.height(40.dp)
            ) {
                Text("Rafraîchir")
            }
        }
    }
}

@Composable
fun WearApp(message: String) {
    PllRunTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeText()
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = message
            )
        }
    }
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}