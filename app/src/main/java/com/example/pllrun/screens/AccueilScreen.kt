package com.example.pllrun.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.pllrun.InventaireViewModel
import kotlinx.coroutines.delay
import com.example.pllrun.R
import com.example.pllrun.nav.AppScreen
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun AccueilScreen(navController: NavHostController, viewModel: InventaireViewModel) {
    var isLoading by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFDEA6)), // Fond couleur #FFDEA6
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Remplacez R.drawable.logo_fitgoal par le nom réel de votre image
            Image(
                painter = painterResource(id = R.drawable.logo_fitgoal),
                contentDescription = "Logo FIT GOAL",
                modifier = Modifier.size(500.dp) // Ajustez la taille selon votre logo
            )
        }
    }

    // Navigation automatique après 3 secondes
    LaunchedEffect(key1 = true) {
        // On récupère le premier utilisateur. firstOrNull() est non-bloquant et sûr.
        val userExists = viewModel.getAllUtilisateurs().firstOrNull()?.isNotEmpty() ?: false

        // On détermine la destination
        val destination = if (userExists) AppScreen.Hub.name else AppScreen.Enregistrement.name

        // On navigue vers la bonne destination
        navController.navigate(destination) {
            // popUpTo(AppScreen.Splash.name) empêche l'utilisateur de revenir
            // à l'écran de chargement avec le bouton "Retour".
            popUpTo(AppScreen.Accueil.name) { inclusive = true }
        }

        // Optionnel : garder l'écran de chargement visible un court instant
        delay(1000)
        isLoading = false
    }
}
