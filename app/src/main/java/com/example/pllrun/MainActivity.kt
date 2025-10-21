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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.Classes.UtilisateurRoomDatabase
import com.example.pllrun.ui.theme.PllRunTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.window.Dialog 
import java.time.format.DateTimeFormatter 


class MainActivity : ComponentActivity() {

    // Initialize the ViewModel using the factory.
    // This connects the UI to your database logic.
    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory(
            UtilisateurRoomDatabase.getDatabase(this).utilisateurDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PllRunTheme {
                UserScreen(viewModel = viewModel)
                }
            }
        }
    }


@Composable
fun UserScreen(viewModel: InventoryViewModel) {
    // Collect the list of users from the ViewModel as a state.
    // The UI will automatically recompose whenever this list changes.
    val userList by viewModel.getAllUtilisateurs().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var selectedUser by remember { mutableStateOf<Utilisateur?>(null) }
    
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
                        val newUserFirstName = "Antoine"
                        val newUserLastName = "Dev"
                        val userCount = userList.size + 1
                        viewModel.addNewUtilisateur(
                            nom = "$newUserLastName $userCount",
                            prenom = newUserFirstName,
                            poids = 75.5,
                            taille = 180,
                            dateDeNaissance = LocalDate.of(2003, 5, 9),
                            sexe = Sexe.HOMME,
                            
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add New Test User")
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()

            UserList(
                users = userList,
                onUserClick = { user ->
                    selectedUser = user // Set the selected user to open the dialog
                }
            )
        }
        // --- SHOW DIALOG ---
        // When selectedUser is not null, the Dialog will be composed
        selectedUser?.let { user ->
            UserDetailDialog(
                user = user,
                onDismiss = {
                    selectedUser = null // Close the dialog by resetting the state
                }
            )
        }
    }
}

@Composable
fun UserList(users: List<Utilisateur>, onUserClick: (Utilisateur) -> Unit) {
    if (users.isEmpty()) {
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
            items(users) { user ->
                UserCard(user = user, onClick = { onUserClick(user) }) // Pass the user to the click handler
            }
        }
    }
}

@Composable
fun UserCard(user: Utilisateur, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${user.prenom} ${user.nom}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text("ID: ${user.id}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.weight(1f))
                Text("Poids: ${user.poids} kg", fontSize = 14.sp)
            }
        }
    }
}


@Composable
fun UserDetailDialog(user: Utilisateur, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${user.prenom} ${user.nom}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                // Use a standard date format
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

                InfoRow("ID:", user.id.toString())
                InfoRow("Date of Birth:", user.dateDeNaissance?.format(formatter) ?: "N/A")
                InfoRow("Sex:", user.sexe.name)
                InfoRow("Weight:", "${user.poids} kg")
                InfoRow("Height:", "${user.taille} cm")
                InfoRow("Experience:", user.niveauExperience.name)
                InfoRow("VMA:", user.vma?.toString() ?: "N/A")
                InfoRow("FCM:", user.fcm?.toString() ?: "N/A")
                InfoRow("FCR:", user.fcr?.toString() ?: "N/A")

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
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
fun UserScreenPreview() {
    PllRunTheme {
        // This is a static preview and won't interact with the ViewModel
        val previewUsers = listOf(
            Utilisateur(id = 1, nom = "Dupont", prenom = "Jean", poids = 80.0, taille = 185),
            Utilisateur(id = 2, nom = "Martin", prenom = "Marie", poids = 65.0, taille = 170)
        )
        // We can't use the real ViewModel in a preview, so we'll simulate the UI state
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Add New Test User") }
            Spacer(modifier = Modifier.height(16.dp))
            UserList(
                users = previewUsers,
                onUserClick = TODO()
            )
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