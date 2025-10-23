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
    var isUserRegistered by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = AppScreen.Accueil.name
    ) {
        composable(route = AppScreen.Accueil.name) {
            AccueilScreen(
                onTimeout = {
                    // Après 3 secondes, naviguer vers l'écran approprié
                    if (isUserRegistered) {
                        navController.navigate(AppScreen.Hub.name) {
                            popUpTo(AppScreen.Accueil.name) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppScreen.Enregistrement.name) {
                            popUpTo(AppScreen.Accueil.name) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(route = AppScreen.Enregistrement.name) {
            EnregistrementScreen(
                onNext = {
                    navController.navigate(AppScreen.Objectif.name)
                },
                onSave = {
                    // Sauvegarder les infos et marquer l'utilisateur comme inscrit
                    isUserRegistered = true
                }
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
                }
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
                }
            )
        }
        composable(route = AppScreen.PlanningSport.name) {
            PlanningSportScreen()
        }
    }
}