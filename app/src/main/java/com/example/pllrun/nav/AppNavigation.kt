package com.example.pllrun.nav

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
//import com.example.pllrun.AccueilScreen
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.screens.AccueilScreen
import com.example.pllrun.screens.EnregistrementScreen
import com.example.pllrun.screens.ObjectifScreen
import com.example.pllrun.screens.HubScreen
import com.example.pllrun.screens.PlanningSportScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

enum class AppScreen {
    Accueil,
    Enregistrement,
    Objectif,
    Hub,
    PlanningSport
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: InventaireViewModel
) {

    // --- DÉFINITION DES ROUTES AVEC ARGUMENTS ---
    // On définit les routes ici pour la clarté et pour éviter les "chaînes magiques".
    val routeEnregistrement = "${AppScreen.Enregistrement.name}?utilisateurId={utilisateurId}"
    val argumentUtilisateurId = "utilisateurId"

    // État pour vérifier si l'utilisateur est inscrit

    NavHost(
        navController = navController,
        startDestination = AppScreen.Accueil.name
    ) {
        composable(route = AppScreen.Accueil.name) {
            AccueilScreen(navController = navController,
                viewModel = viewModel)


        }
        // --- Écran d'Enregistrement
        composable(
            route = routeEnregistrement, // On utilise notre variable de route
            // On déclare que 'utilisateurId' est un argument optionnel (nullable = true)
            arguments = listOf(navArgument(argumentUtilisateurId) {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            // On récupère la valeur de l'argument
            val utilisateurId = backStackEntry.arguments?.getString(argumentUtilisateurId)

            EnregistrementScreen(
                viewModel = viewModel,
                // On convertit l'ID (String) en Long, ou on passe null si absent
                utilisateurId = utilisateurId?.toLongOrNull(),
                onNext = { navController.navigate(AppScreen.Hub.name) },

            )
        }
        composable(route = AppScreen.Objectif.name) {
            ObjectifScreen(
                onSaveAndNext = {
                    navController.navigate(AppScreen.Hub.name) {
                        popUpTo(AppScreen.Accueil.name) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(AppScreen.Hub.name) {
                        popUpTo(AppScreen.Accueil.name) { inclusive = true }
                    }
                },
                viewModel = viewModel,
                utilisateurId = 1
            )
        }
        composable(route = AppScreen.Hub.name) {

            val utilisateurId by viewModel.getFirstUtilisateur().observeAsState()
            HubScreen(
                onEditProfile = {
                    // On construit la route de destination en remplaçant l'argument
                    val destination = routeEnregistrement.replace(
                        "{$argumentUtilisateurId}",
                        utilisateurId?.id.toString()
                    )
                    navController.navigate(destination)
                },
                onPlanningSport = {
                    navController.navigate(AppScreen.PlanningSport.name)
                },
                onAddGoal = {
                    navController.navigate(AppScreen.Objectif.name)
                },

                viewModel = viewModel,

            )
        }
        composable(route = AppScreen.PlanningSport.name) {
            PlanningSportScreen(
                viewModel = viewModel,
                utilisateurId = 1
            )
        }

    }
}

suspend fun verifConnexion(viewModel: InventaireViewModel): Boolean{
    return viewModel.getAllUtilisateurs().isInitialized
}