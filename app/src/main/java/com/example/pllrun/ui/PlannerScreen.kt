package com.example.pllrun.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pllrun.Classes.DailyNutrition
import com.example.pllrun.Classes.DailySleep
import com.example.pllrun.Classes.FullPlan
import com.example.pllrun.Classes.TrainingDay
import com.example.pllrun.Classes.TrainingIntensity
import com.example.pllrun.PlannerUiState
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    stateFlow: StateFlow<PlannerUiState>,   // <-- on accepte un StateFlow
    onGenerateDefault: () -> Unit,
    onReset: () -> Unit
) {
    val state by stateFlow.collectAsState(initial = PlannerUiState.Idle) // <-- on collecte ici

    Scaffold(
        topBar = { TopAppBar(title = { Text("Marathon Planner") }) }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGenerateDefault) { Text("Générer (défaut)") }
                OutlinedButton(onClick = onReset) { Text("Réinitialiser") }
            }

            when (val s = state) {
                is PlannerUiState.Idle    -> Text("Prêt à générer un plan.")
                is PlannerUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is PlannerUiState.Error   -> Text("Erreur : ${s.message}", color = MaterialTheme.colorScheme.error)
                is PlannerUiState.Ready -> PlanDailyList(s.plan)
            }
        }
    }
}

@Composable
private fun PlanList(plan: FullPlan) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(plan.training.weeks) { w ->
            ElevatedCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Semaine ${w.weekIndex} — ${w.phase}", style = MaterialTheme.typography.titleMedium)
                    Text("Sommeil recommandé ~8h (+0.25–0.5h en BUILD/PEAK)")
                    Spacer(Modifier.height(4.dp))
                    w.days.sortedBy { it.date }.forEach { DayRow(it) }
                }
            }
        }
    }
}

@Composable
private fun DayRow(d: TrainingDay) {
    val subtitle = when (d.workout.kind.name) {
        "LONG" -> "Sortie longue • ${d.workout.minutes}’"
        "TEMPO" -> "Tempo • ${d.workout.minutes}’"
        "INTERVALS" -> "Intervalles • ${d.workout.minutes}’"
        "EASY" -> "Footing facile • ${d.workout.minutes}’"
        "RECOVERY" -> "Recovery • ${d.workout.minutes}’"
        "CROSS" -> "Cross-training • ${d.workout.minutes}’"
        else -> "Repos"
    }
    Column(Modifier.padding(vertical = 4.dp)) {
        Text("${d.date.dayOfWeek}  ${d.date}", style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
        if (d.workout.notes.isNotBlank()) Text(d.workout.notes, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PlanDailyList(plan: FullPlan) {
    // Indexer nutrition/sommeil par date pour accès O(1)
    val byDateNutrition = remember(plan) { plan.nutrition.associateBy { it.date } }
    val byDateSleep = remember(plan) { plan.sleep.associateBy { it.date } }
    // Aplatir toutes les semaines -> jours, triés par date
    val allDays = remember(plan) {
        plan.training.weeks.flatMap { it.days }.sortedBy { it.date }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items = allDays, key = { it.date.toEpochDay() }) { day ->
            val nut = byDateNutrition[day.date]
            val slp = byDateSleep[day.date]
            DayCard(day = day, nutrition = nut, sleep = slp)
        }
    }
}

@Composable
private fun DayCard(
    day: TrainingDay,
    nutrition: DailyNutrition?,
    sleep: DailySleep?
) {
    ElevatedCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatDate(day.date), style = MaterialTheme.typography.titleMedium)

            // Entraînement
            val subtitle = when (day.workout.kind) {
                TrainingIntensity.LONG      -> "Sortie longue • ${day.workout.minutes}’"
                TrainingIntensity.TEMPO     -> "Tempo • ${day.workout.minutes}’"
                TrainingIntensity.INTERVALS -> "Intervalles • ${day.workout.minutes}’"
                TrainingIntensity.EASY      -> "Footing facile • ${day.workout.minutes}’"
                TrainingIntensity.RECOVERY  -> "Recovery • ${day.workout.minutes}’"
                TrainingIntensity.CROSS     -> "Cross-training • ${day.workout.minutes}’"
                TrainingIntensity.REST      -> "Repos"
            }
            Text("Entraînement : $subtitle")
            if (day.workout.notes.isNotBlank()) {
                Text(day.workout.notes, style = MaterialTheme.typography.labelSmall)
            }

            // Nutrition (si dispo)
            nutrition?.let { n ->
                Spacer(Modifier.height(4.dp))
                Text("Nutrition", style = MaterialTheme.typography.titleSmall)
                Text("• ${n.targets.kcal} kcal  |  P ${n.targets.proteinGrams} g  •  G ${n.targets.carbsGrams} g  •  L ${n.targets.fatsGrams} g")
                if (n.targets.notes.isNotBlank()) {
                    Text(n.targets.notes, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Sommeil (si dispo)
            sleep?.let { s ->
                Spacer(Modifier.height(4.dp))
                Text("Sommeil", style = MaterialTheme.typography.titleSmall)
                Text("• Min ${"%.1f".format(s.targets.minHours)} h  |  Reco ${"%.1f".format(s.targets.recommendedHours)} h")
                if (s.targets.notes.isNotBlank()) {
                    Text(s.targets.notes, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private val FR = Locale("fr", "FR")
private val DATE_FMT_FR = DateTimeFormatter.ofPattern("EEEE d MMM yyyy", FR)

private fun formatDate(d: LocalDate): String {
    val s = DATE_FMT_FR.format(d)
    return s.replaceFirstChar { if (it.isLowerCase()) it.titlecase(FR) else it.toString() }
}
