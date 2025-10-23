package com.example.pllrun.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.pllrun.R

@Composable
fun AccueilScreen(onTimeout: () -> Unit) {
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
    LaunchedEffect(key1 = Unit) {
        delay(3000)
        onTimeout()
    }
}
