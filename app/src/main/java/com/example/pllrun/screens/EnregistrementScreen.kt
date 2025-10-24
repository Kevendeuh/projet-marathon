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
import com.example.pllrun.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnregistrementScreen(
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
    val optionsSexe = listOf("Homme", "Femme", "Autre", "Non défini")
    val optionsNiveau = listOf("Débutant", "Intermédiaire", "Avancé")

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
                        optionsSexe.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 14.sp) },
                                onClick = {
                                    sexe = option
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
                        optionsNiveau.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 14.sp) },
                                onClick = {
                                    niveauExperience = option
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

            // Checkboxes pour les jours de la semaine - disposition compacte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    DayCheckbox(day = "Lundi", checked = lundi, onCheckedChange = { lundi = it })
                    DayCheckbox(day = "Mardi", checked = mardi, onCheckedChange = { mardi = it })
                    DayCheckbox(day = "Mercredi", checked = mercredi, onCheckedChange = { mercredi = it })
                }
                Column {
                    DayCheckbox(day = "Jeudi", checked = jeudi, onCheckedChange = { jeudi = it })
                    DayCheckbox(day = "Vendredi", checked = vendredi, onCheckedChange = { vendredi = it })
                }
                Column {
                    DayCheckbox(day = "Samedi", checked = samedi, onCheckedChange = { samedi = it })
                    DayCheckbox(day = "Dimanche", checked = dimanche, onCheckedChange = { dimanche = it })
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
}

// Composant réutilisable pour les checkboxes des jours
@Composable
fun DayCheckbox(
    day: String,
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
            text = day,
            fontSize = 12.sp, // Réduit
            color = Color.Black,
            modifier = Modifier.padding(start = 2.dp) // Espacement réduit
        )
    }
}