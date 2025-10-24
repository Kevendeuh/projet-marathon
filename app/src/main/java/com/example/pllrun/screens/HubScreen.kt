package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.R
import com.example.pllrun.calculator.calculHeureCouche
import com.example.pllrun.calculator.calculTotalCalories
import com.example.pllrun.calculator.calculTotalMinutesSleep
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalTime


@Composable
fun HubScreen(
    viewModel: InventaireViewModel,
    onEditProfile: () -> Unit,
    onPlanningSport: () -> Unit,
    onAddGoal: () -> Unit
) {
    /**
     * Variables.
     */
    var utilisateurPrincipal by remember { mutableStateOf<Utilisateur?>(null) }
    var tempsSommeilSuggere by remember { mutableStateOf<Long?>(null) }
    var heureCoucheSuggeree by remember { mutableStateOf<LocalTime?>(null) }
    var totalCaloriesSuggeree by remember { mutableStateOf<Float?>(null) }


    // Récupère le premier utilisateur de manière sûre.

    LaunchedEffect(key1 = true) {
        val user = viewModel.getAllUtilisateurs().firstOrNull()?.firstOrNull()
        if (user != null) {
            utilisateurPrincipal = user
            tempsSommeilSuggere = calculTotalMinutesSleep(user, viewModel)
            heureCoucheSuggeree = calculHeureCouche(user, viewModel)
            totalCaloriesSuggeree = calculTotalCalories( user, viewModel)
        }
    }


        Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1)) // Fond gris clair
            .padding(24.dp)
    ) {
        // Header avec titre et bouton modifier
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hub",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            TextButton(onClick = onEditProfile) {
                Text(
                    text = "Modifier profil",
                    fontSize = 16.sp,
                    color = Color(0xFFFF751F) // Orange
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Carte "A Faire"
        TaskCard(
            title = "A Faire",
            content = "Vos tâches à compléter apparaîtront ici",
            onThreeDotsClick = onPlanningSport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // Section "Défis À Suivre"
        Text(
            text = "Défis À Suivre",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Cartes Sommeil et À Manger côte à côte
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carte Sommeil
            var descriptionSommeil= "estimation impossible veuillez renseigner utilisateur"
            if (tempsSommeilSuggere != null) {
            descriptionSommeil = " temps de sommeil suggéré:$tempsSommeilSuggere minutes \n heure de couche suggérée:$heureCoucheSuggeree"
            }
            TaskCard(
                title = "Sommeil",
                content = descriptionSommeil,
                onThreeDotsClick = { /* TODO: Naviguer vers écran sommeil */ },
                modifier = Modifier.weight(1f)
            )

            // Carte À Manger
            var descriptionCalories= "estimation impossible veuillez renseigner utilisateur"
            if (totalCaloriesSuggeree != null) {
                descriptionCalories = " Calories suggérées:$totalCaloriesSuggeree"
            }
            TaskCard(
                title = "À Manger",
                content = descriptionCalories,
                onThreeDotsClick = { /* TODO: Naviguer vers écran nutrition */ },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton "Ajouter Objectif"
        Button(
            onClick = onAddGoal,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF751F) // Orange
            )
        ) {
            Text(
                text = "Ajouter Objectif",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// Composant réutilisable pour les cartes de tâches
@Composable
fun TaskCard(
    title: String,
    content: String,
    onThreeDotsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Titre de la carte
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Contenu de la carte
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Ligne des trois points en bas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onThreeDotsClick)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}