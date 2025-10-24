package com.example.pllrun.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.Classes.JourSemaine


import com.example.pllrun.Classes.TypeDecoupage
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.InventaireViewModel

import com.example.pllrun.R
import java.time.LocalDate
import kotlin.text.lowercase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnregistrementScreen(
    viewModel: InventaireViewModel?,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    // États pour les champs de formulaire existants
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var poids by remember { mutableStateOf("") }
    var taille by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    // Nouveaux états pour les champs ajoutés
    var sexe by remember { mutableStateOf("") }
    var niveauExperience by remember { mutableStateOf("") }
    var poidsCible by remember { mutableStateOf("") }
    var vma by remember { mutableStateOf("") }
    var fcm by remember { mutableStateOf("") }
    var fcr by remember { mutableStateOf("") }

    // États pour les jours d'entraînement
    var joursSelectionnes by remember { mutableStateOf(setOf<JourSemaine>()) }
    var lundi by remember { mutableStateOf(false) }
    var mardi by remember { mutableStateOf(false) }
    var mercredi by remember { mutableStateOf(false) }
    var jeudi by remember { mutableStateOf(false) }
    var vendredi by remember { mutableStateOf(false) }
    var samedi by remember { mutableStateOf(false) }
    var dimanche by remember { mutableStateOf(false) }

    // États pour les menus déroulants
    var sexeExpanded by remember { mutableStateOf(false) }
    var niveauExpanded by remember { mutableStateOf(false) }

    // Options pour les menus déroulants
    var sex by remember { mutableStateOf(Sexe.NON_SPECIFIE) }
    var niveau by remember { mutableStateOf(NiveauExperience.DEBUTANT) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
            .padding(16.dp), // Réduit le padding général
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Utilisation d'un Column scrollable pour gérer tous les champs
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // Ajout du scroll
            verticalArrangement = Arrangement.spacedBy(12.dp) // Réduit l'espacement
        ) {
            // Titre "Bienvenue !"
            Text(
                text = "Bienvenue !",
                fontSize = 22.sp, // Légèrement réduit
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp), // Réduit le padding
                textAlign = TextAlign.Center
            )

            // Photo de profil ronde - TAILLE RÉDUITE
            Box(
                modifier = Modifier
                    .size(80.dp) // Réduit de 120dp à 80dp
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    .clickable { /* TODO: Ouvrir la galerie */ }
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_icon),
                    contentDescription = "Ajouter une photo de profil",
                    modifier = Modifier.size(30.dp) // Réduit la taille de l'icône
                )
            }

            // Texte "Ajouter une photo de profil"
            Text(
                text = "Ajouter une photo de profil",
                fontSize = 12.sp, // Réduit la taille de police
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Ligne séparatrice
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Informations personnelles
            Text(
                text = "Informations personnelles",
                fontSize = 16.sp, // Légèrement réduit
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Première ligne : Nom et Prénom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacement réduit
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nom",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prénom",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true
                    )
                }
            }

            // Deuxième ligne : Poids, Taille et Âge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacement réduit
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Poids",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = poids,
                        onValueChange = { poids = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "Kg",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Taille",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = taille,
                        onValueChange = { taille = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "Cm",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Âge",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Menu déroulant Sexe
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sexe",
                    fontSize = 12.sp, // Réduit
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = sexeExpanded,
                    onExpandedChange = { sexeExpanded = !sexeExpanded }
                ) {
                    OutlinedTextField(
                        value = sexe,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        placeholder = {
                            Text(
                                "Sélectionner",
                                color = Color.Gray,
                                fontSize = 12.sp // Réduit
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexeExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = sexeExpanded,
                        onDismissRequest = { sexeExpanded = false }
                    ) {
                        Sexe.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toString(), fontSize = 14.sp) },
                                onClick = {
                                    sexe = option.name
                                    sexeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Menu déroulant Niveau d'expérience
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Niveau d'expérience",
                    fontSize = 12.sp, // Réduit
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = niveauExpanded,
                    onExpandedChange = { niveauExpanded = !niveauExpanded }
                ) {
                    OutlinedTextField(
                        value = niveauExperience,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        placeholder = {
                            Text(
                                "Sélectionner",
                                color = Color.Gray,
                                fontSize = 12.sp // Réduit
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = niveauExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = niveauExpanded,
                        onDismissRequest = { niveauExpanded = false }
                    ) {
                        NiveauExperience.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.libelle, fontSize = 14.sp) },
                                onClick = {
                                    niveauExperience = option.name
                                    niveauExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Section Jours d'entraînement
            Text(
                text = "Jours d'entraînement préférés",
                fontSize = 14.sp, // Réduit
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // --- Section Jours d'entraînement ---
            Text(
                "Jours d'entraînement préférés",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Boucle pour générer les DayCheckbox
            val jours = JourSemaine.entries.chunked(3) // Divise les 7 jours en groupes de 3
            Column {
                jours.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        rowItems.forEach { day ->
                            DayCheckbox(
                                day = day,
                                checked = joursSelectionnes.contains(day),
                                onCheckedChange = { isChecked ->
                                    joursSelectionnes = if (isChecked) {
                                        joursSelectionnes + day
                                    } else {
                                        joursSelectionnes - day
                                    }
                                }
                            )
                        }
                        // Pour aligner les colonnes si la dernière ligne n'a pas 3 jours
                        if (rowItems.size < 3) {
                            Spacer(modifier = Modifier.weight((3 - rowItems.size).toFloat()))
                        }
                    }
                }
            }

                // Section Informations optionnelles
                Text(
                    text = "Informations optionnelles",
                    fontSize = 14.sp, // Réduit
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Champ Poids cible
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Poids cible",
                            fontSize = 12.sp, // Réduit
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = " (optionnel)",
                            fontSize = 10.sp, // Réduit
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    OutlinedTextField(
                        value = poidsCible,
                        onValueChange = { poidsCible = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "Kg",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }

                // Ligne VMA, FCM, FCR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacement réduit
                ) {
                    // VMA
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VMA",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = " (opt)",
                                fontSize = 10.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        OutlinedTextField(
                            value = vma,
                            onValueChange = { vma = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp), // Réduit
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // FCM
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FCM",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = " (opt)",
                                fontSize = 10.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        OutlinedTextField(
                            value = fcm,
                            onValueChange = { fcm = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp), // Réduit
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // FCR
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FCR",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = " (opt)",
                                fontSize = 10.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        OutlinedTextField(
                            value = fcr,
                            onValueChange = { fcr = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp), // Réduit
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // Espace supplémentaire en bas pour le scroll
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bouton Suivant
            Button(
                onClick = {
                    onSave()
                    onNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp), // Légèrement réduit
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF751F)
                )
            ) {
                Text(
                    text = "Suivant",
                    fontSize = 16.sp, // Réduit
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

    // --- Fonction de Sauvegarde ---
    fun saveUser() {
        // Calcul d'une date de naissance approximative à partir de l'âge
        val calculatedBirthDate = age.toIntOrNull()?.let {
            LocalDate.now().minusYears(it.toLong())
        }

        viewModel?.addNewUtilisateur(
            nom = nom,
            prenom = prenom,
            dateDeNaissance = calculatedBirthDate,
            sexe = sex,
            // On ne peut pas avoir poids et poidsCible, on utilise poidsCible comme poids initial pour l'exemple
            poids = poidsCible.toDoubleOrNull() ?: 0.0,
            // La taille n'est pas dans le formulaire, on met une valeur par défaut
            taille = 0,
            vma = vma.toDoubleOrNull(),
            fcm = fcm.toIntOrNull(),
            fcr = fcr.toIntOrNull(),
            niveauExperience = niveau,
            joursEntrainementDisponibles = joursSelectionnes.toList() // On convertit le Set en List
        )
    }
    }




// Composant réutilisable pour les checkboxes des jours
@Composable
fun DayCheckbox(
    day: JourSemaine,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp) // Taille réduite
        )
        Text(
            text = day.name.lowercase().replaceFirstChar { it.titlecase() },
            fontSize = 12.sp, // Réduit
            color = Color.Black,
            modifier = Modifier.padding(start = 2.dp) // Espacement réduit
        )
    }
}
