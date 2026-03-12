package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.R
import com.example.pllrun.components.ObjectifEditDialog
import com.example.pllrun.components.ObjectifsListContent
import java.time.LocalTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.runtime.livedata.observeAsState
import java.time.format.DateTimeFormatter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
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
    onNavigateToRecettes: () -> Unit,
) {
    // --- GESTION DES ÉTATS DE L'UI POUR LES DIALOGUES ---
    var objectifToEditId by remember { mutableStateOf<Long?>(null) }
    var activiteToEdit by remember { mutableStateOf<Activite?>(null) } // État pour le dialogue d'activité
    var showAddActivityDialog by remember { mutableStateOf(false) }

    val utilisateurPrincipal by viewModel.getFirstUtilisateur().observeAsState(initial = null)
    val activitesDuJour by viewModel.getActivitesForDay(LocalDate.now()).observeAsState(initial = emptyList())

    // IA nutrition
    val suggestionRepas by viewModel.suggestionRepas.collectAsState()
    val isLoadingSuggestion by viewModel.isLoadingSuggestion.collectAsState()

    val sleepMinutes by viewModel.getRecommendedSleepTime(utilisateurPrincipal?.id ?: -1).observeAsState(0L)
    val bedtime by viewModel.getRecommendedBedtime(utilisateurPrincipal?.id ?: -1).observeAsState(LocalTime.of(22, 0))
    val nutriments by viewModel.getRecommendedNutriments(utilisateurPrincipal?.id ?: -1).observeAsState(ApportsNutritionnels(0F,0F,0F,0F))
    val bpmHistory by viewModel.bpmHistory.collectAsState(initial = emptyList())

    // --- FORMATAGE DES DONNÉES ---
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
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            // --- CONTENU SCROLLABLE ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
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
                                tint = MaterialTheme.colorScheme.primaryContainer
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
                        var descriptionSommeil = "estimation impossible veuillez renseigner utilisateur"
                        if (tempsSommeilSuggere != null) {
                            descriptionSommeil = " temps de sommeil suggéré:$tempsSommeilSuggere \n heure de couche suggérée:$heureCoucheSuggeree"
                        }
                        TaskCard(
                            title = "Sommeil",
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Icône de sommeil",
                                    tint = MaterialTheme.colorScheme.primaryContainer
                                )
                            },
                            onThreeDotsClick = { /* TODO: Naviguer vers écran sommeil */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = descriptionSommeil,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TaskCard(
                            title = "À Manger",
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Icône de nourriture",
                                    tint = MaterialTheme.colorScheme.primaryContainer
                                )
                            },
                            onThreeDotsClick = onNavigateToRecettes,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (utilisateurPrincipal != null && nutriments.calories > 0) {
                                    Column {
                                        Text("${nutriments.calories.toInt()} kcal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            "P: ${nutriments.proteines.toInt()}g | G: ${nutriments.glucides.toInt()}g | L: ${nutriments.lipides.toInt()}g",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp
                                        )
                                    }
                                } else {
                                    Text("N/A", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                utilisateurPrincipal?.let { user ->
                                    Button(
                                        onClick = { viewModel.genererSuggestionRepas(user, viewModel) },
                                        enabled = !isLoadingSuggestion,
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Idée repas", fontSize = 12.sp)
                                    }
                                }

                                if (isLoadingSuggestion) {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                } else if (!suggestionRepas.isNullOrBlank()) {
                                    Text(
                                        text = suggestionRepas!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.saveSuggestionAsRecette(suggestionRepas!!) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Sauvegarder", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    TaskCard(
                        title = "Graphe fréquence cardiaque",
                        onThreeDotsClick = {},
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = "Icône de nourriture",
                                tint = Color(0xFFFF751F)
                            )
                        }
                    ){
                        HeartRateGraphContent(data = bpmHistory)
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }

        // --- 2. BOUTONS FLOTTANTS (COLUMN EN BAS) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showAddActivityDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Ajouter Activité", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAddGoal,
                modifier = Modifier.fillMaxWidth().height(56.dp),
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
        // On écoute en temps réel les détails de course ET de musculation
        val courseDetails by viewModel.getCourseActiviteByActiviteIdFlow(activite.id).collectAsState(initial = null)
        val musculationDetails by viewModel.getMusculationActiviteByActiviteIdFlow(activite.id).collectAsState(initial = null)

        ActivityDialog(
            viewModel = viewModel,
            act = activite,
            initialCourseDetails = courseDetails,
            initialMusculationDetails = musculationDetails, // <-- AJOUT ICI
            isCreationMode = false,
            onDismiss = { activiteToEdit = null },
            onDelete = { toDelete ->
                viewModel.deleteActivite(toDelete)
                activiteToEdit = null
            },
            onSave = { activiteMaj, detailsCourseMaj, detailsMuscuMaj -> // <-- MISE A JOUR SIGNATURE
                viewModel.updateActivite(activiteMaj)

                // Mise à jour de la course
                if (detailsCourseMaj != null) {
                    if (detailsCourseMaj.id != 0L) {
                        viewModel.updateCourseActivite(detailsCourseMaj)
                    } else {
                        viewModel.insertCourseActivite(detailsCourseMaj.copy(activiteId = activiteMaj.id))
                    }
                }

                // Mise à jour de la musculation
                if (detailsMuscuMaj != null) {
                    if (detailsMuscuMaj.id != 0L) {
                        viewModel.updateMusculationActivite(detailsMuscuMaj)
                    } else {
                        viewModel.insertMusculationActivite(detailsMuscuMaj.copy(activiteId = activiteMaj.id))
                    }
                }

                activiteToEdit = null
            }
        )
    }

    // 3. Dialogue de CRÉATION d'activité
    if (showAddActivityDialog) {
        val newActivity = Activite(
            id = 0,
            objectifId = null,
            date = LocalDate.now(),
            heureDeDebut = LocalTime.now(),
            typeActivite = TypeObjectif.COURSE,
            estComplete = false,
            nom = "",
            description = TypeObjectif.COURSE.description,
            tempsEffectue = Duration.ofMinutes(30),
            niveau = NiveauExperience.DEBUTANT
        )

        ActivityDialog(
            act = newActivity,
            viewModel = viewModel,
            onDismiss = { showAddActivityDialog = false },
            onSave = { activiteCreee, detailsCourse, detailsMuscu -> // <-- MISE A JOUR SIGNATURE
                // Le ViewModel gère l'insertion avec transaction selon le type de données retourné
                if (detailsCourse != null) {
                    viewModel.addNewActiviteWithCourseDetails(activiteCreee, detailsCourse)
                } else if (detailsMuscu != null) {
                    viewModel.addNewActiviteWithMusculationDetails(activiteCreee, detailsMuscu)
                } else {
                    viewModel.addNewActivite(activiteCreee)
                }
                showAddActivityDialog = false
            },
            onDelete = { }
        )
    }
}

// Composant réutilisable pour les cartes de tâches
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
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
                icon?.let {
                    it()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                val sortedData = data.sortedBy { it.timestamp }
                modelProducer.runTransaction {
                    columnSeries {
                        series(
                            x = sortedData.map { measurement ->
                                val instant = java.time.Instant.ofEpochMilli(measurement.timestamp)
                                val zdt = java.time.ZonedDateTime.ofInstant(
                                    instant,
                                    java.time.ZoneId.systemDefault()
                                )
                                val decimalHour = zdt.hour + (zdt.minute / 60.0)
                                (decimalHour * 100).toInt() / 100.0
                            },
                            y = data.map { it.bpm },
                        )
                    }
                }
            }
        }
    }

    if (data.isNotEmpty()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            thickness = 8.dp,
                            shape = com.patrykandpatrick.vico.core.common.shape.Shape.Rectangle
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, value, _ -> "${value.toInt()}h" },
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned(spacing = { 4 }, addExtremeLabelPadding = true)
                    }
                ),
            ),
            modelProducer = modelProducer,
            modifier = modifier,
            zoomState = rememberVicoZoomState(zoomEnabled = false)
        )
    } else {
        Text("Pas de données")
    }
}