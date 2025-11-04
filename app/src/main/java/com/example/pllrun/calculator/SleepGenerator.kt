package com.example.pllrun.calculator

import androidx.compose.foundation.gestures.forEach
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration

object SleepGenerator {

    /**
     * Calcule le temps de sommeil recommandé en minutes basé sur les activités
     * et les objectifs qui se terminent aujourd'hui.
     * @param activitesDuJour Liste des activités pour la journée concernée.
     * @param objectifsFinissantLeJour Liste des objectifs qui se terminent ce jour-là.
     * @return Le temps de sommeil recommandé en minutes (Long).
     */
    fun calculateTotalMinutesSleep(
        activitesDuJour: List<Activite>,
        objectifsFinissantLeJour: List<Objectif>
    ): Long {
        val tempsTotalActiviteMinutes = activitesDuJour
            .sumOf { it.tempsEffectue?.toMinutes() ?: 0L }

        val nombreObjectifsFinis = objectifsFinissantLeJour.count()

        var cyclesDeSommeil = 5 // Base de 7h30 de sommeil

        // Ajoute des cycles en fonction de la durée de l'effort
        if (tempsTotalActiviteMinutes > 180) { // Plus de 3h d'effort
            cyclesDeSommeil += 2
        } else if (tempsTotalActiviteMinutes > 120) { // Plus de 2h d'effort
            cyclesDeSommeil += 1
        }

        // Ajoute un cycle de récupération si un objectif majeur se termine
        if (nombreObjectifsFinis > 0) {
            cyclesDeSommeil += 1
        }

        // Chaque cycle dure 90 minutes. On ajoute 15 minutes pour l'endormissement.
        return (cyclesDeSommeil * 90L) + 15L
    }


/**
 * Calcule l'heure de coucher recommandée basée sur l'heure de fin de la dernière activité.
 * @param heureFinDerniereActivite L'heure de fin de la dernière activité de la journée. Peut être null.
 * @param heureDeCoucheParDefaut L'heure de coucher de base de l'utilisateur.
 * @return L'heure de coucher ajustée (LocalTime).
 */
fun calculateHeureDeCouche(
    heureFinDerniereActivite: LocalTime?,
    heureDeCoucheParDefaut: LocalTime
): LocalTime {
    // Si aucune activité n'est prévue aujourd'hui, on retourne l'heure par défaut.
    if (heureFinDerniereActivite == null) {
        return heureDeCoucheParDefaut
    }

    // On définit un temps de repos "biologique" incompressible avant de dormir (ex: 2 heures).
    val tempsDeReposRecommande = Duration.ofHours(2)

    // L'heure de coucher ne peut pas être avant la fin de l'activité + le temps de repos.
    val heureMinimalePourCoucher = heureFinDerniereActivite.plus(tempsDeReposRecommande)

    // On prend l'heure la plus tardive entre l'heure de coucher par défaut et l'heure minimale calculée.
    return if (heureMinimalePourCoucher.isAfter(heureDeCoucheParDefaut)) {
        heureMinimalePourCoucher
    } else {
        heureDeCoucheParDefaut
    }
}
}