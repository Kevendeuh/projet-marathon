package com.example.pllrun.nav

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.navigation.navArgument
//import com.example.pllrun.AccueilScreen // Nous allons créer cet écran
import com.example.pllrun.InventaireViewModel

/**
 * Énumération pour définir les routes de l'application de manière sûre.
 */
enum class AppScreen {
    Accueil,
    Enregistrement,
    Objectif,
    Hub,
    PlanningSport
}

/**
 * Le composant principal qui gère le graphe de navigation.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: InventaireViewModel
) {
   NavHost(
        navController = navController,
        startDestination = AppScreen.Accueil.name // L'écran de démarrage
    ) {
        // Définition de chaque écran dans le graphe de navigation
        composable(route = AppScreen.Accueil.name) {
            // Votre ancien 'UtilisateurScreen' devient l'écran d'accueil
            /**AccueilScreen(
               * viewModel = viewModel,
                *onNavigate = { route -> navController.navigate(route) }
            )**/
        }
        composable(route = AppScreen.Enregistrement.name) {
            EnregistrementScreen() // Écran vide pour l'instant
        }
        composable(route = AppScreen.Objectif.name) {
            ObjectifScreen() // Écran vide pour l'instant
        }
        composable(route = AppScreen.Hub.name) {
            HubScreen() // Écran vide pour l'instant
        }
        composable(route = AppScreen.PlanningSport.name) {
            PlanningSportScreen() // Écran vide pour l'instant
        }
    }
}

// --- Écrans Vides (Placeholders) ---
// Vous pourrez développer ces écrans plus tard.

@Composable
fun EnregistrementScreen() {
    Text("Écran d'Enregistrement")
}

@Composable
fun ObjectifScreen() {
    Text("Écran des Objectifs")
}

@Composable
fun HubScreen() {
    Text("Écran Hub")
}

@Composable
fun PlanningSportScreen() {
    Text("Écran du Planning Sportif")
}
