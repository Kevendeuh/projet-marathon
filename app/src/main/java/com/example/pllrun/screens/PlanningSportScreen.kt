package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.window.Dialog
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.components.ActivityDialog
import com.example.pllrun.components.ActivityRow
import com.example.pllrun.components.ObjectifCard
import com.example.pllrun.components.ObjectifEditDialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.lazy.items as lazyListItems
import java.time.format.DateTimeFormatter

@Composable
fun PlanningSportScreen(viewModel: InventaireViewModel,
                        utilisateurId: Long) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = YearMonth.from(currentDate)

    // --- Observer les données du ViewModel ---
    val objectifs by viewModel.getObjectifsForUtilisateurAsLiveData(utilisateurId).observeAsState(initial = emptyList())
    val activites by viewModel.getAllActivites().observeAsState(initial = emptyList())

    // --- GESTION DES ÉTATS POUR LES POPUPS ---
    var selectedDateForPopup by remember { mutableStateOf<LocalDate?>(null) }
    var objectifToEditId by remember { mutableStateOf<Long?>(null) }
    var activiteToEdit by remember { mutableStateOf<Activite?>(null) } // État pour le dialogue d'activité


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1)) // Fond gris clair
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        // Header avec titre et navigation du mois
        MonthHeader(
            currentMonth = currentMonth,
            onPreviousMonth = {
                currentDate = currentDate.minusMonths(1)
            },
            onNextMonth = {
                currentDate = currentDate.plusMonths(1)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calendrier
        CalendarGrid(
            yearMonth = currentMonth,
            modifier = Modifier.fillMaxWidth(),
            objectifs = objectifs,
            activites = activites,
            onDateClick = { selectedDate ->
                selectedDateForPopup = selectedDate
            }

        )
    }
    // --- GESTION DE L'AFFICHAGE DES POPUPS ---

    // 1. Popup de détails du jour
    selectedDateForPopup?.let { date ->
        DayDetailsPopup(
            date = date,
            objectifs = objectifs,
            activites = activites,
            onDismiss = { selectedDateForPopup = null },
            onObjectifClick = { objectifId ->
                // Quand on clique sur un objectif dans la popup...
                objectifToEditId = objectifId   // ...et on prépare l'ouverture de celle d'édition.
            },
            onActiviteClick = { activiteSelectionnee ->
                // Quand on clique sur une activité dans la popup...
                activiteToEdit = activiteSelectionnee // ...et on prépare l'ouverture du dialogue d'édition d'activité.
            }
        )
    }

    // 2. Dialogue d'édition d'objectif
    objectifToEditId?.let { id ->
        ObjectifEditDialog(
            viewModel = viewModel,
            objectifId = id,
            onDismiss = { objectifToEditId = null }
        )
    }

    // 3. Dialogue d'édition d'activité
    activiteToEdit?.let { activite ->
        ActivityDialog(
            act = activite,
            onDismiss = {
                activiteToEdit = null // Ferme le dialogue
            },
            onSave = { activiteMiseAJour ->
                viewModel.updateActivite(activiteMiseAJour)
                activiteToEdit = null // Ferme le dialogue
            },
            onDelete = { activiteASupprimer ->
                viewModel.deleteActivite(activiteASupprimer)
                activiteToEdit = null // Ferme le dialogue
            }
        )
    }
}

@Composable
fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flèche gauche
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_previous),
                contentDescription = "Mois précédent",
                modifier = Modifier.size(24.dp)
            )
        }

        // Titre du mois et année
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Planning Sport",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + currentMonth.year,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }

        // Flèche droite
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_next),
                contentDescription = "Mois suivant",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    modifier: Modifier = Modifier,
    objectifs: List<Objectif>,
    activites: List<Activite>,
    onDateClick: (LocalDate?) -> Unit,

    ) {
    // ---- ÉTAPE 1 : Créer une liste plate de tous les jours à afficher ----
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    // Lundi = 0, Mardi = 1, ..., Dimanche = 6
    val firstDayOfWeekIndex = (firstDayOfMonth.dayOfWeek.value - 1).coerceAtLeast(0)

    val calendarDays = mutableListOf<LocalDate?>()

    // Ajouter les jours vides du début (null)
    repeat(firstDayOfWeekIndex) {
        calendarDays.add(null)
    }

    // Ajouter tous les jours du mois
    for (day in 1..daysInMonth) {
        calendarDays.add(yearMonth.atDay(day))
    }

    // (Optionnel mais recommandé) Ajouter des jours vides à la fin pour compléter la grille
    while (calendarDays.size % 7 != 0) {
        calendarDays.add(null)
    }

    // ---- ÉTAPE 2 : Utiliser LazyVerticalGrid ----
    Column(modifier = modifier) {
        // En-têtes des jours de la semaine (L, M, M, J, V, S, D)
        DayOfWeekHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // Grille du calendrier
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            // On peut retirer le verticalArrangement si les spacers sont gérés dans les cellules
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays) { date ->

                // Pour chaque date, on vérifie si elle est dans un objectif ou si elle a des activités.
                val isInObjectif = date != null && objectifs.any { !date.isBefore(it.dateDeDebut) && !date.isAfter(it.dateDeFin) }
                val hasActivites = date != null && activites.any { it.date == date }

                CalendarDayCell(
                    date = date,
                    // La vérification du week-end est plus simple
                    isWeekend = date?.dayOfWeek?.value in 6..7,
                    isInObjectif = isInObjectif,
                    hasActivites = hasActivites,
                    onClick = { onDateClick(date) }
                )
            }
        }
    }
}

@Composable
fun DayOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
            Text(
                text = day,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate?,
    isWeekend: Boolean,
    isInObjectif: Boolean,
    hasActivites: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Le Modifier est maintenant appliqué par la LazyGrid
) {
    val isToday = date == LocalDate.now()
    // --- LOGIQUE DE COULEUR DE FOND MISE À JOUR ---
    // On définit la couleur de fond en fonction de la priorité :
    // 1. Aujourd'hui (orange)
    // 2. Dans un objectif (gris léger)
    // 3. Par défaut (blanc)
    val backgroundColor = when {
        isToday -> Color(0xFFFF751F)
        isInObjectif -> Color.Gray.copy(alpha = 0.5f)
        else -> Color.White
    }
    Box(
        modifier = modifier
            .height(80.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isWeekend) Color(0xFFFFE0B2) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
    ) {
        if (date != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize() // La colonne prend toute la place de la Box parente
                    .padding(vertical = 8.dp, horizontal = 4.dp), // Marges en haut/bas
                horizontalAlignment = Alignment.CenterHorizontally // Centre tous les enfants horizontalement
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isToday) Color.White else Color.Black,

                )

                Spacer(modifier = Modifier.weight(1f))

                // On affiche le point seulement s'il y a des activités pour ce jour.
                Box(modifier = Modifier.size(10.dp)) {
                    if (hasActivites) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = if (isToday) Color.White else Color(0xFFFF751F),
                                    shape = CircleShape
                                )
                        )
                    }
                    // Si pas d'activité, le Box de 10.dp est vide mais il occupe l'espace,
                    // garantissant un alignement parfait du numéro en haut.
                }
            }
        }
    }
}

// --- COMPOSABLE POUR LA POPUP ---
@Composable
fun DayDetailsPopup(
    date: LocalDate,
    objectifs: List<Objectif>,
    activites: List<Activite>,
    onDismiss: () -> Unit,
    onObjectifClick: (Long) -> Unit,
    onActiviteClick: (Activite) -> Unit
) {
    // On filtre les listes pour ne garder que les données pertinentes pour la date sélectionnée
    val objectifsDuJour = remember(date, objectifs) {
        objectifs.filter { !date.isBefore(it.dateDeDebut) && !date.isAfter(it.dateDeFin) }
    }
    val activitesDuJour = remember(date, activites) {
        activites.filter { it.date == date }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("eeee dd MMMM", Locale.FRENCH) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp), // Hauteur max pour éviter de remplir tout l'écran
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))
        ) {
            Column {
                // En-tête de la popup
                Text(
                    text = date.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(24.dp)
                )

                // Liste scrollable pour le contenu
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section Objectifs
                    if (objectifsDuJour.isNotEmpty()) {
                        item {
                            Text("Objectifs en cours ce jour-là", style = MaterialTheme.typography.titleMedium)
                        }
                        lazyListItems(objectifsDuJour, key = { it.id }) { objectif ->
                            // On réutilise ObjectifCard de ObjectifsComponent.kt
                            ObjectifCard(
                                objectif = objectif,
                                onClick = { onObjectifClick(objectif.id) }
                            )
                        }
                    }

                    // Section Activités
                    if (activitesDuJour.isNotEmpty()) {
                        item {
                            Text("Activités prévues", style = MaterialTheme.typography.titleMedium)
                        }
                        lazyListItems(activitesDuJour, key = { it.id }) { activite ->
                            // On réutilise ActivityRow de Activity.kt
                            ActivityRow(
                                act = activite,
                                onEdit = { onActiviteClick(activite) }
                            )
                        }
                    }

                    // Message si la journée est vide
                    if (objectifsDuJour.isEmpty() && activitesDuJour.isEmpty()) {
                        item {
                            Text(
                                "Rien de prévu pour ce jour.",
                                color = Color.Gray,
                                modifier = Modifier
                                    .padding(vertical = 32.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Bouton pour fermer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

