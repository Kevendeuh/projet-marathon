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
    // État pour vérifier si l'utilisateur est inscrit
    var idUtilisateur by remember { mutableStateOf(0) }

    NavHost(
        navController = navController,
        startDestination = AppScreen.Accueil.name
    ) {
        composable(route = AppScreen.Accueil.name) {
            AccueilScreen(navController = navController,
                viewModel = viewModel)


        }

        composable(route = AppScreen.Enregistrement.name) {
            EnregistrementScreen(
                onNext = {
                    navController.navigate(AppScreen.Objectif.name)
                },
                viewModel = viewModel
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
            HubScreen(
                onEditProfile = {
                    navController.navigate(AppScreen.Enregistrement.name)
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