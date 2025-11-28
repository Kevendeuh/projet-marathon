package com.example.pllrun.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.CourseActivite
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.TypeObjectif
import java.time.Duration

@Composable
fun ActivityRow(
    act: Activite,
    onEdit: (Activite) -> Unit,          // ← bouton crayon
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onEdit(act) }),
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
                    text = "${act.heureDeDebut.hour}h${act.heureDeDebut.minute}min • $typeLibelle • $niveauLibelle",
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

            // Icon crayon
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Modifier l’activité"
                )

        }
    }
}
/**
 * Boîte de dialogue modale pour modifier tous les champs d'une activité.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDialog(
    act: Activite,
    initialCourseDetails: CourseActivite? = null,
    onDismiss: () -> Unit,
    isCreationMode: Boolean = false,
    onSave: (Activite, CourseActivite?) -> Unit,
    onDelete: (Activite) -> Unit
) {
    // --- 1. ÉTATS DU FORMULAIRE ---
    // On utilise `remember` avec `act` comme clé pour réinitialiser les états si l'activité change.
    var nom by remember(act) { mutableStateOf(act.nom) }
    var description by remember(act) { mutableStateOf(act.description) }
    var date by remember(act) { mutableStateOf(act.date) }
    var heureDeDebut by remember(act) { mutableStateOf(act.heureDeDebut) }
    var distance by remember(act) { mutableStateOf(act.distanceEffectuee.toString()) }
    var tempsEffectueMinutes by remember(act) { mutableStateOf(act.tempsEffectue.toMinutes().toString()) }
    var estComplete by remember(act) { mutableStateOf(act.estComplete) }
    var niveau by remember(act) { mutableStateOf(act.niveau) }
    var typeActivite by remember(act) { mutableStateOf(act.typeActivite) }

    // État pour la Course
    var courseDetailsState by remember(initialCourseDetails) {
        mutableStateOf(initialCourseDetails)
    }
    // --- 2. ÉTATS POUR LES PICKERS ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // --- DIALOGUE PRINCIPAL ---
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isCreationMode) "Nouvelle Activité" else "Modifier l'Activité",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // --- FORMULAIRE SCROLLABLE ---
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false) // Pour que la colonne ne prenne que la place nécessaire et scrolle
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom de l'activité") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Ligne pour la date et l'heure
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Champ Date

                        ReadOnlyField(
                            value = date.format(dateFormatter),
                            label = "Date",
                            modifier = Modifier.weight(1f),
                            onClick = { showDatePicker = true }
                        )

                        // Champ Heure
                        ReadOnlyField(
                            value = heureDeDebut.format(timeFormatter),
                            label = "Heure",
                            modifier = Modifier.weight(1f),
                            onClick = { showTimePicker = true }

                        )

                    }

                    // Ligne pour la distance et le temps
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = distance,
                            onValueChange = { distance = it },
                            label = { Text("Distance (km)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = tempsEffectueMinutes,
                            onValueChange = { tempsEffectueMinutes = it },
                            label = { Text("Temps (min)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // --- SÉLECTEUR DE TYPE D'ACTIVITÉ (Dropdown) ---
                    var expandedType by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = typeActivite.name, // Affiche le nom de l'enum (ex: COURSE)
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type d'activité") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            // On parcourt toutes les valeurs de l'enum TypeObjectif
                            TypeObjectif.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(text = type.name) },
                                    onClick = {
                                        typeActivite = type
                                        // Si on passe sur AUTRE ou un type sans détails, on peut vouloir nettoyer courseDetailsState
                                        // Mais pour l'instant, le SpecificActivityFormContent gère la création si nécessaire.
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }

// --- SÉLECTEUR DE NIVEAU (Dropdown) ---

                    ExposedDropdownMenuComponent(
                        label = "Niveau",
                        items = NiveauExperience.values().map { it.name },
                        selectedItem = niveau.name,
                        onItemSelected = { selectedString ->
                            niveau = NiveauExperience.valueOf(selectedString)
                        }
                    )

                    // CHAMP VALIDATION
                    ValidationField(
                        isValid = estComplete,
                        onStateChange = { estComplete = it }
                    )
                    SpecificActivityFormContent(
                        type = typeActivite,
                        courseDetails = courseDetailsState,
                        onCourseDetailsChange = { updated -> courseDetailsState = updated }
                    )


                    Spacer(modifier = Modifier.height(24.dp))

                    // --- BOUTONS D'ACTION ---
                    ActionButtons(
                        isCreationMode = isCreationMode,
                        activite = act,
                        onSave = {
                            val updatedActivite = act.copy(
                                nom = nom,
                                description = description,
                                date = date,
                                heureDeDebut = heureDeDebut,
                                distanceEffectuee = distance.toDoubleOrNull()
                                    ?: act.distanceEffectuee,
                                tempsEffectue = Duration.ofMinutes(
                                    tempsEffectueMinutes.toLongOrNull()
                                        ?: act.tempsEffectue.toMinutes()
                                ),
                                estComplete = estComplete,
                                niveau = niveau,
                                typeActivite = typeActivite
                            )
                            val specificCourseData =
                                if (typeActivite == TypeObjectif.COURSE) courseDetailsState else null
                            onSave(updatedActivite, specificCourseData)
                        },
                        onDelete = onDelete,
                        onDismiss = onDismiss
                    )
                }
                }
            }
        }


    // --- PICKERS DE DATE ET HEURE ---
    if (showDatePicker) {
        DatePickerComponent(
            initialDate = date,
            onDateSelected = { newDate ->
                date = newDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { newTime ->
                heureDeDebut = newTime
                showTimePicker = false
            }
        )
    }
}

/**
 * Section des boutons d'action pour le dialogue d'activité.
 */
@Composable
private fun ActionButtons(
    isCreationMode : Boolean=false,
    activite: Activite,
    onSave: () -> Unit,
    onDelete: (Activite) -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- BOUTON SUPPRIMER ---
        Button(
            onClick = { showDeleteConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer l'activité"
                )
                Text("Supprimer l'activité")
            }
        }

        // --- BOUTONS ANNULER ET ENREGISTRER ---
        Row {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("Enregistrer")
            }
        }
    }

    // --- POPUP DE CONFIRMATION DE SUPPRESSION ---
    if (showDeleteConfirmDialog) {
        DeleteActivityConfirmationDialog(
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirmDelete = {
                onDelete(activite)
                showDeleteConfirmDialog = false
            }
        )
    }
}

/**
 * Dialogue simple pour confirmer la suppression d'une activité.
 */
@Composable
private fun DeleteActivityConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Avertissement") },
        title = { Text("Supprimer l'activité ?") },
        text = { Text("Cette action est définitive et ne peut pas être annulée.") },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Ce composable agit comme un "Switch" géant.
 * Il décide quel formulaire spécifique afficher en fonction du TypeObjectif sélectionné.
 */
@Composable
fun SpecificActivityFormContent(
    type: TypeObjectif,
    // On passe tous les états spécifiques potentiels ici (nullable)
    courseDetails: CourseActivite?,
    // Callbacks pour chaque type
    onCourseDetailsChange: (CourseActivite) -> Unit
    // Pour le futur : onNatationDetailsChange, onMuscuDetailsChange...
) {
    when (type) {
        TypeObjectif.COURSE -> {
            // Si l'objet est null (ex: on passe de Vélo à Course), on en crée un vide
            val safeDetails = courseDetails ?: CourseActivite(
                activiteId = 0,
                vitesseMoyenne =10.0,
                vitesseMax = 13.0,
                bpmMoyen = 130,
                bpmMax = 200,
                distanceParZoneFc = emptyMap(),
                tempsParZoneFc = emptyMap(),
                traceGpsJson = null
            )

            // On notifie le parent immédiatement si on a dû créer l'objet par défaut
            // pour éviter les états nulls incohérents
            LaunchedEffect(courseDetails) {
                if (courseDetails == null) onCourseDetailsChange(safeDetails)
            }

            CourseActivityForm(
                courseDetails = safeDetails,
                onCourseDetailsChange = onCourseDetailsChange
            )
        }
        TypeObjectif.AUTRE -> {
            // Si l'objet est null (ex: on passe de Course à Natation), on en crée un vide
        }

        // --- EXTENSIBILITÉ FUTURE ---
        // TypeObjectif.NATATION -> { NatationForm(...) }
        // TypeObjectif.VELO -> { VeloForm(...) }

        else -> {
            // Rien à afficher pour les types génériques
        }
    }
}
