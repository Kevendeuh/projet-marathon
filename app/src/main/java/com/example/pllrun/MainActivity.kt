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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import kotlinx.coroutines.coroutineScope
import java.time.Duration
import java.time.format.DateTimeFormatter


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
                UtilisateurScreen(viewModel = viewModel)
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
                onUtilisateurClick = { utilisateur ->
                    selectedUtilisateur = utilisateur // Set the selected user to open the dialog
                }
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
    }
}

@Composable
fun UtilisateurList(utilisateurs: List<Utilisateur>,
                    viewModel: InventaireViewModel,
                    onUtilisateurClick: (Utilisateur) -> Unit) {
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

                UtilisateurCard(utilisateur = utilisateur,
                    objectifs = objectifs,
                    viewModel = viewModel,
                    onClick = { onUtilisateurClick(utilisateur) }) // Pass the user to the click handler
            }
        }
    }
}

@Composable
fun UtilisateurCard(utilisateur: Utilisateur,
                    objectifs:List<Objectif>,
                    viewModel: InventaireViewModel,
                    onClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row (modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = "${utilisateur.prenom} ${utilisateur.nom}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.weight(1f))


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
                    ObjectifCard(objectif = objectif, viewModel = viewModel)
                }
            }
        }
    }
}
@Composable
fun ObjectifCard(objectif: Objectif, viewModel: InventaireViewModel) {
    val activites by viewModel.getActivitesForObjectif(objectif.id).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
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
                    ActiviteItem(activite = activite, viewModel = viewModel) // Utilise un nouveau composable
                }
            } else {
                Text("  Aucune activité planifiée.", color = Color.Gray)
            }
        }
    }
}

@Composable
fun ActiviteItem(activite: Activite, viewModel: InventaireViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("EEE dd MMM")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp)
    ) {
        Text(
            text = "  - ${activite.nom} le ${activite.date.format(formatter)}",
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                coroutineScope.launch {
                    viewModel.deleteActivite(activite)
                }
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Supprimer l'activité",
                tint = Color.Gray
            )
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PllRunTheme {
        Greeting("Android")
    }
}