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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pllrun.R
import com.example.pllrun.InventaireViewModel
import java.time.LocalDate
import com.example.pllrun.Classes.Objectif
import java.time.format.DateTimeFormatter

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
        Row(
            modifier = Modifier
                .padding(12.dp) // Padding interne
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icône pour représenter un objectif (cohérent avec le thème "cible")
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flag), // Utilisez une icône de drapeau ou de cible
                    contentDescription = "Icône d'objectif",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black.copy(alpha = 0.8f)
                )
            }

            // Colonne pour le nom et les dates
            Column(
                modifier = Modifier.weight(1f), // Prend l'espace restant
                verticalArrangement = Arrangement.Center
            ) {
                // Nom de l'objectif
                Text(
                    text = objectif.nom,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1, // Assure que le titre ne prend pas trop de place
                    overflow = TextOverflow.Ellipsis // Ajoute "..." si le texte est trop long
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dates de début et de fin
                Text(
                    text = "Du ${objectif.dateDeDebut.format(dateFormatter)} au ${objectif.dateDeFin.format(dateFormatter)}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Icône "Chevron" pour indiquer que l'élément est cliquable
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right), // Assurez-vous d'avoir cette icône
                contentDescription = "Voir les détails de l'objectif",
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
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
    val objectifs by produceState<List<Objectif>>(initialValue = emptyList(), utilisateurId) {
        viewModel.getObjectifsForUtilisateur(utilisateurId).collect { nouveauxObjectifs ->
            value = nouveauxObjectifs // 'value' met à jour le state produit
        }
    }
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
    // On utilise les mêmes états que dans l'écran de détail

    var objectifInitial by remember { mutableStateOf<Objectif?>(null) }
    var nomObjectif by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf(LocalDate.now()) }
    var dateFin by remember { mutableStateOf(LocalDate.now()) }
    // ... Ajoutez les autres états (niveau, type, etc.)

    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // On charge les données de l'objectif
    LaunchedEffect(key1 = objectifId) {
        viewModel.getObjectifById(objectifId).collect { objectifFromDb ->
            if (objectifFromDb != null) {
                objectifInitial = objectifFromDb
                // Pré-remplissage des champs
                nomObjectif = objectifFromDb.nom
                dateDebut = objectifFromDb.dateDeDebut
                dateFin = objectifFromDb.dateDeFin
                // ... pré-remplir les autres champs
            }
        }
    }

    // Le composant Dialog qui flotte par-dessus l'écran
    Dialog(onDismissRequest = onDismiss) {
        // Le contenu visuel de notre dialogue
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp), // Laisse de l'espace en haut et en bas
            shape = RoundedCornerShape(16.dp),
        ) {
            // Si les données ne sont pas chargées, on affiche un loader
            if (objectifInitial == null) {
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
                        .verticalScroll(rememberScrollState()) // Pour les petits écrans
                ) {
                    Text(
                        text = "Modifier l'Objectif",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // ---- FORMULAIRE (similaire à ObjectifDetailScreen) ----
                    OutlinedTextField(
                        value = nomObjectif,
                        onValueChange = { nomObjectif = it },
                        label = { Text("Nom de l'objectif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = dateDebut.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Début") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePickerDebut = true }
                        )
                        OutlinedTextField(
                            value = dateFin.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fin") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePickerFin = true }
                        )
                    }

                    // ... Ajoutez les autres champs (Dropdowns, Description, etc.)

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
                                val objectifMisAJour = objectifInitial?.copy(
                                    nom = nomObjectif,
                                    dateDeDebut = dateDebut,
                                    dateDeFin = dateFin,
                                    // ... mettez à jour les autres champs
                                )
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


