package com.example.pllrun.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pllrun.Classes.EffortRessenti
import com.example.pllrun.Classes.Muscle
import com.example.pllrun.Classes.MusculationActivite
import com.example.pllrun.Classes.SerieMusculation

@Composable
fun MusculationActivityForm(
    musculationDetails: MusculationActivite,
    onDetailsChange: (MusculationActivite) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // --- 1. MUSCLES CIBLÉS ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Muscles ciblés", style = MaterialTheme.typography.titleSmall)
            MuscleSelector(
                selectedMuscles = musculationDetails.musclesCibles,
                onMusclesChange = { onDetailsChange(musculationDetails.copy(musclesCibles = it)) }
            )
        }

        // --- 2. SÉRIES (REPS + POIDS) ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Séries (Répétitions & Poids)", style = MaterialTheme.typography.titleSmall)
            SeriesRepsEditor(
                seriesList = musculationDetails.series,
                onSeriesChange = { newSeries ->
                    onDetailsChange(musculationDetails.copy(series = newSeries))
                }
            )
        }

        // --- 3. EFFORT RESSENTI (Cercles de couleurs) ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Effort ressenti : ${musculationDetails.effortRessenti.libelle}", style = MaterialTheme.typography.titleSmall)
            EffortRessentiSelector(
                selectedEffort = musculationDetails.effortRessenti,
                onEffortSelected = { onDetailsChange(musculationDetails.copy(effortRessenti = it)) }
            )
        }

    }
}

@Composable
fun EffortRessentiSelector(selectedEffort: EffortRessenti, onEffortSelected: (EffortRessenti) -> Unit) {
    // Mapping des couleurs selon tes spécifications
    val effortColors = mapOf(
        EffortRessenti.ECHAUFFEMENT to Color.White,
        EffortRessenti.FACILE to Color(0xFFFFF59D), // Jaune clair
        EffortRessenti.MODERE to Color(0xFFFFB74D), // Orange
        EffortRessenti.DIFFICILE to Color(0xFFE53935), // Rouge
        EffortRessenti.ECHEC to Color(0xFF212121)   // Noir (ou presque pour le contraste)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EffortRessenti.entries.forEach { effort ->
            val isSelected = effort == selectedEffort
            val color = effortColors[effort] ?: Color.Gray

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 4.dp else 1.dp,
                        color = if (isSelected) Color.Yellow else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onEffortSelected(effort) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MuscleSelector(selectedMuscles: List<Muscle>, onMusclesChange: (List<Muscle>) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Muscles déjà sélectionnés (Bouton Moins)
        selectedMuscles.forEach { muscle ->
            InputChip(
                selected = true,
                onClick = { /* Rien, on utilise l'icone pour retirer */ },
                label = { Text(muscle.libelle) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Retirer",
                        modifier = Modifier.clickable {
                            onMusclesChange(selectedMuscles - muscle)
                        }
                    )
                }
            )
        }

        // Bouton Plus pour ajouter
        Box {
            InputChip(
                selected = false,
                onClick = { expanded = true },
                label = { Text("Ajouter") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Ajouter un muscle") }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                val availableMuscles = Muscle.entries.filter { it !in selectedMuscles }
                if (availableMuscles.isEmpty()) {
                    DropdownMenuItem(text = { Text("Tous les muscles sont sélectionnés") }, onClick = { expanded = false })
                } else {
                    availableMuscles.forEach { muscle ->
                        DropdownMenuItem(
                            text = { Text(muscle.libelle) },
                            onClick = {
                                onMusclesChange(selectedMuscles + muscle)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesRepsEditor(seriesList: List<SerieMusculation>, onSeriesChange: (List<SerieMusculation>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        seriesList.forEachIndexed { index, serie ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("N°${index + 1}", modifier = Modifier.width(36.dp), fontWeight = FontWeight.Bold)

                // Champ Répétitions
                OutlinedTextField(
                    value = if (serie.repetitions == 0) "" else serie.repetitions.toString(),
                    onValueChange = { input ->
                        val newList = seriesList.toMutableList()
                        newList[index] = serie.copy(repetitions = input.toIntOrNull() ?: 0)
                        onSeriesChange(newList)
                    },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Champ Poids
                OutlinedTextField(
                    value = if (serie.poidsKg == 0.0) "" else serie.poidsKg.toString(),
                    onValueChange = { input ->
                        val newList = seriesList.toMutableList()
                        newList[index] = serie.copy(poidsKg = input.toDoubleOrNull() ?: 0.0)
                        onSeriesChange(newList)
                    },
                    label = { Text("Kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Bouton Supprimer
                IconButton(onClick = {
                    val newList = seriesList.toMutableList()
                    newList.removeAt(index)
                    onSeriesChange(newList)
                }) {
                    Icon(Icons.Default.Remove, "Retirer la série", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Bouton Ajouter
        TextButton(onClick = {
            // Ajoute une série vide par défaut (0 reps, 0 kg)
            // Ou on pourrait copier le poids de la série précédente pour aller plus vite !
            val lastWeight = seriesList.lastOrNull()?.poidsKg ?: 0.0
            onSeriesChange(seriesList + SerieMusculation(0, lastWeight))
        }) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter une série")
            Spacer(Modifier.width(4.dp))
            Text("Ajouter une série")
        }
    }
}