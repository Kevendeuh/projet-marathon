package com.example.pllrun.util

import com.example.pllrun.Classes.JourSemaine
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.TypeObjectif
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

fun JourSemaine.toDayOfWeek(): DayOfWeek = when (this) {
    JourSemaine.LUNDI     -> DayOfWeek.MONDAY
    JourSemaine.MARDI     -> DayOfWeek.TUESDAY
    JourSemaine.MERCREDI  -> DayOfWeek.WEDNESDAY
    JourSemaine.JEUDI     -> DayOfWeek.THURSDAY
    JourSemaine.VENDREDI  -> DayOfWeek.FRIDAY
    JourSemaine.SAMEDI    -> DayOfWeek.SATURDAY
    JourSemaine.DIMANCHE  -> DayOfWeek.SUNDAY
}

fun DayOfWeek.toJourSemaine(): JourSemaine = when (this) {
    DayOfWeek.MONDAY    -> JourSemaine.LUNDI
    DayOfWeek.TUESDAY   -> JourSemaine.MARDI
    DayOfWeek.WEDNESDAY -> JourSemaine.MERCREDI
    DayOfWeek.THURSDAY  -> JourSemaine.JEUDI
    DayOfWeek.FRIDAY    -> JourSemaine.VENDREDI
    DayOfWeek.SATURDAY  -> JourSemaine.SAMEDI
    DayOfWeek.SUNDAY    -> JourSemaine.DIMANCHE
}

// ==== Aide au planning ====
object TimeMapping {

    data class PlannedSession(
        val date: LocalDate,
        val nom: String,
        val description: String
    )

    fun enumerateDates(
        start: LocalDate,
        end: LocalDate,
        allowed: Set<DayOfWeek>
    ): List<LocalDate> {
        if (start.isAfter(end) || allowed.isEmpty()) return emptyList()
        val out = mutableListOf<LocalDate>()
        var d = start
        while (!d.isAfter(end)) {
            if (d.dayOfWeek in allowed) out += d
            d = d.plusDays(1)
        }
        return out
    }

    /** Regroupe par semaine ISO (clé = (annéeISO, semaineISO)). */
    fun groupByIsoWeek(dates: List<LocalDate>): Map<Pair<Int, Int>, List<LocalDate>> {
        val wf = WeekFields.of(Locale.FRANCE)
        return dates.groupBy { d -> d.get(wf.weekBasedYear()) to d.get(wf.weekOfWeekBasedYear()) }
            .toSortedMap(compareBy<Pair<Int, Int>>({ it.first }, { it.second }))
    }

    /** Durées “type” (minutes) selon niveau: (longue, qualité, footing/totalEasyParSemaine). */
    fun minutesPreset(level: NiveauExperience): Triple<Int, Int, Int> = when (level) {
        NiveauExperience.DEBUTANT      -> Triple(70, 25, 35)
        NiveauExperience.INTERMEDIAIRE -> Triple(95, 35, 50)
        NiveauExperience.AVANCE        -> Triple(115, 45, 60)
    }

    /** Nb de séances qualité par semaine selon niveau et nb de séances planifiées. */
    fun qualityCount(level: NiveauExperience, sessions: Int): Int = when (level) {
        NiveauExperience.DEBUTANT      -> if (sessions >= 3) 1 else 0
        NiveauExperience.INTERMEDIAIRE -> if (sessions >= 2) 1 else 0
        NiveauExperience.AVANCE        -> if (sessions >= 4) 2 else 1
    }

    /** Choisit la sortie longue (dimanche si possible, sinon dernier jour de la semaine). */
    fun pickLongDay(weekDates: List<LocalDate>): LocalDate =
        weekDates.lastOrNull { it.dayOfWeek == DayOfWeek.SUNDAY } ?: weekDates.last()

    /** Choisit les jours “qualité”, en priorisant mardi/jeudi, sans chevaucher la longue. */
    fun pickQualityDays(
        weekDates: List<LocalDate>,
        longDate: LocalDate?,
        count: Int
    ): Set<LocalDate> {
        if (count <= 0) return emptySet()
        val prefs = listOf(
            DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY
        )
        val candidates = weekDates.filter { it != longDate }.sortedBy { it.dayOfWeek.value }
        val ordered = buildList {
            addAll(candidates.filter { it.dayOfWeek in prefs })
            addAll(candidates.filter { it.dayOfWeek !in prefs })
        }
        return ordered.take(count).toSet()
    }

    fun longNote(level: NiveauExperience): String = when (level) {
        NiveauExperience.DEBUTANT      -> "Endurance Z2 ; finir à l’aise"
        NiveauExperience.INTERMEDIAIRE -> "Z2 ; possible 10’ en Z3 en fin de sortie"
        NiveauExperience.AVANCE        -> "Z2 soutenu ; 15’ fin en Z3 si OK"
    }

    fun qualityNote(level: NiveauExperience): String = when (level) {
        NiveauExperience.DEBUTANT      -> "Tempo doux (Z3) continu"
        NiveauExperience.INTERMEDIAIRE -> "Tempo Z3 ou 6×400m Z4"
        NiveauExperience.AVANCE        -> "Intervalles Z4/Z5 (ex: 5×1000m ou 3×2000m)"
    }

    fun easyNote(level: NiveauExperience): String = when (level) {
        NiveauExperience.DEBUTANT      -> "Footing Z1–Z2 ; conversation facile"
        NiveauExperience.INTERMEDIAIRE -> "Z1–Z2 ; relâché"
        NiveauExperience.AVANCE        -> "Z1–Z2 ; éducatifs/strides si envie"
    }

    /** Libellé simple par défaut (fallback), si tu veux l'utiliser ailleurs. */
    fun defaultLabelFor(dow: DayOfWeek, type: TypeObjectif): Pair<String, String> = when (dow) {
        DayOfWeek.SUNDAY   -> "Sortie longue"  to "Endurance Z2 ; adaptée à l’objectif ${type.libelle}"
        DayOfWeek.TUESDAY  -> "Séance qualité" to "Tempo/Intervalles léger selon niveau"
        else               -> "Footing"        to "Z1–Z2 ; mobilité légère"
    }

    /**
     * Construit le plan hebdo marathon pour une semaine donnée (dates déjà filtrées par jours dispo).
     * Retourne une liste de (date, nom, description) prête à convertir en Activite.
     */
    fun buildMarathonWeekPlan(
        weekDates: List<LocalDate>,
        niveau: NiveauExperience
    ): List<PlannedSession> {
        val sessions = weekDates.size
        if (sessions == 0) return emptyList()

        val (longM, qualM, easyTotal) = minutesPreset(niveau)
        val out = mutableListOf<PlannedSession>()

        // Si 1 seule séance dispo → footing “long” mais facile
        if (sessions == 1) {
            val d = weekDates.first()
            val mins = maxOf(40, easyTotal) // base safe
            out += PlannedSession(
                date = d,
                nom = "Footing",
                description = "${easyNote(niveau)} • ~${mins} min"
            )
            return out
        }

        // ≥2 séances: on place une longue + X qualités + le reste en footings
        val longDate = pickLongDay(weekDates)
        out += PlannedSession(
            date = longDate,
            nom = "Sortie longue",
            description = "${longNote(niveau)} • ~${longM} min"
        )

        val qCount = qualityCount(niveau, sessions)
        val qDays = pickQualityDays(weekDates, longDate, qCount)
        qDays.forEach { d ->
            out += PlannedSession(
                date = d,
                nom = "Séance qualité",
                description = "${qualityNote(niveau)} • ~${qualM} min"
            )
        }

        val easyDays = weekDates.toSet() - qDays - setOf(longDate)
        val perEasy = maxOf(30, easyTotal / maxOf(1, easyDays.size))
        easyDays.sorted().forEach { d ->
            out += PlannedSession(
                date = d,
                nom = "Footing",
                description = "${easyNote(niveau)} • ~${perEasy} min"
            )
        }

        return out.sortedBy { it.date }
    }
}
