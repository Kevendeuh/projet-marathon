/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.pllrunwatch.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.pllrunwatch.presentation.health.HealthConnectWriter
import com.example.pllrunwatch.presentation.health.HealthServicesManager
import com.example.pllrunwatch.presentation.health.SensorDataRepository
import com.example.pllrunwatch.presentation.theme.PllRunTheme
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {

    private lateinit var healthServicesManager: HealthServicesManager

    // Enum pour gérer proprement l'état de l'UI
    enum class PermissionState {
        LOADING,
        NEEDS_BODY_SENSORS,
        NEEDS_ACTIVITY_RECOGNITION,
        NEEDS_BACKGROUND_SETTINGS,
        GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        healthServicesManager = HealthServicesManager(this)

        setContent {
            PllRunTheme {

                // 1. Launcher pour le PREMIER PLAN uniquement
                val foregroundPermissions = remember { arrayOf(
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACTIVITY_RECOGNITION
                    )
                }

                // 2. État de l'UI calculé au chargement
                var uiState by remember {
                    mutableStateOf(checkPermissionsState())
                }

                // 3. Le Launcher (Doit être déclaré inconditionnellement au début)
                val foregroundLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    // Callback appelé au retour de la pop-up
                    val allGranted = result.values.all { it }
                    if (allGranted) {
                        Log.d("Main", "Pop-up: Permissions accordées !")
                        // On met à jour l'état pour passer à la suite (check background)
                        uiState = checkPermissionsState()
                    } else {
                        Log.e("Main", "Pop-up: Permissions refusées.")
                        uiState = PermissionState.NEEDS_BODY_SENSORS
                    }
                }


                // 3. Action selon l'état
                LaunchedEffect(uiState) {
                    if (uiState == PermissionState.GRANTED) {
                        activatePassiveMonitoring()
                    }
                }

                // 5. Gestion du retour des paramètres (Cycle de vie OnResume simplifié)
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            // Au retour (ex: depuis les paramètres), on force une revérification
                            // seulement si on n'est pas déjà OK pour éviter des clignotements
                            if (uiState != PermissionState.GRANTED) {
                                uiState = checkPermissionsState()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // --- UI ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState) {
                        PermissionState.LOADING -> {
                            Text("Chargement...")
                        }
                        PermissionState.GRANTED -> {
                            // Ce bloc ne vit que tant que l'écran affiche "GRANTED"
                            LaunchedEffect(Unit) {
                                Log.d("Main", "Lancement du flux Actif (Foreground)")
                                // On s'abonne au flux actif défini dans HealthServicesManager
                                launch {
                                    healthServicesManager.observeActiveHeartRate()
                                        .collect { activeHeartRate ->
                                            SensorDataRepository.latestHeartRate.value = activeHeartRate
                                            Log.d("Main", "⚡ FC Active reçue: $activeHeartRate")
                                        }
                                }
                                // Lancer la collecte PAS dans une autre coroutine séparée
                                launch {
                                    healthServicesManager.observeActiveStepCount()
                                        .collect { activeStepCount ->
                                            SensorDataRepository.latestSteps.value = activeStepCount
                                            Log.d("Main", "Pas Actifs reçus: $activeStepCount")
                                        }
                                }
                            }
                            // ----------------------------------------------------------

                            // Lecture des données depuis le Repository (mis à jour par le Passif ET l'Actif)
                            val heartRate by SensorDataRepository.latestHeartRate.collectAsState()
                            val steps by SensorDataRepository.latestSteps.collectAsState()

                            DebugScreen(hr = heartRate, steps = steps)

                        }
                        PermissionState.NEEDS_ACTIVITY_RECOGNITION -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Capteurs requis", color = Color.Red)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { foregroundLauncher.launch(foregroundPermissions)
                                    Log.d("Main", "Clic bouton -> Lancement Pop-up")}) {
                                    Text("Autoriser")
                                }
                            }
                        }

                        PermissionState.NEEDS_BACKGROUND_SETTINGS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Autorisation en arrière-plan requise", color = Color.Yellow)
                                Text("Ouvrez les paramètres pour 'Toujours autoriser'")
                                Button(onClick = { openAppSystemSettings() }) {
                                    Text("Ouvrir paramètres")
                                }
                            }
                        }
                        PermissionState.NEEDS_BODY_SENSORS -> {
                            // CAS CRITIQUE : On doit envoyer l'utilisateur dans les paramètres
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                                Text("Mode 'Toujours' requis", color = Color.Yellow, textAlign = TextAlign.Center, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Paramètres > Apps > Permissions > Capteurs > Toujours autoriser", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(onClick = { openAppSystemSettings() }) {
                                    Text("Ouvrir Paramètres", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                // Bouton de secours pour l'émulateur si l'API renvoie faux alors que c'est coché
                                Button(
                                    onClick = {
                                        // Force le passage (HACK EMULATEUR)
                                        activatePassiveMonitoring()
                                        uiState = PermissionState.GRANTED
                                    },
                                    colors = ButtonDefaults.secondaryButtonColors(),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("J'ai déjà fait", fontSize = 10.sp)
                                }
                            }
                        }

                    }
                }
            }
        }
    }


    // Logique de vérification stricte selon la doc Android
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionsState(): PermissionState {

        val body = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED

        if (!body) return PermissionState.NEEDS_BODY_SENSORS


        val act = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (!act) return PermissionState.NEEDS_ACTIVITY_RECOGNITION


        // Android 13 / Wear OS 4 → background requires system settings
        val background = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS_BACKGROUND
        ) == PackageManager.PERMISSION_GRANTED

        if (!background) {
            return PermissionState.NEEDS_BACKGROUND_SETTINGS
        }


        return PermissionState.GRANTED
    }

    // Fonction officielle pour ouvrir les paramètres de l'app
    private fun openAppSystemSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun activatePassiveMonitoring() {
        lifecycleScope.launch {
            try {
                healthServicesManager.registerForPassiveData()
                Log.d("Main", "Monitoring passif activé !")
            } catch (e: Exception) {
                Log.e("Main", "Erreur activation", e)
            }
        }
    }
}

// Les autres @Composables (WearApp, Greeting, DefaultPreview) restent inchangés.

// --- NOUVEL ÉCRAN DE DEBUG ---
@Composable
fun DebugScreen(hr: Double, steps: Long) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TEST CAPTEURS", color = MaterialTheme.colors.primary)
        Spacer(modifier = Modifier.height(10.dp))

        Text("❤️ FC: ${hr.toInt()} bpm", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Text("👣 Pas: $steps", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(15.dp))
        Text("Bougez la montre ou\nsimulez des données !", fontSize = 10.sp, color = Color.Gray)
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