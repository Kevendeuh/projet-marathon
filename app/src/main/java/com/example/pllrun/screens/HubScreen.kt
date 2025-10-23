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
fun HubScreen(
    onEditProfile: () -> Unit,
    onPlanningSport: () -> Unit,
    onAddGoal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hub",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onEditProfile) {
                Text("Modifier")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "A Faire",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Défis À Suivre", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sommeil")
            Text("À Manger")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAddGoal,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Ajouter Objectif")
        }
    }
}