package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.R
import com.example.pllrun.calculator.calculHeureCouche
import com.example.pllrun.calculator.calculTotalCalories
import com.example.pllrun.calculator.calculTotalMinutesSleep
import com.example.pllrun.components.ObjectifEditDialog
import com.example.pllrun.components.ObjectifsListContent
import com.example.pllrun.components.ActivityRow
import com.example.pllrun.components.ActivityDialog
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.ColumnScope

@Composable
fun HubScreen(
    viewModel: InventaireViewModel,
    onEditProfile: () -> Unit,
    onPlanningSport: () -> Unit,
    onAddGoal: () -> Unit,
) {
    var utilisateurPrincipal by remember { mutableStateOf<Utilisateur?>(null) }
    var tempsSommeilSuggere by remember { mutableStateOf<Long?>(null) }
    var heureCoucheSuggeree by remember { mutableStateOf<LocalTime?>(null) }
    var totalCaloriesSuggeree by remember { mutableStateOf<Float?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedObjectifId by remember { mutableStateOf<Long?>(null) }

    // Pop-up activité
    var selectedActivity by remember { mutableStateOf<com.example.pllrun.Classes.Activite?>(null) }

    LaunchedEffect(Unit) {
        val user = viewModel.getAllUtilisateurs().firstOrNull()?.firstOrNull()
        if (user != null) {
            utilisateurPrincipal = user
            tempsSommeilSuggere = calculTotalMinutesSleep(user, viewModel)
            heureCoucheSuggeree = calculHeureCouche(user, viewModel)
            totalCaloriesSuggeree = calculTotalCalories(user, viewModel)
        }
    }

    // Activités du jour
    val today = remember { LocalDate.now() }
    val activitesDuJour by viewModel.getActivitesForDay(today)
        .collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
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
                        color = Color(0xFFFF751F)
                    )
                }
            }

            // Liste scrollable
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Objectifs en cours
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
                                tint = Color(0xFFFF751F)
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

                // Activités du jour
                item {
                    TaskCard(
                        title = "Activités du jour",
                        onThreeDotsClick = onPlanningSport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_flag),
                                contentDescription = "Séances du jour",
                                tint = Color(0xFFFF751F)
                            )
                        }
                    ) {
                        if (activitesDuJour.isEmpty()) {
                            Text(
                                text = "Aucune activité planifiée pour aujourd’hui.",
                                color = Color.Gray
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                activitesDuJour
                                    .sortedBy { it.heureDeDebut?.toString() }
                                    .forEach { act ->
                                        ActivityRow(
                                            act = act,
                                            onClick = { selectedActivity = act },   // tap sur la carte
                                            onEdit = { selectedActivity = it }      // tap sur le crayon
                                        )
                                    }
                            }
                        }
                    }
                }

                // Conseils santé
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
                        val descriptionSommeil =
                            if (tempsSommeilSuggere != null && heureCoucheSuggeree != null)
                                "Temps de sommeil suggéré : $tempsSommeilSuggere min\nHeure de coucher suggérée : $heureCoucheSuggeree"
                            else
                                "Estimation impossible — renseigne ton profil"

                        TaskCard(
                            title = "Sommeil",
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Icône de sommeil",
                                    tint = Color(0xFFFF751F)
                                )
                            },
                            onThreeDotsClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = descriptionSommeil, fontSize = 14.sp, color = Color.Gray)
                        }

                        val descriptionCalories =
                            totalCaloriesSuggeree?.let { "Calories suggérées : $it" }
                                ?: "Estimation impossible — renseigne ton profil"

                        TaskCard(
                            title = "À Manger",
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Icône de nourriture",
                                    tint = Color(0xFFFF751F)
                                )
                            },
                            onThreeDotsClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = descriptionCalories, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Bouton flottant
        Button(
            onClick = onAddGoal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF751F)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "Ajouter Objectif",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    // Pop-up d’édition d’objectif
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

    // Pop-up d’édition d’activité (avec Switch terminé/à faire)
    selectedActivity?.let { act ->
        ActivityDialog(
            act = act,
            onDismiss = { selectedActivity = null },
            onSave = { updated ->
                viewModel.updateActivite(updated)
                viewModel.recalculateObjectifProgress(updated.objectifId)
                selectedActivity = null
            },
            onDelete = { toDelete ->
                viewModel.deleteActivite(toDelete)
                viewModel.recalculateObjectifProgress(toDelete.objectifId)
                selectedActivity = null
            }
        )
    }
}

/** Carte réutilisable */
@Composable
fun TaskCard(
    title: String,
    onThreeDotsClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                icon?.let { it(); Spacer(modifier = Modifier.width(8.dp)) }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            content()
            Spacer(modifier = Modifier.height(16.dp))
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
                    Text("...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }
    }
}
