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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlanningSportScreen() {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = YearMonth.from(currentDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1)) // Fond gris clair
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
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Lundi, 6 = Dimanche

    val weeks = mutableListOf<List<LocalDate?>>()
    var currentWeek = mutableListOf<LocalDate?>()

    // Ajouter les jours vides du début
    repeat(firstDayOfWeek) {
        currentWeek.add(null)
    }

    // Ajouter tous les jours du mois
    for (day in 1..daysInMonth) {
        currentWeek.add(yearMonth.atDay(day))

        // Si on arrive à dimanche ou fin du mois, on crée une nouvelle semaine
        if (currentWeek.size == 7 || day == daysInMonth) {
            // Compléter la dernière semaine avec des jours vides si nécessaire
            while (currentWeek.size < 7) {
                currentWeek.add(null)
            }
            weeks.add(currentWeek.toList())
            currentWeek = mutableListOf()
        }
    }

    Column(modifier = modifier) {
        // En-têtes des jours de la semaine
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grille des jours
        weeks.forEach { week ->
            CalendarWeekRow(week = week)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CalendarWeekRow(week: List<LocalDate?>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        week.forEachIndexed { index, date ->
            CalendarDayCell(
                date = date,
                isWeekend = index >= 5, // Samedi et Dimanche
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate?,
    isWeekend: Boolean,
    modifier: Modifier = Modifier
) {
    val isToday = date == LocalDate.now()

    Box(
        modifier = modifier
            .padding(4.dp)
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

                // Indicateur d'entraînement (à personnaliser plus tard)
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