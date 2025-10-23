package com.example.pllrun
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.ui.theme.PllRunTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.window.Dialog
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.InventaireRoomDatabase
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.TypeObjectif
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.gestures.forEach
import androidx.navigation.compose.rememberNavController
import com.example.pllrun.Classes.NiveauExperience
import java.util.Calendar
import com.example.pllrun.nav.AppNavHost

class MainActivity : ComponentActivity() {

    // Initialize the ViewModel using the factory.
    // This connects the UI to your database logic.
    private val viewModel: InventaireViewModel by viewModels {
        //val database = (application as PllRunApplication).database
        InventaireViewModelFactory(
            utilisateurDao = InventaireRoomDatabase.getDatabase(this).utilisateurDao(),
            objectifDao = InventaireRoomDatabase.getDatabase(this).objectifDao(),
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PllRunTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                }
            }
        }
    }


@Composable
fun UtilisateurScreen(viewModel: InventaireViewModel) {
    // Collect the list of users from the ViewModel as a state.
    // The UI will automatically recompose whenever this list changes.
    val utilisateurList by viewModel.getAllUtilisateurs().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var selectedUtilisateur by remember { mutableStateOf<Utilisateur?>(null) }
    var selectedObjectif by remember { mutableStateOf<Objectif?>(null) }
    var selectedActivite by remember { mutableStateOf<Activite?>(null) }

    var editingUtilisateur by remember { mutableStateOf<Utilisateur?>(null) }
    var editingObjectif by remember { mutableStateOf<Objectif?>(null) }
    var editingActivite by remember { mutableStateOf<Activite?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("User Management", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            // Button to add a new user
            Button(
                onClick = {
                    coroutineScope.launch {
                        // For this prototype, we add a user with some default test data.
                        val newUtilisateurPrenom = "Antoine"
                        val newUtilisateurNom = "Dev"
                        val utilisateurCount = utilisateurList.size + 1
                        viewModel.addNewUtilisateur(
                            nom = "$newUtilisateurPrenom $utilisateurCount",
                            prenom = newUtilisateurNom,
                            poids = 75.5,
                            taille = 180,
                            dateDeNaissance = LocalDate.of(2003, 5, 9),
                            sexe = Sexe.HOMME,
                            
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter nouvel utilisateur")
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()

            UtilisateurList(
                utilisateurs = utilisateurList,
                viewModel = viewModel,
                onUtilisateurClick = { selectedUtilisateur = it },
                onUtilisateurEdit = { editingUtilisateur = it }, // Lier le clic de modification
                onObjectifClick = { selectedObjectif = it },
                onObjectifEdit = { editingObjectif = it },
                onActiviteClick = { selectedActivite = it },
                onActiviteEdit = { editingActivite = it }
            )
        }
        // --- SHOW DIALOG ---
        // When selectedUser is not null, the Dialog will be composed
        selectedUtilisateur?.let { utilisateur ->
            UtilisateurDetailDialog(
                utilisateur = utilisateur,
                viewModel = viewModel,
                onDismiss = {
                    selectedUtilisateur = null // Close the dialog by resetting the state
                }
            )
        }

        selectedObjectif?.let { objectif ->
            ObjectifDetailDialog(
                objectif = objectif,
                onDismiss = { selectedObjectif = null }
            )
        }

        selectedActivite?.let { activite ->
            ActiviteDetailDialog(
                activite = activite,
                onDismiss = { selectedActivite = null }
            )
        }

        editingUtilisateur?.let { utilisateur ->
            EditUtilisateurDialog(
                utilisateur = utilisateur,
                viewModel = viewModel,
                onDismiss = { editingUtilisateur = null }
            )
        }
        editingObjectif?.let { objectif ->
            EditObjectifDialog(
                objectif = objectif,
                viewModel = viewModel,
                onDismiss = { editingObjectif = null }
            )
        }
        editingActivite?.let { activite ->
            EditActiviteDialog(
                activite = activite,
                viewModel = viewModel,
                onDismiss = { editingActivite = null }
            )
        }
    }
}

@Composable
fun UtilisateurList(utilisateurs: List<Utilisateur>,
                    viewModel: InventaireViewModel,
                    onUtilisateurClick: (Utilisateur) -> Unit,
                    onObjectifClick: (Objectif) -> Unit,
                    onActiviteClick: (Activite) -> Unit,
                    onUtilisateurEdit: (Utilisateur) -> Unit,
                    onObjectifEdit: (Objectif) -> Unit,
                    onActiviteEdit: (Activite) -> Unit) {
    if (utilisateurs.isEmpty()) {
        Text(
            text = "No users in the database. Add one!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 20.dp)
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Existing Users:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(utilisateurs) { utilisateur ->
                val objectifs by viewModel.getObjectifsForUtilisateur(utilisateur.id)
                    .collectAsState(initial = emptyList())

                UtilisateurCard(
                    utilisateur = utilisateur,
                    objectifs = objectifs,
                    viewModel = viewModel,
                    onCardClick = { onUtilisateurClick(utilisateur) },
                    onEditClick = { onUtilisateurEdit(utilisateur) },
                    onObjectifClick = onObjectifClick,
                    onObjectifEdit = onObjectifEdit,
                    onActiviteClick = onActiviteClick,
                    onActiviteEdit = onActiviteEdit
                )
            }
        }
    }
}

@Composable
fun UtilisateurCard(utilisateur: Utilisateur,
                    objectifs:List<Objectif>,
                    viewModel: InventaireViewModel,
                    onCardClick: () -> Unit,
                    onObjectifClick: (Objectif) -> Unit,
                    onActiviteClick: (Activite) -> Unit,
                    onEditClick: () -> Unit,
                    onObjectifEdit: (Objectif) -> Unit,
                    onActiviteEdit: (Activite) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onCardClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row (modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = "${utilisateur.prenom} ${utilisateur.nom}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, "Modifier l'utilisateur", tint = Color.Gray)
                }

                IconButton(onClick = {
                    coroutineScope.launch {
                        viewModel.deleteUtilisateur(utilisateur)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Supprimer l'objectif",
                        tint = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text("ID: ${utilisateur.id}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.weight(1f))
                Text("Poids: ${utilisateur.poids} kg", fontSize = 14.sp)
            }

            // Affichage des objectifs et activités de l'utilisateur
            if (objectifs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                objectifs.forEach { objectif ->
                    ObjectifCard(
                        objectif = objectif, viewModel = viewModel,
                        onCardClick = { onObjectifClick(objectif) },
                        onActiviteClick = onActiviteClick,
                        onEditClick = { onObjectifEdit(objectif) },
                        onActiviteEdit = onActiviteEdit)
                }
            }
        }
    }
}
@Composable
fun ObjectifCard(
    objectif: Objectif,
    viewModel: InventaireViewModel,
    onCardClick: () -> Unit,
    onActiviteClick: (Activite) -> Unit,
    onEditClick: () -> Unit,
    onActiviteEdit: (Activite) -> Unit
) {
    val activites by viewModel.getActivitesForObjectif(objectif.id).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Le nom de l'objectif prend l'espace disponible
                Text(
                    text = "Objectif: ${objectif.nom}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f), // Permet au texte de prendre l'espace
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit, "Modifier l'objectif", tint = Color.Gray)
                }
                // Icône pour supprimer l'objectif entier
                IconButton(onClick = {
                    coroutineScope.launch {
                        viewModel.deleteObjectif(objectif)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Supprimer l'objectif",
                        tint = Color.Gray
                    )
                }
            }

            // Liste des activités
            if (activites.isNotEmpty()) {
                activites.forEach { activite ->
                    ActiviteItem(
                        activite = activite,
                        viewModel = viewModel,
                        onClick = { onActiviteClick(activite) },
                        onEditClick = { onActiviteEdit(activite) }
                    )
                }
            } else {
                Text("  Aucune activité planifiée.", color = Color.Gray)
            }
        }
    }
}


@Composable
fun ActiviteItem(
    activite: Activite,
    viewModel: InventaireViewModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("EEE dd MMM")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Text("  - ${activite.nom} le ${activite.date.format(formatter)}",
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onEditClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Edit, "Modifier l'activité", tint = Color.Gray)
        }
        IconButton(onClick = { coroutineScope.launch { viewModel.deleteActivite(activite) } },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Close, "Supprimer l'activité", tint = Color.Gray)
        }
    }
}

@Composable
fun UtilisateurDetailDialog(utilisateur: Utilisateur,
                             viewModel: InventaireViewModel,
                             onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${utilisateur.prenom} ${utilisateur.nom}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                // Use a standard date format
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

                InfoRow("ID:", utilisateur.id.toString())
                InfoRow("Date of Birth:", utilisateur.dateDeNaissance?.format(formatter) ?: "N/A")
                InfoRow("Sex:", utilisateur.sexe.name)
                InfoRow("Weight:", "${utilisateur.poids} kg")
                InfoRow("Height:", "${utilisateur.taille} cm")
                InfoRow("Experience:", utilisateur.niveauExperience.name)
                InfoRow("VMA:", utilisateur.vma?.toString() ?: "N/A")
                InfoRow("FCM:", utilisateur.fcm?.toString() ?: "N/A")
                InfoRow("FCR:", utilisateur.fcr?.toString() ?: "N/A")

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // BOUTON "AJOUTER OBJECTIF"
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // 1. Créer l'objectif
                                val nouvelObjectif = Objectif(
                                    utilisateurId = utilisateur.id,
                                    nom = "Prépa Marathon Express",
                                    dateDeDebut = LocalDate.now(),
                                    dateDeFin = LocalDate.now().plusWeeks(1),
                                    niveau = utilisateur.niveauExperience,
                                    tauxDeProgression = 0.0,
                                    type = TypeObjectif.MARATHON
                                )
                                // On ne peut récupérer l'ID qu'après insertion, donc on suppose pour le proto
                                // Dans une vraie app, on ferait un insert qui retourne l'ID
                                val objectifId = viewModel.insertAndGetObjectifId(nouvelObjectif).await()
                                // 2. Créer les activités
                                for (i in 0..6) { // Pour chaque jour de la semaine
                                    val dateActivite = LocalDate.now().plusDays(i.toLong())
                                    val nouvelleActivite = Activite(
                                        objectifId = objectifId, // Lien vers l'objectif
                                        nom = "Course du ${dateActivite.dayOfWeek.name.lowercase()}",
                                        date = dateActivite,
                                        distanceEffectuee = 0.0,
                                        tempsEffectue = Duration.ofHours(1),
                                        typeActivite = TypeObjectif.COURSE,
                                        estComplete = false
                                    )
                                    viewModel.addNewActivite(nouvelleActivite)
                                }

                                // 3. Fermer le dialogue
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Ajouter Objectif")
                    }

                    // Bouton "Fermer"
                    Button(onClick = onDismiss) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

@Composable
fun ActiviteDetailDialog(activite: Activite, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Détails de l'Activité", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                InfoRow("ID:", activite.id.toString())
                InfoRow("Nom:", activite.nom)
                InfoRow("Date:", activite.date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                InfoRow("Type:", activite.typeActivite.name)
                InfoRow("Distance:", "${activite.distanceEffectuee} km")
                InfoRow("Heure de debut:", activite.heureDeDebut.format(DateTimeFormatter.ofPattern("HH:mm")) + "h")
                InfoRow("Durée:", activite.tempsEffectue.toMinutes().toString() + " min")
                InfoRow("Complétée:", if (activite.estComplete) "Oui" else "Non")
                InfoRow("Liée à l'objectif ID:", activite.objectifId.toString())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fermer") }
            }
        }
    }
}

@Composable
fun ObjectifDetailDialog(objectif: Objectif, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Détails de l'Objectif", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                InfoRow("ID:", objectif.id.toString())
                InfoRow("Nom:", objectif.nom)
                InfoRow("Type:", objectif.type.name)
                InfoRow("Niveau:", objectif.niveau.name)
                InfoRow("Début:", objectif.dateDeDebut.format(DateTimeFormatter.ISO_LOCAL_DATE))
                InfoRow("Fin:", objectif.dateDeFin.format(DateTimeFormatter.ISO_LOCAL_DATE))
                InfoRow("Progression:", "${objectif.tauxDeProgression} %")
                InfoRow("Lié à l'utilisateur ID:", objectif.utilisateurId.toString())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fermer") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUtilisateurDialog(utilisateur: Utilisateur, viewModel: InventaireViewModel, onDismiss: () -> Unit) {
    // --- États locaux pour chaque champ modifiable ---
    var nom by remember { mutableStateOf(utilisateur.nom) }
    var prenom by remember { mutableStateOf(utilisateur.prenom) }
    var poids by remember { mutableStateOf(utilisateur.poids.toString()) }
    var taille by remember { mutableStateOf(utilisateur.taille.toString()) }
    var vma by remember { mutableStateOf(utilisateur.vma?.toString() ?: "") }
    var fcm by remember { mutableStateOf(utilisateur.fcm?.toString() ?: "") }
    var fcr by remember { mutableStateOf(utilisateur.fcr?.toString() ?: "") }
    var dateDeNaissance by remember { mutableStateOf(utilisateur.dateDeNaissance) }
    var sexe by remember { mutableStateOf(utilisateur.sexe) }
    var niveauExperience by remember { mutableStateOf(utilisateur.niveauExperience) }

    // États pour gérer les composants UI
    var showDatePicker by remember { mutableStateOf(false) }
    var showSexeDropdown by remember { mutableStateOf(false) }
    var showNiveauDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            // --- Column scrollable pour éviter les dépassements d'écran ---
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()), // Très important !
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Modifier l'Utilisateur", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()

                // --- Champs de texte simples ---
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom") }, modifier = Modifier.fillMaxWidth())

                // --- Champ de date cliquable ---
                OutlinedTextField(
                    value = dateDeNaissance?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Choisir une date",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date de Naissance") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                // --- GESTION DE L'ENUM 'SEXE' AVEC UN DROPDOWNMENU ---
                ExposedDropdownMenuBox(
                    expanded = showSexeDropdown,
                    onExpandedChange = { showSexeDropdown = !showSexeDropdown }
                ) {
                    OutlinedTextField(
                        value = sexe.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sexe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSexeDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showSexeDropdown,
                        onDismissRequest = { showSexeDropdown = false }
                    ) {
                        Sexe.entries.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    sexe = selectionOption
                                    showSexeDropdown = false
                                }
                            )
                        }
                    }
                }

                // --- Champs numériques ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = poids,
                        onValueChange = { poids = it },
                        label = { Text("Poids (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = taille,
                        onValueChange = { taille = it },
                        label = { Text("Taille (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = vma, onValueChange = { vma = it }, label = { Text("VMA") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fcm, onValueChange = { fcm = it }, label = { Text("FCM") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fcr, onValueChange = { fcr = it }, label = { Text("FCR") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }

                // --- GESTION DE L'ENUM 'NIVEAUEXPERIENCE' AVEC UN DROPDOWNMENU ---
                ExposedDropdownMenuBox(
                    expanded = showNiveauDropdown,
                    onExpandedChange = { showNiveauDropdown = !showNiveauDropdown }
                ) {
                    OutlinedTextField(
                        value = niveauExperience.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Niveau d'expérience") },
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
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    niveauExperience = selectionOption
                                    showNiveauDropdown = false
                                }
                            )
                        }
                    }
                }

                // --- Boutons Annuler et Sauvegarder ---
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedUtilisateur = utilisateur.copy(
                            nom = nom,
                            prenom = prenom,
                            poids = poids.toDoubleOrNull() ?: utilisateur.poids,
                            taille = taille.toIntOrNull() ?: utilisateur.taille,
                            vma = vma.toDoubleOrNull(),
                            fcm = fcm.toIntOrNull(),
                            fcr = fcr.toIntOrNull(),
                            dateDeNaissance = dateDeNaissance,
                            sexe = sexe,
                            niveauExperience = niveauExperience
                        )
                        viewModel.updateUtilisateur(updatedUtilisateur)
                        onDismiss()
                    }) { Text("Sauvegarder") }
                }
            }
        }
    }

    // --- Date Picker Dialog (le code ne change pas) ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateDeNaissance?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: Instant.now().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= Instant.now().toEpochMilli()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateDeNaissance = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditObjectifDialog(objectif: Objectif, viewModel: InventaireViewModel, onDismiss: () -> Unit) {
    // --- États locaux pour chaque champ modifiable ---
    var nom by remember { mutableStateOf(objectif.nom) }
    var dateDeDebut by remember { mutableStateOf(objectif.dateDeDebut) }
    var dateDeFin by remember { mutableStateOf(objectif.dateDeFin) }
    var niveau by remember { mutableStateOf(objectif.niveau) }
    var type by remember { mutableStateOf(objectif.type) }
    var tauxDeProgression by remember { mutableStateOf(objectif.tauxDeProgression.toString()) }

    // --- États pour gérer les composants UI ---
    var showDatePickerDebut by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    var showNiveauDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()), // Ajout du scroll
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Modifier l'Objectif", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom de l'objectif") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- GESTION DE L'ENUM 'TYPEOBJECTIF' ---
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = !showTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = type.name,
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
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    type = selectionOption
                                    showTypeDropdown = false
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
                        value = niveau.name,
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
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    niveau = selectionOption
                                    showNiveauDropdown = false
                                }
                            )
                        }
                    }
                }

                // --- Champs de date ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateDeDebut.format(formatter),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date de début") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePickerDebut = true }
                    )
                    OutlinedTextField(
                        value = dateDeFin.format(formatter),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date de fin") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePickerFin = true }
                    )
                }

                OutlinedTextField(
                    value = tauxDeProgression,
                    onValueChange = { tauxDeProgression = it },
                    label = { Text("Taux de progression (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Boutons Annuler et Sauvegarder ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedObjectif = objectif.copy(
                            nom = nom,
                            dateDeDebut = dateDeDebut,
                            dateDeFin = dateDeFin,
                            niveau = niveau,
                            type = type,
                            tauxDeProgression = tauxDeProgression.toDoubleOrNull() ?: objectif.tauxDeProgression
                        )
                        viewModel.updateObjectif(updatedObjectif)
                        onDismiss()
                    }) {
                        Text("Sauvegarder")
                    }
                }
            }
        }
    }

    // --- Dialogues DatePicker ---

    if (showDatePickerDebut) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateDeDebut.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePickerDebut = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateDeDebut = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePickerDebut = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDatePickerFin) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateDeFin.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePickerFin = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateDeFin = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePickerFin = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActiviteDialog(activite: Activite, viewModel: InventaireViewModel, onDismiss: () -> Unit) {
    // --- États locaux pour chaque champ modifiable ---
    var nom by remember { mutableStateOf(activite.nom) }
    var date by remember { mutableStateOf(activite.date) }
    var distance by remember { mutableStateOf(activite.distanceEffectuee.toString()) }
    // On convertit la durée en minutes pour une modification plus simple
    var dureeEnMinutes by remember { mutableStateOf(activite.tempsEffectue.toMinutes().toString()) }
    var typeActivite by remember { mutableStateOf(activite.typeActivite) }
    var estComplete by remember { mutableStateOf(activite.estComplete) }

    // --- États pour gérer les composants UI ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Modifier l'Activité", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom de l'activité") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Champ de date cliquable ---
                OutlinedTextField(
                    value = date.format(formatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date de l'activité") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                // --- GESTION DE L'ENUM 'TYPEOBJECTIF' ---
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = !showTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = typeActivite.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type d'activité") },
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
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    typeActivite = selectionOption
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // --- Champs numériques ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dureeEnMinutes,
                        onValueChange = { dureeEnMinutes = it },
                        label = { Text("Durée (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- GESTION DU BOOLEEN 'estComplete' AVEC UN SWITCH ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activité complétée", modifier = Modifier.weight(1f))
                    Switch(
                        checked = estComplete,
                        onCheckedChange = { estComplete = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Boutons Annuler et Sauvegarder ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedActivite = activite.copy(
                            nom = nom,
                            date = date,
                            distanceEffectuee = distance.toDoubleOrNull() ?: activite.distanceEffectuee,
                            // On reconvertit les minutes en objet Duration
                            tempsEffectue = Duration.ofMinutes(dureeEnMinutes.toLongOrNull() ?: activite.tempsEffectue.toMinutes()),
                            typeActivite = typeActivite,
                            estComplete = estComplete
                        )
                        viewModel.updateActivite(updatedActivite)
                        onDismiss()
                    }) { Text("Sauvegarder") }
                }
            }
        }
    }

    // --- Dialogue DatePicker ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}




// Helper composable to keep the UI clean
@Composable
fun InfoRow(label: String, value: String) {
    Row {
        Text(text = label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(text = value, modifier = Modifier.weight(1f))
    }
}


@Preview(showBackground = true)
@Composable
fun UtilisateurScreenPreview() {
    PllRunTheme {
        // This is a static preview and won't interact with the ViewModel
        val previewUtilisateurs = listOf(
            Utilisateur(id = 1, nom = "Dupont", prenom = "Jean", poids = 80.0, taille = 185),
            Utilisateur(id = 2, nom = "Martin", prenom = "Marie", poids = 65.0, taille = 170)
        )
        // We can't use the real ViewModel in a preview, so we'll simulate the UI state
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Add New Test User") }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

