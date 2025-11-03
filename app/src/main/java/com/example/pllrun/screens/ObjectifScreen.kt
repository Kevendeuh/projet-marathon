package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.TypeDecoupage
import com.example.pllrun.Classes.TypeObjectif
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.components.DatePickerComponent
import com.example.pllrun.ui.theme.PllRunTheme
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectifScreen(
    viewModel: InventaireViewModel?,
    onSaveAndNext: () -> Unit,
    onSkip: () -> Unit,
    utilisateurId: Long
) {
    // États pour les champs de formulaire
    var nomObjectif by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    var dateFin by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    var niveau by remember { mutableStateOf(NiveauExperience.DEBUTANT) }
    var type by remember { mutableStateOf(TypeObjectif.MARATHON) }
    var decoupage by remember { mutableStateOf(TypeDecoupage.UNIQUE) }

    var descriptionObjectif by remember { mutableStateOf("") }

    // Options pour les menus déroulants
    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    var showNiveauDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showDecoupageDropdown by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // --- AFFICHAGE CONDITIONNEL DES DIALOGUES DE DATE ---
    // Cette logique s'activera dès que le booléen correspondant passera à true

    if (showDatePickerDebut) {
        DatePickerComponent(
            initialDate = dateDebut,
            onDateSelected = { newDate ->
                dateDebut = newDate
                if (newDate.isAfter(dateFin)) {
                    dateFin = newDate.plusDays(1)
                }
            },
            onDismiss = { showDatePickerDebut = false } // Cache le dialogue
        )
    }

    if (showDatePickerFin) {
        DatePickerComponent(
            initialDate = dateFin,
            onDateSelected = { newDate ->
                // On s'assure que la date de fin n'est pas avant la date de début
                if (!newDate.isBefore(dateDebut)) {
                    dateFin = newDate
                }
            },
            onDismiss = { showDatePickerFin = false } // Cache le dialogue
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1)) // Fond gris clair
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titre "Quel est votre objectif ?"
        Text(
            text = "Quel est votre objectif ?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        // Formulaire des champs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Champ Nom de l'objectif
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Nom de l'objectif",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = nomObjectif,
                    onValueChange = { nomObjectif = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "Ex: Préparation marathon",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            // Ligne Date début et Date fin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                    // Champ Date début
                    Column(modifier = Modifier.weight(1f)
                        .clickable( onClick = { showDatePickerDebut = true })) {
                        Text(
                            text = "Date début",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box {

                            OutlinedTextField(
                                // On affiche la date formatée
                                value = dateDebut.format(dateFormatter),
                                onValueChange = {}, // Le champ n'est pas éditable au clavier
                                readOnly = true,    // On le met en lecture seule
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDatePickerDebut = true
                                    }, // AU CLIC : on active le dialogue !
                                shape = RoundedCornerShape(8.dp),
                                textStyle = TextStyle(fontSize = 16.sp),
                                singleLine = true
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize() // Prend exactement la même taille que le OutlinedTextField
                                    .clickable(
                                        onClick = { showDatePickerDebut = true },
                                        // Enlève l'ondulation visuelle du clic si vous le souhaitez
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    )
                            )
                        }


                    }


                // Champ Date fin

                Column(modifier = Modifier.weight(1f)
                    .clickable { showDatePickerFin = true },) {
                    Text(
                            text = "Date fin",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    Box {
                        OutlinedTextField(
                            // On affiche la date formatée
                            value = dateFin.format(dateFormatter),
                            onValueChange = {}, // Le champ n'est pas éditable au clavier
                            readOnly = true,    // On le met en lecture seule
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDatePickerFin = true
                                }, // AU CLIC : on active le dialogue !
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(fontSize = 16.sp),
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize() // Prend exactement la même taille que le OutlinedTextField
                                .clickable(
                                    onClick = { showDatePickerFin = true },
                                    // Enlève l'ondulation visuelle du clic si vous le souhaitez
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        )
                    }
                }
            }

            // Ligne Niveau et Type


            // --- GESTION DE L'ENUM 'TYPEOBJECTIF' ---
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown }
            ) {
                OutlinedTextField(
                    value = type.libelle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type d'objectif") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    TypeObjectif.entries.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.libelle) },
                            onClick = {
                                type = selectionOption
                                showTypeDropdown = false

                                descriptionObjectif = selectionOption.description

                            }
                        )
                    }
                }
            }

            // --- GESTION DE L'ENUM 'NIVEAUEXPERIENCE' ---
            ExposedDropdownMenuBox(
                expanded = showNiveauDropdown,
                onExpandedChange = { showNiveauDropdown = !showNiveauDropdown }
            ) {
                OutlinedTextField(
                    value = niveau.libelle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Niveau") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showNiveauDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showNiveauDropdown,
                    onDismissRequest = { showNiveauDropdown = false }
                ) {
                    NiveauExperience.entries.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.libelle) },
                            onClick = {
                                niveau = selectionOption
                                showNiveauDropdown = false
                            }
                        )
                    }
                }
            }


            // --- GESTION DE L'ENUM 'TYPE DECOUPAGE' ---
            ExposedDropdownMenuBox(
                expanded = showDecoupageDropdown,
                onExpandedChange = { showDecoupageDropdown = !showDecoupageDropdown }
            ) {
                OutlinedTextField(
                    value = decoupage.libelle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Découpage") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDecoupageDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showDecoupageDropdown,
                    onDismissRequest = { showDecoupageDropdown = false }
                ) {
                    TypeDecoupage.entries.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.libelle) },
                            onClick = {
                                decoupage = selectionOption
                                showDecoupageDropdown = false
                            }
                        )
                    }
                }
            }

            // Cadre pour la description de l'objectif
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Description de l'objectif",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = descriptionObjectif,
                        onValueChange = { descriptionObjectif = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        decorationBox = { innerTextField ->
                            if (descriptionObjectif.isEmpty()) {
                                Text(
                                    "Rédiger votre objectif ici ...",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        // Bouton Enregistrer

            Button(
                onClick = {
                    if (viewModel != null) {
                        val nouvelObjectif = Objectif(
                            nom = nomObjectif,
                            dateDeDebut = LocalDate.now(),
                            dateDeFin = LocalDate.now(),
                            niveau = niveau,
                            type = type,
                            typeDecoupage = decoupage,
                            tauxDeProgression = 0.0,
                            description = descriptionObjectif,
                            utilisateurId = utilisateurId,
                            estValide = false
                        )
                        viewModel.addNewObjectif(objectif = nouvelObjectif)

                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF751F) // Orange
                )
            ) {
                Text(
                    text = "Enregistrer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton Suivant
            Button(
                onClick = onSaveAndNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF751F) // Orange
                )
            ) {
                Text(
                    text = "Suivant",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bouton Ignorer pour l'instant
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ignorer pour l'instant",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

    }

}

@Preview(showBackground = true)
@Composable
fun ObjectifScreenPreview() {
    PllRunTheme {
        ObjectifScreen( null,{ }, { },1)
    }
}