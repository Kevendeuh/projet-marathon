package com.example.pllrun.components

import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import kotlin.let
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
import com.example.pllrun.Classes.EffortRessenti
import com.example.pllrun.Classes.MusculationActivite
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.TypeObjectif
import com.example.pllrun.InventaireViewModel
import java.time.Duration
import kotlin.collections.emptyList
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import kotlinx.coroutines.coroutineScope


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
    viewModel: InventaireViewModel? = null,
    initialCourseDetails: CourseActivite? = null,
    initialMusculationDetails: MusculationActivite? = null,
    onDismiss: () -> Unit,
    isCreationMode: Boolean = false,
    onSave: (Activite, CourseActivite?, MusculationActivite?) -> Unit,
    onDelete: (Activite) -> Unit
) {
    // --- 1. ÉTATS DU FORMULAIRE ---
    var nom by remember(act) { mutableStateOf(act.nom) }
    var description by remember(act) { mutableStateOf(act.description) }
    var date by remember(act) { mutableStateOf(act.date) }
    var heureDeDebut by remember(act) { mutableStateOf(act.heureDeDebut) }
    var tempsEffectueMinutes by remember(act) { mutableStateOf(act.tempsEffectue.toMinutes().toString()) }
    var estComplete by remember(act) { mutableStateOf(act.estComplete) }
    var niveau by remember(act) { mutableStateOf(act.niveau) }
    var typeActivite by remember(act) { mutableStateOf(act.typeActivite) }

    var courseDetailsState by remember(initialCourseDetails) { mutableStateOf(initialCourseDetails) }
    var musculationDetailsState by remember(initialMusculationDetails) { mutableStateOf(initialMusculationDetails) }

    // --- LOGIQUE AUTO-COMPLETE ---

    // 1. Écoute réactive propre via LiveData
    val allNames by (viewModel?.getDistinctActiviteNames()?.observeAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })

    var expandedNom by remember { mutableStateOf(false) }

    // 2. Filtrage optimisé (limité à 5 résultats)
    val filteredNames = remember(nom, allNames) {
        if (nom.isEmpty()) {
            allNames
        } else {
            allNames.filter { it.contains(nom, ignoreCase = true) }
        }
    }
    // On récupère TOUTES les activités pour voir si la base est vraiment vide
    val toutesLesActivites by (viewModel?.getAllActivites()?.observeAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })
    val coroutineScope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Reduced padding to fit better on small screens
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isCreationMode) "Nouvelle Activité" else "Modifier l'Activité",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- NOM FIELD WITH IMPROVED DROPDOWN ---
                    ExposedDropdownMenuBox(
                        expanded = expandedNom && filteredNames.isNotEmpty(),
                        onExpandedChange = { expandedNom = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                            OutlinedTextField(
                                value = nom,
                                onValueChange = {
                                    nom = it
                                    expandedNom = true
                                },
                                label = { Text("Nom de l'activité") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(), // CRITICAL: This attaches the menu to the field
                                trailingIcon = {
                                    // Using the standard trailing icon which handles rotation
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNom)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                        if (filteredNames.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expandedNom,
                                onDismissRequest = { expandedNom = false }
                            ) {
                                filteredNames.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            nom = name
                                            expandedNom = false

                                            // Récupération instantanée de TOUS les détails
                                            viewModel?.let { vm ->
                                                coroutineScope.launch {
                                                    // 1. On récupère l'activité de base
                                                    val last = vm.getLastActiviteByName(name)

                                                    last?.let {
                                                        description = it.description
                                                        typeActivite = it.typeActivite
                                                        niveau = it.niveau
                                                        tempsEffectueMinutes = it.tempsEffectue.toMinutes().toString()

                                                        // 2. On récupère les détails spécifiques selon le type
                                                        if (it.typeActivite == TypeObjectif.COURSE) {
                                                            val courseDetails = vm.getCourseActiviteByActiviteIdSuspend(it.id)
                                                            // On conserve l'ID de la modification en cours pour éviter les doublons !
                                                            val currentId = courseDetailsState?.id ?: 0L
                                                            courseDetailsState = courseDetails?.copy(id = currentId, activiteId = act.id)
                                                            musculationDetailsState = null

                                                        } else if (it.typeActivite == TypeObjectif.MUSCULATION) {
                                                            val muscuDetails = vm.getMusculationActiviteByActiviteIdSuspend(it.id)
                                                            val currentId = musculationDetailsState?.id ?: 0L
                                                            musculationDetailsState = muscuDetails?.copy(id = currentId, activiteId = act.id)
                                                            courseDetailsState = null

                                                        } else {
                                                            courseDetailsState = null
                                                            musculationDetailsState = null
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        }



                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReadOnlyField(
                            value = date.format(dateFormatter),
                            label = "Date",
                            modifier = Modifier.weight(1f),
                            onClick = { showDatePicker = true }
                        )
                        ReadOnlyField(
                            value = heureDeDebut.format(timeFormatter),
                            label = "Heure",
                            modifier = Modifier.weight(1f),
                            onClick = { showTimePicker = true }
                        )
                    }

                    OutlinedTextField(
                        value = tempsEffectueMinutes,
                        onValueChange = { tempsEffectueMinutes = it },
                        label = { Text("Temps (min)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // --- TYPE SELECTOR ---
                    var expandedType by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        OutlinedTextField(
                            value = typeActivite.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type d'activité") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            TypeObjectif.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        typeActivite = type
                                        expandedType = false
                                        // Reset other details when changing type to avoid data corruption
                                        if (type != TypeObjectif.COURSE) courseDetailsState = null
                                        if (type != TypeObjectif.MUSCULATION) musculationDetailsState = null
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuComponent(
                        label = "Niveau",
                        items = NiveauExperience.entries.map { it.name },
                        selectedItem = niveau.name,
                        onItemSelected = { selectedString ->
                            niveau = NiveauExperience.valueOf(selectedString)
                        }
                    )

                    ValidationField(
                        isValid = estComplete,
                        onStateChange = { estComplete = it }
                    )

                    // --- INJECTS CUSTOM FORMS (Course/Musculation) ---
                    SpecificActivityFormContent(
                        type = typeActivite,
                        courseDetails = courseDetailsState,
                        musculationDetails = musculationDetailsState,
                        onCourseDetailsChange = { courseDetailsState = it },
                        onMusculationDetailsChange = { musculationDetailsState = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ActionButtons(
                        isCreationMode = isCreationMode,
                        activite = act,
                        onSave = {
                            val updatedActivite = act.copy(
                                nom = nom,
                                description = description,
                                date = date,
                                heureDeDebut = heureDeDebut,
                                tempsEffectue = Duration.ofMinutes(tempsEffectueMinutes.toLongOrNull() ?: 0),
                                estComplete = estComplete,
                                niveau = niveau,
                                typeActivite = typeActivite
                            )
                            val specificCourseData = if (typeActivite == TypeObjectif.COURSE) courseDetailsState else null
                            val specificMuscuData = if (typeActivite == TypeObjectif.MUSCULATION) musculationDetailsState else null

                            onSave(updatedActivite, specificCourseData, specificMuscuData)
                        },
                        onDelete = onDelete,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }

    // Pickers stay the same...
    if (showDatePicker) {
        DatePickerComponent(
            initialDate = date,
            onDateSelected = { date = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { heureDeDebut = it; showTimePicker = false }
        )
    }
}
@Composable
private fun ActionButtons(
    isCreationMode : Boolean = false,
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
        if (!isCreationMode) {
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
        }

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

@Composable
fun SpecificActivityFormContent(
    type: TypeObjectif,
    courseDetails: CourseActivite?,
    musculationDetails: MusculationActivite?,
    onCourseDetailsChange: (CourseActivite) -> Unit,
    onMusculationDetailsChange: (MusculationActivite) -> Unit
) {
    when (type) {
        TypeObjectif.COURSE -> {
            val safeDetails = courseDetails ?: CourseActivite(
                activiteId = 0,
                vitesseMoyenne = 10.0,
                vitesseMax = 13.0,
                bpmMoyen = 130,
                bpmMax = 200,
                distanceEffectuee = 10.0,
                distanceParZoneFc = emptyMap(),
                tempsParZoneFc = emptyMap(),
                traceGpsJson = null
            )

            LaunchedEffect(courseDetails) {
                if (courseDetails == null) onCourseDetailsChange(safeDetails)
            }

            CourseActivityForm(
                courseDetails = safeDetails,
                onCourseDetailsChange = onCourseDetailsChange
            )
        }
        TypeObjectif.MUSCULATION -> {
            val safeDetails = musculationDetails ?: MusculationActivite(
                activiteId = 0,
                musclesCibles = emptyList(),
                series = emptyList(),
                effortRessenti = EffortRessenti.ECHAUFFEMENT,

            )

            LaunchedEffect(musculationDetails) {
                if (musculationDetails == null) onMusculationDetailsChange(safeDetails)
            }

            MusculationActivityForm(
                musculationDetails = safeDetails,
                onDetailsChange = onMusculationDetailsChange
            )
        }
        else -> {}
    }
}
