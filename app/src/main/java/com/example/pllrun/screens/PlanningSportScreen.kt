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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.pllrun.InventaireViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlanningSportScreen(viewModel: InventaireViewModel,
                        utilisateurId: Long) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = YearMonth.from(currentDate)

    // --- Observer les données du ViewModel ---
    val objectifs by viewModel.getObjectifsForUtilisateur(utilisateurId).observeAsState(initial = emptyList())
    val activites by viewModel.getActivitesForObjectif(utilisateurId).observeAsState(initial = emptyList())

    // État pour gérer l'affichage de la popup
    var selectedDateForPopup by remember { mutableStateOf<LocalDate?>(null) }


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
            modifier = Modifier.fillMaxWidth()
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
    modifier: Modifier = Modifier
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
                CalendarDayCell(
                    date = date,
                    // La vérification du week-end est plus simple
                    isWeekend = date?.dayOfWeek?.value in 6..7
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
    modifier: Modifier = Modifier // Le Modifier est maintenant appliqué par la LazyGrid
) {
    val isToday = date == LocalDate.now()

    // La Box n'a plus besoin du .weight(1f) ni du .padding(4.dp) car c'est géré par la LazyGrid
    Box(
        modifier = modifier
            .height(60.dp)
            .background(
                color = if (isToday) Color(0xFFFF751F) else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isWeekend) Color(0xFFFFE0B2) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = date != null) {
                // TODO: Ouvrir les détails de l'entraînement pour ce jour
            },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isToday) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp)) // Ajoute un peu d'espace

                // Indicateur d'entraînement
                if (hasTraining(date)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (isToday) Color.White else Color(0xFFFF751F),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}


// Fonction temporaire pour indiquer les jours avec entraînement
fun hasTraining(date: LocalDate): Boolean {
    // TODO: Remplacer par la logique réelle de génération d'entraînement
    // Pour l'instant, on met des entraînements les lundis, mercredis, vendredis
    return date.dayOfWeek.value % 2 == 1 // Lundi=1, Mercredi=3, Vendredi=5
}