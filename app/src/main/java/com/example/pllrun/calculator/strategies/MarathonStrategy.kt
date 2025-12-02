package com.example.pllrun.calculator.strategies

import androidx.compose.foundation.layout.size
import com.example.pllrun.Classes.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

class MarathonStrategy : IPlanStrategy {

    override fun generate(objectif: Objectif, user: Utilisateur): List<Activite> {
        val activities = mutableListOf<Activite>()

        // 1. Validation : Un marathon demande du temps
        val weeksDuration = ChronoUnit.WEEKS.between(objectif.dateDeDebut, objectif.dateDeFin).toInt()
        if (weeksDuration < 1) return emptyList() // Sécurité minimale

        // 2. Jours disponibles (Triés)
        val userAvailableDays = user.joursEntrainementDisponibles.map { it.toJavaDayOfWeek() }.sorted()
        if (userAvailableDays.isEmpty()) return emptyList()

        // On limite le volume en fonction du niveau pour éviter le surentraînement
        val maxSessionsAllowed = when (user.niveauExperience) {
            NiveauExperience.DEBUTANT -> 3       // Max 3 sorties
            NiveauExperience.INTERMEDIAIRE -> 5  // Max 5 sorties
            NiveauExperience.AVANCE -> 7         // Max 7 sorties
        }

        // On ne peut pas planifier plus de jours que l'utilisateur n'en a donné
        val targetSessionsPerWeek = maxSessionsAllowed.coerceAtMost(userAvailableDays.size)


        // 3. Génération semaine par semaine
        for (week in 0 until weeksDuration) {
            val startOfWeek = objectif.dateDeDebut.plusWeeks(week.toLong())

            // Logique d'affûtage (Tapering) : les 2 dernières semaines sont plus calmes
            val isTapering = week >= weeksDuration - 2

            // Facteur de progression (0.5 -> 1.0 -> 0.4)
            val progressionFactor = calculateProgression(week, weeksDuration, isTapering)

            activities.addAll(
                generateWeeklySessions(
                    startOfWeek = startOfWeek,
                    availableDays = userAvailableDays,
                    targetSessionCount = targetSessionsPerWeek,
                    level = user.niveauExperience,
                    volumeFactor = progressionFactor,
                    objectifId = objectif.id,
                    weekNumber = week + 1
                )
            )
        }

        // 4. L'épreuve finale
        activities.add(createRaceDayActivity(objectif, user))

        return activities
    }

    private fun calculateProgression(week: Int, totalWeeks: Int, isTapering: Boolean): Double {
        if (isTapering) {
            return if (week == totalWeeks - 2) 0.60 else 0.40 // Chute du volume avant la course
        }
        // Progression linéaire de 50% à 100% du volume max
        val effectiveWeeks = (totalWeeks - 2).coerceAtLeast(1)
        return 0.5 + (0.5 * (week.toDouble() / effectiveWeeks))
    }

    private fun generateWeeklySessions(
        startOfWeek: LocalDate,
        availableDays: List<DayOfWeek>,
        targetSessionCount: Int,
        level: NiveauExperience,
        volumeFactor: Double,
        objectifId: Long,
        weekNumber: Int
    ): List<Activite> {
        val sessions = mutableListOf<Activite>()

        // --- SÉLECTION DES JOURS ---
        // On sélectionne les 'targetSessionCount' meilleurs jours parmi ceux dispos
        val selectedDays = selectOptimalDays(availableDays, targetSessionCount)
        val daysCount = selectedDays.size

        // --- Paramètres selon niveau ---
        val maxLongRunDistance = when (level) {
            NiveauExperience.DEBUTANT -> 22.0
            NiveauExperience.INTERMEDIAIRE -> 28.0
            NiveauExperience.AVANCE -> 34.0
        }

        // 1. La Sortie Longue (SL) - Priorité Absolue - Dernier jour dispo
        val longRunDay = availableDays.last()
        val longRunDate = startOfWeek.with(TemporalAdjusters.nextOrSame(longRunDay))
        val currentLongRunDist = (maxLongRunDistance * volumeFactor).coerceAtLeast(8.0)

        sessions.add(Activite(
            objectifId = objectifId,
            nom = "S$weekNumber - Sortie Longue",
            description = "Endurance fondamentale. Pilier de la prépa marathon.",
            date = longRunDate,
            distanceEffectuee = (currentLongRunDist * 100).roundToInt() / 100.0,
            tempsEffectue = Duration.ofMinutes((currentLongRunDist * getPacePerKm(level) * 1.15).toLong()),
            typeActivite = TypeObjectif.COURSE,
            niveau = level
        ))

        if (daysCount == 1) return sessions

        // 2. La Séance Qualité (VMA/Seuil) - Premier jour dispo
        val intervalDay = availableDays.first()
        val intervalDate = startOfWeek.with(TemporalAdjusters.nextOrSame(intervalDay))
        val intervalDist = 6.0 + (4.0 * volumeFactor) // De 6 à 10km

        sessions.add(Activite(
            objectifId = objectifId,
            nom = "S$weekNumber - Fractionné/Seuil",
            description = "Travail d'allure spécifique.",
            date = intervalDate,
            distanceEffectuee = (intervalDist * 100).roundToInt() / 100.0,
            tempsEffectue = Duration.ofMinutes((intervalDist * getPacePerKm(level)).toLong()), // Allure plus rapide
            typeActivite = TypeObjectif.COURSE,
            niveau = level
        ))

        // 3. Footings de récupération (EF) - Les jours du milieu
        if (daysCount > 2) {
            for (i in 1 until daysCount - 1) {
                val efDay = availableDays[i]
                val efDate = startOfWeek.with(TemporalAdjusters.nextOrSame(efDay))

                sessions.add(Activite(
                    objectifId = objectifId,
                    nom = "S$weekNumber - Footing EF",
                    description = "Récupération active.",
                    date = efDate,
                    distanceEffectuee = 8.0 * volumeFactor,
                    tempsEffectue = Duration.ofMinutes(45),
                    typeActivite = TypeObjectif.COURSE,
                    niveau = level
                ))
            }
        }

        return sessions
    }


    /**
     * Algorithme pour choisir les meilleurs jours d'entraînement parmi ceux disponibles.
     * Priorités :
     * 1. Le Week-end pour la Sortie Longue.
     * 2. Le début/milieu de semaine pour la qualité.
     * 3. Espacement des séances.
     */
    private fun selectOptimalDays(available: List<DayOfWeek>, target: Int): List<DayOfWeek> {
        // Si on a besoin de tous les jours dispos (ou plus), on retourne tout.
        if (available.size <= target) return available

        val selection = mutableSetOf<DayOfWeek>()

        // RÈGLE 1 : Le Week-End est prioritaire pour la Sortie Longue
        // On cherche Dimanche, sinon Samedi, sinon le jour le plus tardif dispo
        val slDay = available.find { it == DayOfWeek.SUNDAY }
            ?: available.find { it == DayOfWeek.SATURDAY }
            ?: available.last()
        selection.add(slDay)

        // Si on a atteint le compte (ex: target=1), on arrête
        if (selection.size == target) return selection.toList().sorted()

        // RÈGLE 2 : Une séance Qualité en début/milieu de semaine (Mardi ou Mercredi idéalement)
        // On cherche Mardi ou Mercredi, sinon le premier jour dispo
        val qualityDay = available.firstOrNull { it == DayOfWeek.TUESDAY || it == DayOfWeek.WEDNESDAY }
            ?: available.first()
        selection.add(qualityDay)

        if (selection.size >= target) return selection.toList().sorted()

        // RÈGLE 3 : Remplissage pour les séances EF restantes
        // On prend les jours disponibles restants
        for (day in available) {
            if (selection.size >= target) break
            if (!selection.contains(day)) {
                selection.add(day)
            }
        }

        return selection.toList().sorted()
    }

    private fun createRaceDayActivity(objectif: Objectif, user: Utilisateur): Activite {
        return Activite(
            objectifId = objectif.id,
            nom = "MARATHON - Jour J",
            description = "Objectif final : 42.195 km",
            date = objectif.dateDeFin,
            distanceEffectuee = 42.195,
            tempsEffectue = Duration.ofHours(4), // Moyenne, à affiner
            typeActivite = TypeObjectif.MARATHON,
            estComplete = false,
            niveau = user.niveauExperience
        )
    }

    private fun getPacePerKm(level: NiveauExperience): Double {
        return when (level) {
            NiveauExperience.DEBUTANT -> 7.0
            NiveauExperience.INTERMEDIAIRE -> 5.5
            NiveauExperience.AVANCE -> 4.5
        }
    }

    // Helper pour la conversion
    private fun JourSemaine.toJavaDayOfWeek(): DayOfWeek {
        return when (this) {
            JourSemaine.LUNDI -> DayOfWeek.MONDAY
            JourSemaine.MARDI -> DayOfWeek.TUESDAY
            JourSemaine.MERCREDI -> DayOfWeek.WEDNESDAY
            JourSemaine.JEUDI -> DayOfWeek.THURSDAY
            JourSemaine.VENDREDI -> DayOfWeek.FRIDAY
            JourSemaine.SAMEDI -> DayOfWeek.SATURDAY
            JourSemaine.DIMANCHE -> DayOfWeek.SUNDAY
        }
    }
}
