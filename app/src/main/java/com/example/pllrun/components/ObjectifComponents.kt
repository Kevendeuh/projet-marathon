package com.example.pllrun.components

import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.values
import com.example.pllrun.R
import com.example.pllrun.InventaireViewModel
import java.time.LocalDate
import com.example.pllrun.Classes.Objectif
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.type
import com.example.pllrun.Classes.TypeObjectif
import com.example.pllrun.Classes.NiveauExperience
/**
 * Un composant réutilisable pour afficher une carte résumant un objectif.
 * Cette carte est cliquable pour naviguer vers un écran de détail.
 *
 * @param objectif L'objet de données `Objectif` à afficher.
 * @param onClick L'action à exécuter lorsque la carte est cliquée.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectifCard(
    objectif: Objectif,
    onClick: () -> Unit
) {
    // Formatter pour afficher les dates de manière concise et lisible
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    // Convertir le pourcentage (0-100) en fraction (0.0-1.0)
    val progress = (objectif.tauxDeProgression / 100).toFloat()

    Card(
        onClick = onClick, // Rend la carte entière cliquable
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Coins arrondis cohérents avec le reste de l'app
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Fond blanc comme les champs de texte
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // Petite ombre pour donner de la profondeur
        )
    ) {

        // Box pour superposer l'icône de validation principale (en haut à droite)
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // --- EN-TÊTE DE LA CARTE ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = objectif.nom,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f) // Prend l'espace restant
                    )
                    // Icon d'édition
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier l'objectif"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- DÉTAILS DE L'OBJECTIF ---
                Text(
                    text = "Type : ${objectif.type.libelle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Période : ${objectif.dateDeDebut.format(dateFormatter)} au ${objectif.dateDeFin.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SECTION BARRE DE PROGRESSION (MODIFIÉE) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, // Aligne la barre et le texte/icône
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Espace entre la barre et l'élément de droite
                ) {
                    // Barre avec dégradé
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f) // La barre prend tout l'espace disponible
                            .height(8.dp) // Un peu plus épaisse pour une meilleure visibilité
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    // --- AFFICHAGE CONDITIONNEL : POURCENTAGE OU ICÔNE DE VALIDATION ---
                    if (objectif.estValide) {
                        // Si l'objectif est valide, on affiche une icône de check à droite
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Objectif complété",
                            tint = Color(0xFF4CAF50) // Vert
                        )
                    } else {
                        // Sinon, on affiche le pourcentage
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ObjectifsListContent(
    viewModel: InventaireViewModel,
    utilisateurId: Long,
    onObjectifClick: (Long) -> Unit
) {
    // Récupérez la liste des objectifs depuis le ViewModel
    val objectifs by viewModel.getObjectifsForUtilisateur(utilisateurId).observeAsState(initial = emptyList())
    if (objectifs.isEmpty()) {
        Text(
            text = "Aucun objectif défini. Appuyez sur 'Ajouter Objectif' pour commencer !",
            fontSize = 14.sp,
            color = Color.Gray
        )
    } else {
        // On utilise une Column simple car on est déjà dans un composant scrollable
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Affiche les 3 premiers objectifs par exemple, pour ne pas surcharger la carte
            objectifs.take(3).forEach { objectif ->
                ObjectifCard(
                    objectif = objectif,
                    onClick = { onObjectifClick(objectif.id) }
                )
            }
        }
    }
}

// Dans un fichier de composants, par exemple ObjectifComponents.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectifEditDialog(
    viewModel: InventaireViewModel,
    objectifId: Long,
    onDismiss: () -> Unit // Pour fermer le dialogue
) {
    // ---- CHARGEMENT ET ÉTATS DU FORMULAIRE ----

    val objectifCible by viewModel.getObjectifById(objectifId).observeAsState(initial = null)
    val objectif = objectifCible
    var nomObjectif by remember(objectif) { mutableStateOf(objectif?.nom ?: "") }
    var description by remember(objectif) { mutableStateOf(objectif?.description ?: "") }
    var dateDebut by remember(objectif) { mutableStateOf(objectif?.dateDeDebut ?: LocalDate.now()) }
    var dateFin by remember(objectif) { mutableStateOf(objectif?.dateDeFin ?: LocalDate.now()) }
    var typeObjectif by remember(objectif) { mutableStateOf(objectif?.type ?: TypeObjectif.COURSE) }
    var niveau by remember(objectif) { mutableStateOf(objectif?.niveau ?: NiveauExperience.DEBUTANT) }
    var estValide by remember(objectif) { mutableStateOf(objectif?.estValide ?: false) }


    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")


    // Le composant Dialog qui flotte par-dessus l'écran
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            // Si les données ne sont pas chargées, on affiche un loader
            if (objectifCible == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Une fois les données chargées, on affiche le formulaire
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Modifier l'Objectif",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Formulaire scrollable
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // CHAMP NOM
                        EditField(
                            icon = Icons.Filled.Edit,
                            label = "Nom de l'objectif",
                            value = nomObjectif,
                            onValueChange = { nomObjectif = it }
                        )

                        // CHAMP DESCRIPTION
                        EditField(
                            icon = Icons.Filled.Edit,
                            label = "Description",
                            value = description,
                            onValueChange = { description = it },
                            singleLine = false,
                            maxLines = 4
                        )

                        // CHAMPS DATE
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ReadOnlyField(
                                modifier = Modifier.weight(1f),
                                label = "Début",
                                value = dateDebut.format(dateFormatter),
                                onClick = { showDatePickerDebut = true }
                            )
                            ReadOnlyField(
                                modifier = Modifier.weight(1f),
                                label = "Fin",
                                value = dateFin.format(dateFormatter),
                                onClick = { showDatePickerFin = true }
                            )
                        }

                        // CHAMP TYPE OBJECTIF (Dropdown)
                        ExposedDropdownMenuComponent(
                            label = "Type d'objectif",
                            items = TypeObjectif.values().map { it.name.replace("_", " ") },
                            selectedItem = typeObjectif.name.replace("_", " "),
                            onItemSelected = { selectedString ->
                                typeObjectif = TypeObjectif.valueOf(selectedString.replace(" ", "_"))
                            }
                        )

                        // CHAMP NIVEAU (Dropdown)
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
                            isValid = estValide,
                            onStateChange = { estValide = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ---- BOUTONS D'ACTION ----
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Annuler")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // On utilise 'objectifCible' qui contient la version originale
                                val objectifMisAJour = objectifCible?.copy(
                                    nom = nomObjectif,description = description,
                                    dateDeDebut = dateDebut,
                                    dateDeFin = dateFin,
                                    type = typeObjectif,
                                    niveau = niveau,
                                    estValide = estValide
                                )
                                if(estValide){
                                    objectifMisAJour?.tauxDeProgression = 100.0
                                }
                                if (objectifMisAJour != null) {
                                    viewModel.updateObjectif(objectifMisAJour)
                                }
                                onDismiss() // Ferme le dialogue après la sauvegarde
                            }
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }

    // Affiche les DatePicker par-dessus le dialogue si nécessaire
    if (showDatePickerDebut) {
        DatePickerComponent(
            initialDate = dateDebut,
            onDateSelected = { dateDebut = it },
            onDismiss = { showDatePickerDebut = false }
        )
    }
    if (showDatePickerFin) {
        DatePickerComponent(
            initialDate = dateFin,
            onDateSelected = { dateFin = it },
            onDismiss = { showDatePickerFin = false }
        )
    }
}

/**
 * Un champ de texte modifiable avec une icône de début.
 */
@Composable
private fun EditField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = "Icône de modification") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions
    )
}

/**
 * Un champ non modifiable qui ressemble à un OutlinedTextField mais qui est cliquable.
 * Idéal pour les sélecteurs de date/heure.
 */
@Composable
private fun ReadOnlyField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
        // Box cliquable superposée pour intercepter les clics
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

/**
 * Un composant réutilisable pour un menu déroulant Material 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuComponent(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // Important pour lier le champ de texte au menu
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * Un champ pour afficher et modifier l'état de validation d'un objectif.
 * Affiche une icône, un texte, et un interrupteur (Switch).
 */
@Composable
private fun ValidationField(
    isValid: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    val icon = if (isValid) Icons.Filled.CheckCircle else Icons.Filled.Cancel
    val text = if (isValid) "Objectif validé" else "Non validé"
    val color = if (isValid) MaterialTheme.colorScheme.primary else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Rend la rangée cliquable avec un effet visuel
            .clickable { onStateChange(!isValid) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "État de la validation",
                tint = color
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
        }
        Switch(
            checked = isValid,
            onCheckedChange = onStateChange
        )
    }
}




