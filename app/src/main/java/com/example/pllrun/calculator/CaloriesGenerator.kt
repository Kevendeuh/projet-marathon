package com.example.pllrun.calculator

import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.Period

data class ApportsNutritionnels(
    val calories: Float,
    val proteines: Float, // en grammes
    val glucides: Float,  // en grammes
    val lipides: Float     // en grammes
)
object CaloriesGenerator {

    fun calculateTotalCalories(
        utilisateur: Utilisateur?,
        activitesDuJour: List<Activite>
    ): ApportsNutritionnels {
        // Si l'utilisateur n'est pas fourni, on ne peut rien calculer.
        if (utilisateur == null) {
            return ApportsNutritionnels(0F,0F,0F,0F)
        }

        // 1. Calcul du métabolisme de base (RMR)
        val age = utilisateur.dateDeNaissance?.let {
            Period.between(it, LocalDate.now()).years
        } ?: 25 // Âge par défaut

        val rmr = when (utilisateur.sexe.name) {
            "HOMME" -> (9.65F * utilisateur.poids.toFloat()) + (5.73F * utilisateur.taille.toFloat()) - (5.08F * age.toFloat()) + 260F
            "FEMME" -> (7.38F * utilisateur.poids.toFloat()) + (6.07F * utilisateur.taille.toFloat()) - (2.31F * age.toFloat()) + 43F
            else -> 1500F
        }

        // 2. Calcul du facteur de poids cible
        var facteurPoidsCible = 1.0F
        if (utilisateur.poidsCible > 0.0 && utilisateur.poids > 0.0) {
            facteurPoidsCible = (utilisateur.poids / utilisateur.poidsCible).toFloat()
            // Plafonnage
            facteurPoidsCible = facteurPoidsCible.coerceIn(0.7F, 1.3F)
        }

        // 3. Calcul du coefficient d'activité basé sur les activités du jour fournies
        var coefficient = 1.2F // Coefficient de base pour une personne sédentaire
        activitesDuJour.forEach { activite ->
            coefficient += (activite.tempsEffectue?.toMinutes() ?: 0L) * 0.001F * activite.niveau.facteur
        }
        // Plafonnage
        coefficient = coefficient.coerceAtMost(2.5F)

        val calorieJournaliere = rmr * coefficient * facteurPoidsCible

        val proteines = (calorieJournaliere * 0.2F) / 4F
        val glucides = (calorieJournaliere * 0.3F) / 4F
        val lipides = (calorieJournaliere * 0.5F) / 4F


        // 4. Calcul final
        return ApportsNutritionnels(calorieJournaliere, proteines, glucides, lipides)
    }
}