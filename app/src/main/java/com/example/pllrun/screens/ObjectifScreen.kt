package com.example.pllrun.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ObjectifScreen(
    onSaveAndNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Quel est votre objectif ?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // TODO: Ajouter le TextField pour l'objectif

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Sauvegarder l'objectif */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSaveAndNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Suivant")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ignorer pour l'instant")
        }
    }
}