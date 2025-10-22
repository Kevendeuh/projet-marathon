package com.example.pllrun.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.pllrun.Classes.* // Utilisateur, Sexe, NiveauExperience, JourSemaine
import java.time.LocalDate
import com.example.pllrun.PlannerViewModel

class PlannerActivity : ComponentActivity() {

    // ViewModel MVVM côté Activity
    private val vm: PlannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: remplace par un utilisateur réel depuis Room (dao.getItem(...) / Flow)
        val utilisateurDemo = Utilisateur().apply {
            nom = "Ndiaye"
            prenom = "Charles"
            sexe = Sexe.HOMME
            taille = 200
            poids = 93.2
            niveauExperience = NiveauExperience.INTERMEDIAIRE
            joursEntrainementDisponibles = listOf(
                JourSemaine.LUNDI, JourSemaine.MARDI, JourSemaine.JEUDI, JourSemaine.DIMANCHE
            )
        }

        setContent {
            // Option A : la Composable attend un StateFlow et fait collectAsState() à l’intérieur
            PlannerScreen(
                stateFlow = vm.state,
                onGenerateDefault = {
                    vm.buildPlanForTarget(
                        utilisateurDemo,
                        LocalDate.now().plusWeeks(16)
                    )
                },
                onReset = vm::reset
            )
        }
    }
}
