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
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.HeartRateMeasurement
import com.example.pllrun.Classes.TypeObjectif
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.calculator.ApportsNutritionnels
import com.example.pllrun.components.ActivityDialog
import com.example.pllrun.components.ActivityRow

import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.ColumnProvider.Companion.series
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate

@Composable
fun HubScreen(
    viewModel: InventaireViewModel,
    onEditProfile: () -> Unit,
    onPlanningSport: () -> Unit,
    onAddGoal: () -> Unit,
) {
    // --- GESTION DES ÉTATS DE L'UI POUR LES DIALOGUES ---
    var objectifToEditId by remember { mutableStateOf<Long?>(null) }
    var activiteToEdit by remember { mutableStateOf<Activite?>(null) } // État pour le dialogue d'activité
    // AJOUT : État pour afficher la popup de création d'activite
    var showAddActivityDialog by remember { mutableStateOf(false) }

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
            .background(MaterialTheme.colorScheme.background)
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onEditProfile) {
                    Text(
                        text = "Modifier profil",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primaryContainer// Orange
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
                                tint = MaterialTheme.colorScheme.primaryContainer// Couleur orange
                            )
                        },
                    ) {
                        utilisateurPrincipal?.let { user ->
                            ObjectifsListContent(
                                viewModel = viewModel,
                                utilisateurId = user.id,
                                onObjectifClick = { objectifId ->
                                    objectifToEditId = objectifId
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
                            Text("Aucune activité planifiée pour aujourd’hui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                activitesDuJour.sortedBy { it.heureDeDebut }.forEach { act ->
                                    ActivityRow(
                                        act = act,
                                        onEdit = { activiteSelectionnee ->
                                            activiteToEdit = activiteSelectionnee
                                        }
                                    )
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                                    tint = MaterialTheme.colorScheme.primaryContainer// Couleur orange
                                )
                            },
                            onThreeDotsClick = { /* TODO: Naviguer vers écran sommeil */ },
                            modifier = Modifier.weight(1f)
                        )
                        {
                            Text(
                                text = descriptionSommeil,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text("N/A", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // --- 2. BOUTONS FLOTTANTS (COLUMN EN BAS) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espace entre les boutons
        ) {
            // BOUTON 1 : Ajouter Activité
            Button(
                onClick = { showAddActivityDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, // Couleur différente (ex: secondaire)
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Ajouter Activité",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // BOUTON 2 : Ajouter Objectif
            Button(
                onClick = onAddGoal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Ajouter Objectif",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

    }


    // --- AFFICHAGE CONDITIONNEL DES DIALOGUES ---

    // 1. Dialogue d'édition d'objectif
    objectifToEditId?.let { id ->
        ObjectifEditDialog(
            viewModel = viewModel,
            objectifId = id,
            onDismiss = { objectifToEditId = null }
        )
    }

    // 2. Dialogue d'édition d'activité
    activiteToEdit?.let { activite ->
        // Astuce : Chargez les détails spécifiques AVANT d'ouvrir le dialogue ou observez-les
        // Pour simplifier ici, imaginons que vous les ayez récupérés via le ViewModel
        val courseDetails by viewModel.getCourseActiviteByActiviteIdFlow(activite.id).collectAsState(initial = null)

        ActivityDialog(
            act = activite,
            initialCourseDetails = courseDetails, // On passe les données existantes
            isCreationMode = false,
            onDismiss = { activiteToEdit = null },
            onDelete = { toDelete ->
                viewModel.deleteActivite(toDelete)
                activiteToEdit = null
            },
            onSave = { activiteMaj, detailsCourseMaj ->
                viewModel.updateActivite(activiteMaj)

                if (detailsCourseMaj != null) {
                    // Si l'ID existe déjà, update, sinon insert
                    if (detailsCourseMaj.id != 0L) {
                        viewModel.updateCourseActivite(detailsCourseMaj)
                    } else {
                        // Cas rare où on ajoute des détails à une activité qui n'en avait pas
                        viewModel.insertCourseActivite(detailsCourseMaj.copy(activiteId = activiteMaj.id))
                    }
                }
                activiteToEdit = null
            }
        )
    }


    // 3. AJOUT : Dialogue de CRÉATION d'activité
    if (showAddActivityDialog) {
        // On crée une activité vide par défaut (ID = 0 pour que Room sache qu'il faut insérer)
        // Vérifiez que ce constructeur correspond bien à votre Data Class Activite
        val newActivity = Activite(
            id = 0,
            objectifId = null, // Pas d'objectif lié par défaut
            date = LocalDate.now(),
            heureDeDebut = LocalTime.now(),
            typeActivite = TypeObjectif.COURSE, // Type par défaut (à adapter selon votre Enum)
            estComplete = false,
            nom = "",
            description = TypeObjectif.COURSE.description,
            distanceEffectuee =10.0,
            tempsEffectue =  Duration.ofMinutes(30),
            niveau = NiveauExperience.DEBUTANT
        )

        ActivityDialog(
            act = newActivity,
            onDismiss = { showAddActivityDialog = false },
            onSave = { activiteCreee, detailsCourse ->
                // C'est ici que la magie opère : le ViewModel gère l'insertion intelligente
                if (detailsCourse != null) {
                    // Transaction Activité + Course
                    viewModel.addNewActiviteWithCourseDetails(activiteCreee, detailsCourse)
                } else {
                    // Activité simple
                    viewModel.addNewActivite(activiteCreee)
                }
                showAddActivityDialog = false
            },
            // Pas de suppression possible lors de la création
            onDelete = { }
        )
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
            containerColor =MaterialTheme.colorScheme.surfaceVariant
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
                    color = MaterialTheme.colorScheme.onBackground
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
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


}

@Composable
fun HeartRateGraphContent(
    data: List<HeartRateMeasurement>,
    modifier: Modifier = Modifier
) {
    // 1. Créer un "Producer" pour les données de Vico
    val modelProducer = remember { CartesianChartModelProducer() }

    // 2. Charger les données de manière asynchrone
    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                // On mappe vos objets HeartRateMeasurement vers des séries de colonnes
                // x = timestamp (ou index), y = bpm
                columnSeries {
                    series(
                        x = data.map { it.timestamp.toDouble() }, // Ou utilisez un index simple 0..n
                        y = data.map { it.bpm },
                    )
                }
            }
        }
    }

    // 3. Afficher le graphique
    if (data.isNotEmpty()) {
        CartesianChartHost(
            chart = rememberCartesianChart(

                // Axes (Optionnel, vous pouvez les retirer si vous voulez juste les barres)
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
            modelProducer = modelProducer,
            modifier = modifier,
            // Active le scroll horizontal et le zoom
            zoomState = rememberVicoZoomState(zoomEnabled = false)
        )
    } else {
        // Gestion du cas vide
        Text("Pas de données")
    }
}


