package com.example.pllrun.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pllrun.Classes.Activite

@Composable
fun ActivityRow(
    act: Activite,
    onEdit: (Activite) -> Unit,          // ← bouton crayon
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Titre + infos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = act.nom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val typeLibelle = runCatching { act.typeActivite.libelle }.getOrElse { act.typeActivite.name }
                val niveauLibelle = runCatching { act.niveau.libelle }.getOrElse { act.niveau.name }

                Text(
                    text = "${act.heureDeDebut} • $typeLibelle • $niveauLibelle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (act.description.isNotBlank()) {
                    Text(
                        text = act.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Statut léger
                Text(
                    text = if (act.estComplete) "Terminé" else "À faire",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (act.estComplete) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bouton crayon (édition)
            IconButton(onClick = { onEdit(act) }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Modifier l’activité"
                )
            }
        }
    }
}

@Composable
fun ActivityDialog(
    act: Activite,
    onDismiss: () -> Unit,
    onSave: (Activite) -> Unit,
    onDelete: (Activite) -> Unit = {}
) {
    var nom by remember { mutableStateOf(act.nom) }
    var description by remember { mutableStateOf(act.description) }
    var distanceTxt by remember { mutableStateOf(act.distanceEffectuee.toString()) }
    var dureeMinTxt by remember { mutableStateOf(act.tempsEffectue.toMinutes().toString()) }
    var estComplete by remember { mutableStateOf(act.estComplete) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val distance = distanceTxt.toDoubleOrNull() ?: act.distanceEffectuee
                val minutes = dureeMinTxt.toLongOrNull() ?: act.tempsEffectue.toMinutes()
                onSave(
                    act.copy(
                        nom = nom,
                        description = description,
                        distanceEffectuee = distance,
                        tempsEffectue = java.time.Duration.ofMinutes(minutes),
                        estComplete = estComplete
                    )
                )
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Annuler") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onDelete(act) }) { Text("Supprimer") }
            }
        },
        title = { Text("Détail de l’activité") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Date : ${act.date}")
                act.heureDeDebut?.let { Text("Heure : $it") }

                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 4
                )
                OutlinedTextField(
                    value = distanceTxt, onValueChange = { distanceTxt = it },
                    label = { Text("Distance (km)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = dureeMinTxt, onValueChange = { dureeMinTxt = it },
                    label = { Text("Durée (min)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                // Switch pour terminé / à faire
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marquer comme terminé")
                    Switch(checked = estComplete, onCheckedChange = { estComplete = it })
                }
            }
        }
    )
}
