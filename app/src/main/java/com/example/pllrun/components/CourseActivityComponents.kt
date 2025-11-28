package com.example.pllrun.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pllrun.Classes.CourseActivite

@Composable
fun CourseActivityForm(
    courseDetails: CourseActivite,
    onCourseDetailsChange: (CourseActivite) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Détails Spécifiques Course",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // --- Vitesses ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = courseDetails.vitesseMoyenne?.toString() ?: "",
                onValueChange = { val v = it.toDoubleOrNull(); onCourseDetailsChange(courseDetails.copy(vitesseMoyenne = v)) },
                label = { Text("Vitesse Moy (km/h)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = courseDetails.vitesseMax?.toString() ?: "",
                onValueChange = { val v = it.toDoubleOrNull(); onCourseDetailsChange(courseDetails.copy(vitesseMax = v)) },
                label = { Text("Vitesse Max (km/h)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        // --- Fréquence Cardiaque ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = courseDetails.bpmMoyen?.toString() ?: "",
                onValueChange = { val v = it.toIntOrNull(); onCourseDetailsChange(courseDetails.copy(bpmMoyen = v)) },
                label = { Text("BPM Moyen") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = courseDetails.bpmMax?.toString() ?: "",
                onValueChange = { val v = it.toIntOrNull(); onCourseDetailsChange(courseDetails.copy(bpmMax = v)) },
                label = { Text("BPM Max") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        // --- Zones de FC (Distance) ---
        Text("Distances par Zone (km)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        ZoneMapEditor(
            dataMap = courseDetails.distanceParZoneFc,
            valueType = "km",
            onMapChange = { newMap -> onCourseDetailsChange(courseDetails.copy(distanceParZoneFc = newMap)) }
        )

        // --- Zones de FC (Temps) ---
        Text("Temps par Zone (minutes)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        ZoneMapEditorTime(
            dataMap = courseDetails.tempsParZoneFc,
            onMapChange = { newMap -> onCourseDetailsChange(courseDetails.copy(tempsParZoneFc = newMap)) }
        )

        // --- Trace GPS ---
        OutlinedTextField(
            value = courseDetails.traceGpsJson ?: "",
            onValueChange = { onCourseDetailsChange(courseDetails.copy(traceGpsJson = if (it.isBlank()) null else it)) },
            label = { Text("Trace GPS (JSON)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )
    }
}

// Helper pour éditer la Map<Int, Double> (Distance)
@Composable
fun ZoneMapEditor(
    dataMap: Map<Int, Double>,
    valueType: String,
    onMapChange: (Map<Int, Double>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // On affiche 5 zones fixes
        (1..5).chunked(3).forEach { rowZones -> // Affiche par rangée de 3 pour gagner de la place
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowZones.forEach { zoneId ->
                    val currentValue = dataMap[zoneId]?.toString() ?: ""
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = {
                            val newVal = it.toDoubleOrNull()
                            val mutableMap = dataMap.toMutableMap()
                            if (newVal != null) mutableMap[zoneId] = newVal else mutableMap.remove(zoneId)
                            onMapChange(mutableMap)
                        },
                        label = { Text("Z$zoneId") },
                        placeholder = { Text(valueType) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                // Remplir l'espace vide si la rangée n'est pas complète
                if (rowZones.size < 3) Spacer(modifier = Modifier.weight((3 - rowZones.size).toFloat()))
            }
        }
    }
}

// Helper pour éditer la Map<Int, Long> (Temps en minutes, stocké en millis ou secondes selon votre logique)
// Ici je pars du principe que l'input est en MINUTES pour l'utilisateur, mais stocké en LONG
@Composable
fun ZoneMapEditorTime(
    dataMap: Map<Int, Long>,
    onMapChange: (Map<Int, Long>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).chunked(3).forEach { rowZones ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowZones.forEach { zoneId ->
                    // On convertit pour l'affichage (ex: millis vers minutes si nécessaire, ici brut pour l'exemple)
                    val currentValue = dataMap[zoneId]?.toString() ?: ""
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = {
                            val newVal = it.toLongOrNull()
                            val mutableMap = dataMap.toMutableMap()
                            if (newVal != null) mutableMap[zoneId] = newVal else mutableMap.remove(zoneId)
                            onMapChange(mutableMap)
                        },
                        label = { Text("Z$zoneId") },
                        placeholder = { Text("min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                if (rowZones.size < 3) Spacer(modifier = Modifier.weight((3 - rowZones.size).toFloat()))
            }
        }
    }
}
