package com.example.pllrun.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.pllrun.ui.theme.PllRunTheme
import java.time.Duration
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectifScreen(
    viewModel: InventaireViewModel?,
    onSaveAndNext: () -> Unit,
    onSkip: () -> Unit
) {
    // États pour les champs de formulaire
    var nomObjectif by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf("") }
    var dateFin by remember { mutableStateOf("") }
    var niveau by remember { mutableStateOf(NiveauExperience.DEBUTANT) }
    var type by remember { mutableStateOf(TypeObjectif.MARATHON) }
    var decoupage by remember { mutableStateOf(TypeDecoupage.UNIQUE) }

    var typeDecoupage by remember { mutableStateOf("") }
    var descriptionObjectif by remember { mutableStateOf("") }

    // Options pour les menus déroulants
    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    var showNiveauDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showDecoupageDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1)) // Fond gris clair
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date début",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = dateDebut,
                        onValueChange = { dateDebut = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "JJ/MM/AAAA",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    )
                }

                // Champ Date fin
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date fin",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = dateFin,
                        onValueChange = { dateFin = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "JJ/MM/AAAA",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    )
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

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton Enregistrer
        Button(
            onClick = {
                if(viewModel != null){
                    val nouvelObjectif = Objectif(
                        nom = nomObjectif,
                        dateDeDebut = LocalDate.now(),
                        dateDeFin = LocalDate.now(),
                        niveau = niveau,
                        type = type,
                        typeDecoupage = decoupage,
                        tauxDeProgression = 0.0,
                        description = descriptionObjectif,
                        utilisateurId = 0,
                        estValide = false
                    )
                    //viewModel.addNewObjectif(objectif = nouvelObjectif)

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
        ObjectifScreen( null,{ }, { })
    }
}