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
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.rememberNavController
import com.example.pllrun.Classes.InventaireRepository
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.nav.AppNavHost

class MainActivity : ComponentActivity() {

    // Initialize the ViewModel using the factory.
    // This connects the UI to your database logic.
    private val viewModel: InventaireViewModel by viewModels {
        val database = InventaireRoomDatabase.getDatabase(applicationContext)
        val repository = InventaireRepository(InventaireRoomDatabase.getDatabase(this).objectifDao(),InventaireRoomDatabase.getDatabase(this).utilisateurDao(),InventaireRoomDatabase.getDatabase(this).measurementDao() )
        //val database = (application as PllRunApplication).database
        InventaireViewModelFactory(
            utilisateurDao = InventaireRoomDatabase.getDatabase(this).utilisateurDao(),
            objectifDao = InventaireRoomDatabase.getDatabase(this).objectifDao(),
            recetteDao = InventaireRoomDatabase.getDatabase(this).recetteDao(),
            application = application,
            inventaireRepository = repository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PllRunTheme(dynamicColor = false) {
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




