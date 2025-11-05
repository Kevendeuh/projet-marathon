package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.pllrun.components.ObjectifEditDialog
import com.example.pllrun.components.ObjectifsListContent
import java.time.LocalTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.livedata.observeAsState
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.isEmpty
import com.example.pllrun.calculator.ApportsNutritionnels
import java.time.LocalDate


@Composable
fun HubScreen(
    viewModel: InventaireViewModel,
    onEditProfile: () -> Unit,
    onPlanningSport: () -> Unit,
    onAddGoal: () -> Unit,
) {
    // ... (Toutes les déclarations de variables restent les mêmes)
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedObjectifId by remember { mutableStateOf<Long?>(null) }

    val utilisateurPrincipal by viewModel.getFirstUtilisateur().observeAsState(initial = null)
    val activitesDuJour by viewModel.getActivitesForDay(LocalDate.now()).observeAsState(initial = emptyList())


    val sleepMinutes by viewModel.getRecommendedSleepTime(utilisateurPrincipal?.id ?: -1).observeAsState(0L)
    val bedtime by viewModel.getRecommendedBedtime(utilisateurPrincipal?.id ?: -1).observeAsState(LocalTime.of(22, 0))
    val nutriments by viewModel.getRecommendedNutriments(utilisateurPrincipal?.id ?: -1).observeAsState(ApportsNutritionnels(0F,0F,0F,0F))

    // --- 2. FORMATAGE DES DONNÉES ---
    val (tempsSommeilSuggere, heureCoucheSuggeree) = remember(sleepMinutes, bedtime) {
        val formattedSleepTime = if (sleepMinutes > 0) "${sleepMinutes / 60}h ${sleepMinutes % 60}min" else "N/A"
        val formattedBedtime = bedtime.format(DateTimeFormatter.ofPattern("HH:mm"))
        Pair(formattedSleepTime, formattedBedtime)
    }




    // --- STRUCTURE PRINCIPALE AVEC BOX ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
    ) {

        // --- 1. CONTENU PRINCIPAL (HEADER + LISTE SCROLLABLE) ---
        // Cette Column contient le header et la liste.
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
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

            // --- CONTENU SCROLLABLE ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Prend tout l'espace restant dans la Column
                    .padding(horizontal = 24.dp),
                // Ajoute un padding en bas pour que le dernier élément ne soit pas caché par le bouton
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    TaskCard(
                        title = "Objectifs en cours",
                        onThreeDotsClick = onPlanningSport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_flag),
                                contentDescription = "Icône d'objectif",
                                tint = Color(0xFFFF751F) // Couleur orange
                            )
                        },
                    ) {
                        utilisateurPrincipal?.let { user ->
                            ObjectifsListContent(
                                viewModel = viewModel,
                                utilisateurId = user.id,
                                onObjectifClick = { objectifId ->
                                    selectedObjectifId = objectifId
                                    showEditDialog = true
                                },
                            )
                        }
                    }
                }

                // --- CARTE ACTIVITÉS DU JOUR ---
                item {
                    TaskCard(
                        title = "Activités du jour",
                        onThreeDotsClick = onPlanningSport,
                        modifier = Modifier.padding(bottom = 24.dp),
                        icon = { Icon(painterResource(R.drawable.ic_flag), "Activités", tint = Color(0xFFFF751F)) }
                    ) {
                        if (activitesDuJour.isEmpty()) {
                            Text("Aucune activité planifiée pour aujourd’hui.", color = Color.Gray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                activitesDuJour.sortedBy { it.heureDeDebut }.forEach { act ->
                                    ActivityRow(act = act, onClick = onPlanningSport)
                                }
                            }
                        }
                    }
                }
                // --- SECTION CONSEILS SANTÉ ---
                item {
                    Text(
                        text = "Conseils santé",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        var descriptionSommeil =
                            "estimation impossible veuillez renseigner utilisateur"
                        if (tempsSommeilSuggere != null) {
                            descriptionSommeil =
                                " temps de sommeil suggéré:$tempsSommeilSuggere \n heure de couche suggérée:$heureCoucheSuggeree"
                        }
                        TaskCard(
                            title = "Sommeil",
                            // AJOUT : Passage de l'icône
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Icône de sommeil",
                                    tint = Color(0xFFFF751F) // Couleur orange
                                )
                            },
                            onThreeDotsClick = { /* TODO: Naviguer vers écran sommeil */ },
                            modifier = Modifier.weight(1f)
                        )
                        {
                            Text(
                                text = descriptionSommeil,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        TaskCard(
                            title = "À Manger",
                            // AJOUT : Passage de l'icône
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Icône de nourriture",
                                    tint = Color(0xFFFF751F)
                                )
                            },
                            onThreeDotsClick = { /* TODO: Naviguer vers écran nutrition */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            // On affiche les données formatées de l'objet nutriments
                            if (utilisateurPrincipal != null && nutriments.calories > 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "${nutriments.calories.toInt()} kcal",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "P: ${nutriments.proteines.toInt()}g | G: ${nutriments.glucides.toInt()}g | L: ${nutriments.lipides.toInt()}g",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Text("N/A", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // --- 2. BOUTON FLOTTANT ---
        Button(
            onClick = onAddGoal,
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligne le bouton en bas au centre de la Box
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp) // Espace autour du bouton
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF751F) // Orange
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp) // Ombre du bouton
        ) {
            Text(
                text = "Ajouter Objectif",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }


    // ---- AFFICHAGE CONDITIONNEL DU DIALOGUE ----
    selectedObjectifId?.let { id ->
        if (showEditDialog) {
            ObjectifEditDialog(
                viewModel = viewModel,
                objectifId = id,
                onDismiss = {
                    showEditDialog = false
                    selectedObjectifId = null
                }
            )
        }
    }
}

@Composable
fun ActivityRow(
    act: com.example.pllrun.Classes.Activite,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = act.nom,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            // Ligne d’infos : heure • type • niveau • statut
            val typeLibelle = try { act.typeActivite.libelle } catch (_: Throwable) { act.typeActivite.name }
            val niveauLibelle = try { act.niveau.libelle } catch (_: Throwable) { act.niveau.name }
            Text(
                text = "${act.heureDeDebut} • $typeLibelle • $niveauLibelle" +
                        if (act.estComplete) " • Terminé" else " • À faire",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )

            if (act.description.isNotBlank()) {
                Text(
                    text = act.description,
                    fontSize = 13.sp,
                    color = Color(0xFF7A7A7A)
                )
            }
        }
    }
}


// Dans HubScreen.kt

// Composant réutilisable pour les cartes de tâches
@Composable
fun TaskCard(
    title: String,
    onThreeDotsClick: () -> Unit,
    modifier: Modifier = Modifier,
    // AJOUT : Paramètre optionnel pour l'icône
    icon: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
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
            // MODIFICATION : Le titre est maintenant dans un Row pour accueillir l'icône
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Affiche l'icône si elle est fournie
                icon?.let {
                    it() // Exécute le Composable de l'icône
                    Spacer(modifier = Modifier.width(8.dp)) // Espace entre l'icône et le titre
                }
                // Titre de la carte
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            content()
            Spacer(modifier = Modifier.height(16.dp)) // Ajout d'un espace avant les points

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
    @Composable
    fun ActivityRow(
        act: com.example.pllrun.Classes.Activite,
        onClick: () -> Unit = {}
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = act.nom,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                // Ligne d’infos : heure • type • niveau • statut
                val typeLibelle = try { act.typeActivite.libelle } catch (_: Throwable) { act.typeActivite.name }
                val niveauLibelle = try { act.niveau.libelle } catch (_: Throwable) { act.niveau.name }
                Text(
                    text = "${act.heureDeDebut} • $typeLibelle • $niveauLibelle" +
                            if (act.estComplete) " • Terminé" else " • À faire",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )

                if (act.description.isNotBlank()) {
                    Text(
                        text = act.description,
                        fontSize = 13.sp,
                        color = Color(0xFF7A7A7A)
                    )
                }
            }
        }
    }

}


