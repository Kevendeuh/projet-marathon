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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pllrun.InventaireViewModel
import kotlinx.coroutines.delay
import com.example.pllrun.R
import com.example.pllrun.nav.AppScreen
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun AccueilScreen(navController: NavHostController, viewModel: InventaireViewModel) {
    // --- 1. OBSERVATION RÉACTIVE ---
    // On observe le premier utilisateur. La valeur initiale est `null`.
    // `observeAsState` nous donnera une nouvelle valeur dès que le LiveData sera mis à jour.
    // On utilise une valeur spéciale (Unit) comme état initial pour distinguer "chargement" de "non trouvé".
    val firstUser by viewModel.getFirstUtilisateur().observeAsState(initial = Unit)

    // --- 2. EFFET DE NAVIGATION ---
    // LaunchedEffect se déclenchera chaque fois que `firstUser` change.
    LaunchedEffect(firstUser) {
        // On attend que l'observation nous donne un résultat (pas l'état initial 'Unit').
        if (firstUser is com.example.pllrun.Classes.Utilisateur? && firstUser != Unit) {
            // L'observation est terminée. On sait si l'utilisateur existe ou non.
            val userExists = (firstUser != null)

            // On détermine la destination
            val destination = if (userExists) {
                AppScreen.Hub.name
            } else {
                AppScreen.Enregistrement.name
            }

            // On ajoute un petit délai pour que le logo soit visible
            delay(1000)

            // On navigue vers la bonne destination en nettoyant la pile de navigation
            navController.navigate(destination) {
                // Efface la pile de navigation jusqu'à l'écran d'accueil inclus,
                // pour empêcher l'utilisateur de revenir en arrière sur cet écran de chargement.
                popUpTo(AppScreen.Accueil.name) { inclusive = true }
            }
        }
    }

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



}
