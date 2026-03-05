package com.example.pllrun.calculator.strategies

import com.example.pllrun.Classes.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

class MarathonStrategy : IPlanStrategy {

    // On retourne une liste de paires (Activite, CourseActivite)
    override fun generate(objectif: Objectif, user: Utilisateur): List<Pair<Activite, CourseActivite?>> {
        val activitiesAndDetails = mutableListOf<Pair<Activite, CourseActivite?>>()

        val weeksDuration = ChronoUnit.WEEKS.between(objectif.dateDeDebut, objectif.dateDeFin).toInt()
        if (weeksDuration < 1) return emptyList()

        val userAvailableDays = user.joursEntrainementDisponibles.map { it.toJavaDayOfWeek() }.sorted()
        if (userAvailableDays.isEmpty()) return emptyList()

        val maxSessionsAllowed = when (user.niveauExperience) {
            NiveauExperience.DEBUTANT -> 3
            NiveauExperience.INTERMEDIAIRE -> 5
            NiveauExperience.AVANCE -> 7
        }

        val targetSessionsPerWeek = maxSessionsAllowed.coerceAtMost(userAvailableDays.size)

        for (week in 0 until weeksDuration) {
            val startOfWeek = objectif.dateDeDebut.plusWeeks(week.toLong())
            val isTapering = week >= weeksDuration - 2
            val progressionFactor = calculateProgression(week, weeksDuration, isTapering)

            activitiesAndDetails.addAll(
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

        activitiesAndDetails.add(createRaceDayActivity(objectif, user))

        return activitiesAndDetails
    }

    private fun calculateProgression(week: Int, totalWeeks: Int, isTapering: Boolean): Double {
        if (isTapering) {
            return if (week == totalWeeks - 2) 0.60 else 0.40
        }
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
    ): List<Pair<Activite, CourseActivite?>> {
        val sessions = mutableListOf<Pair<Activite, CourseActivite?>>()

        val selectedDays = selectOptimalDays(availableDays, targetSessionCount)
        val daysCount = selectedDays.size

        val maxLongRunDistance = when (level) {
            NiveauExperience.DEBUTANT -> 22.0
            NiveauExperience.INTERMEDIAIRE -> 28.0
            NiveauExperience.AVANCE -> 34.0
        }

        // 1. Sortie Longue
        val longRunDay = availableDays.last()
        val longRunDate = startOfWeek.with(TemporalAdjusters.nextOrSame(longRunDay))
        val currentLongRunDist = (maxLongRunDistance * volumeFactor).coerceAtLeast(8.0)

        val slActivite = Activite(
            objectifId = objectifId,
            nom = "S$weekNumber - Sortie Longue",
            description = "Endurance fondamentale. Pilier de la prépa marathon.",
            date = longRunDate,
            tempsEffectue = Duration.ofMinutes((currentLongRunDist * getPacePerKm(level) * 1.15).toLong()),
            typeActivite = TypeObjectif.COURSE,
            niveau = level
        )
        val slCourseDetails = CourseActivite(
            activiteId = 0, // Sera mis à jour par Room lors de l'insertion
            distanceEffectuee = (currentLongRunDist * 100).roundToInt() / 100.0,
            vitesseMoyenne = null, vitesseMax = null, bpmMoyen = null, bpmMax = null
        )
        sessions.add(Pair(slActivite, slCourseDetails))

        if (daysCount == 1) return sessions

        // 2. Séance Qualité
        val intervalDay = availableDays.first()
        val intervalDate = startOfWeek.with(TemporalAdjusters.nextOrSame(intervalDay))
        val intervalDist = 6.0 + (4.0 * volumeFactor)

        val qualiteActivite = Activite(
            objectifId = objectifId,
            nom = "S$weekNumber - Fractionné/Seuil",
            description = "Travail d'allure spécifique.",
            date = intervalDate,
            tempsEffectue = Duration.ofMinutes((intervalDist * getPacePerKm(level)).toLong()),
            typeActivite = TypeObjectif.COURSE,
            niveau = level
        )
        val qualiteCourseDetails = CourseActivite(
            activiteId = 0,
            distanceEffectuee = (intervalDist * 100).roundToInt() / 100.0,
            vitesseMoyenne = null, vitesseMax = null, bpmMoyen = null, bpmMax = null
        )
        sessions.add(Pair(qualiteActivite, qualiteCourseDetails))

        // 3. Footings EF
        if (daysCount > 2) {
            for (i in 1 until daysCount - 1) {
                val efDay = availableDays[i]
                val efDate = startOfWeek.with(TemporalAdjusters.nextOrSame(efDay))
                val efDist = 8.0 * volumeFactor

                val efActivite = Activite(
                    objectifId = objectifId,
                    nom = "S$weekNumber - Footing EF",
                    description = "Récupération active.",
                    date = efDate,
                    tempsEffectue = Duration.ofMinutes(45),
                    typeActivite = TypeObjectif.COURSE,
                    niveau = level
                )
                val efCourseDetails = CourseActivite(
                    activiteId = 0,
                    distanceEffectuee = (efDist * 100).roundToInt() / 100.0,
                    vitesseMoyenne = null, vitesseMax = null, bpmMoyen = null, bpmMax = null
                )
                sessions.add(Pair(efActivite, efCourseDetails))
            }
        }

        return sessions
    }

    private fun selectOptimalDays(available: List<DayOfWeek>, target: Int): List<DayOfWeek> {
        if (available.size <= target) return available
        val selection = mutableSetOf<DayOfWeek>()

        val slDay = available.find { it == DayOfWeek.SUNDAY }
            ?: available.find { it == DayOfWeek.SATURDAY }
            ?: available.last()
        selection.add(slDay)

        if (selection.size == target) return selection.toList().sorted()

        val qualityDay = available.firstOrNull { it == DayOfWeek.TUESDAY || it == DayOfWeek.WEDNESDAY }
            ?: available.first()
        selection.add(qualityDay)

        if (selection.size >= target) return selection.toList().sorted()

        for (day in available) {
            if (selection.size >= target) break
            if (!selection.contains(day)) {
                selection.add(day)
            }
        }

        return selection.toList().sorted()
    }

    private fun createRaceDayActivity(objectif: Objectif, user: Utilisateur): Pair<Activite, CourseActivite?> {
        val raceActivite = Activite(
            objectifId = objectif.id,
            nom = "MARATHON - Jour J",
            description = "Objectif final : 42.195 km",
            date = objectif.dateDeFin,
            tempsEffectue = Duration.ofHours(4),
            typeActivite = TypeObjectif.MARATHON,
            estComplete = false,
            niveau = user.niveauExperience
        )
        val raceCourseDetails = CourseActivite(
            activiteId = 0,
            distanceEffectuee = 42.195,
            vitesseMoyenne = null, vitesseMax = null, bpmMoyen = null, bpmMax = null
        )
        return Pair(raceActivite, raceCourseDetails)
    }

    private fun getPacePerKm(level: NiveauExperience): Double {
        return when (level) {
            NiveauExperience.DEBUTANT -> 7.0
            NiveauExperience.INTERMEDIAIRE -> 5.5
            NiveauExperience.AVANCE -> 4.5
        }
    }

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