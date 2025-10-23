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

suspend fun calculTotalMinutesSleep(
    utilisateur: Utilisateur,
    viewModel: InventaireViewModel,
    additionalActivites: List<Activite> = emptyList() // <-- NOUVEAU PARAMÈTRE OPTIONNEL
): Long {    // --- 1. Initialisation ---
    val aujourdhui = LocalDate.now()
    var tempsTotalActiviteMinutes: Long = 0
    var nombreObjectifsAujourdhui = 0

    // --- 2. Récupérer tous les objectifs de l'utilisateur ---
    // .first() est une fonction terminale qui attend le premier (et unique) résultat de la liste d'objectifs.
    val objectifs = viewModel.getObjectifsForUtilisateur(utilisateur.id).first()

    // --- 3. Parcourir chaque objectif pour trouver les activités et objectifs du jour ---
    objectifs.forEach { objectif ->
        // Vérifie si la date de fin de l'objectif est aujourd'hui
        if (objectif.dateDeFin == aujourdhui) {
            nombreObjectifsAujourdhui++
        }

        // Récupérer les activités pour cet objectif
        val activites = viewModel.getActivitesForObjectif(objectif.id).first()
        activites.forEach { activite ->
            // Vérifie si l'activité est prévue pour aujourd'hui
            if (activite.date == aujourdhui) {
                // Ajoute la durée de l'activité (en minutes) au total
                tempsTotalActiviteMinutes += activite.tempsEffectue.toMinutes()
            }
        }
    }

    // --- 4. NOUVELLE LOGIQUE : Ajouter les activités de la liste supplémentaire ---
    additionalActivites.forEach { activite ->
        // Vérifie si l'activité de la liste supplémentaire est prévue pour aujourd'hui
        if (activite.date == aujourdhui) {
            tempsTotalActiviteMinutes += activite.tempsEffectue.toMinutes()
        }
    }

    // --- 4. Calculer le nombre de cycles de sommeil ---
    var cyclesDeSommeil = 5 // Nombre de cycles par défaut

    // Logique pour ajouter des cycles en fonction de la charge d'entraînement
    if (tempsTotalActiviteMinutes > 180) { // Plus de 2h d'activité
        cyclesDeSommeil += 2
    } else if (tempsTotalActiviteMinutes > 120) { // Plus de 1h d'activité
        cyclesDeSommeil += 1
    }

    // Si un objectif majeur (comme une course) a lieu aujourd'hui, ajoute un cycle supplémentaire
    if (nombreObjectifsAujourdhui > 0) {
        cyclesDeSommeil += 1
    }

    // --- 5. Calculer et retourner le temps de sommeil total en minutes ---
    // (Nombre de cycles * 90 minutes) + 15 minutes pour s'endormir
    return (cyclesDeSommeil * 90L) + 15L
}

suspend fun calculHeureCouche(
    utilisateur: Utilisateur,
    viewModel: InventaireViewModel,
    heureDeCoucheParDefaut: LocalTime = LocalTime.of(22, 0),
    additionalActivites: List<Activite> = emptyList()
): LocalTime {
    val aujourdhui = LocalDate.now()
    var heureFinDerniereActivite: LocalTime? = null

    // --- 1. Rassembler toutes les activités du jour ---
    val toutesLesActivitesDuJour = mutableListOf<Activite>()
    val objectifs = viewModel.getObjectifsForUtilisateur(utilisateur.id).first()
    objectifs.forEach { objectif ->
        val activitesDB = viewModel.getActivitesForObjectif(objectif.id).first()
        toutesLesActivitesDuJour.addAll(activitesDB.filter { it.date == aujourdhui })
    }
    toutesLesActivitesDuJour.addAll(additionalActivites.filter { it.date == aujourdhui })

    // --- 2. Trouver l'heure de fin de la dernière activité ---
    if (toutesLesActivitesDuJour.isNotEmpty()) {

        toutesLesActivitesDuJour.forEach { activite ->
            val heureDeFinActivite = activite.heureDeDebut.plus(activite.tempsEffectue)
            if (heureFinDerniereActivite == null || heureDeFinActivite.isAfter(heureFinDerniereActivite!!)) {
                heureFinDerniereActivite = heureDeFinActivite
            }
        }
    }

    // --- 3. Logique d'ajustement de l'heure de coucher ---

    // Si aucune activité n'est prévue, on retourne simplement l'heure par défaut.
    if (heureFinDerniereActivite == null) {
        return heureDeCoucheParDefaut
    }

    // Calcule la durée entre la fin de l'activité et l'heure de coucher par défaut.
    val tempsDeReposAvantCouche = Duration.between(heureFinDerniereActivite, heureDeCoucheParDefaut)

    // La durée de repos recommandée est de 2 heures (120 minutes).
    val tempsDeReposRecommande = Duration.ofHours(2)

    // Si le temps de repos est inférieur au temps recommandé...
    if (tempsDeReposAvantCouche < tempsDeReposRecommande) {
        // ... on calcule le décalage nécessaire.
        heureDeCoucheParDefaut.equals(heureFinDerniereActivite.plus(tempsDeReposRecommande))
        // Et on décale l'heure de coucher par défaut d'autant.
        return heureDeCoucheParDefaut
    } else {
        // Sinon, l'heure de coucher par défaut est respectée.
        return heureDeCoucheParDefaut
    }
}