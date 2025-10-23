package com.example.pllrun.calculator

import com.example.pllrun.Classes.JourSemaine
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Génère un plan complet (entraînement + nutrition + sommeil)
 * à partir de vos entités existantes (Utilisateur, enums) et des UserParams.
 *
 * Utilisation :
 *   val plan = PlannerService.generatePlan(utilisateur, params)
 */
class PlanGenerator(
    private val trainingGenerator: TrainingPlanGenerator = TrainingPlanGenerator(),
    private val nutritionPlanner: NutritionPlanner = NutritionPlanner(),
    private val sleepPlanner: SleepPlanner = SleepPlanner()
) {
    fun generate(utilisateur: Utilisateur, params: UserParams): FullPlan {
        val training = trainingGenerator.build(utilisateur, params)
        val nutrition = nutritionPlanner.planFor(training, utilisateur, params)
        val sleep = sleepPlanner.planFor(training)
        return FullPlan(training, nutrition, sleep)
    }
}

/* ============================================================
 *                 TRAINING PLAN GENERATOR
 * ============================================================
 */
class TrainingPlanGenerator {

    fun build(user: Utilisateur, params: UserParams): TrainingPlan {
        require(params.trainingDaysPerWeek in 3..6) { "3 à 6 jours/sem recommandés pour un marathon." }

        val today = LocalDate.now()
        val totalWeeks = max(8, ChronoUnit.WEEKS.between(today, params.targetDate).toInt())
        val (baseW, buildW, peakW, taperW) = phaseSplit(totalWeeks)

        val startDate = weekStart(today) // Lundi de la semaine courante
        val weeks = mutableListOf<WeeklyBlock>()
        var currentStart = startDate
        var weekIndex = 1

        // Volume de base selon niveau et fréquence
        fun weekMinutesBase(level: NiveauExperience, days: Int): Int = when (level) {
            NiveauExperience.DEBUTANT       -> 150 + (days - 3) * 20   // 150–210 min
            NiveauExperience.INTERMEDIAIRE  -> 210 + (days - 3) * 30   // 210–300 min
            NiveauExperience.AVANCE         -> 300 + (days - 3) * 30   // 300–390 min
        }

        val baseWeekly = weekMinutesBase(user.niveauExperience, params.trainingDaysPerWeek)
        var weeklyMinutes = baseWeekly
        val recoveryEvery = 4

        fun phaseOf(weekIdx: Int): TrainingPhase = when {
            weekIdx <= baseW -> TrainingPhase.BASE
            weekIdx <= baseW + buildW -> TrainingPhase.BUILD
            weekIdx <= baseW + buildW + peakW -> TrainingPhase.PEAK
            else -> TrainingPhase.TAPER
        }

        while (weekIndex <= totalWeeks) {
            val phase = phaseOf(weekIndex)
            val adjusted = when {
                phase == TrainingPhase.TAPER -> (weeklyMinutes * 0.60).roundToInt()
                (weekIndex % recoveryEvery == 0) && phase != TrainingPhase.TAPER -> (weeklyMinutes * 0.80).roundToInt()
                phase == TrainingPhase.BUILD -> (weeklyMinutes * 1.10).roundToInt()
                phase == TrainingPhase.PEAK  -> (weeklyMinutes * 1.15).roundToInt()
                else -> weeklyMinutes
            }
            weeklyMinutes = min(adjusted, baseWeekly * 2) // plafond souple

            val availableDays: Set<DayOfWeek> =
                if (user.joursEntrainementDisponibles.isNotEmpty())
                    user.joursEntrainementDisponibles.map { it.toDayOfWeek() }.toSet()
                else emptySet()

            val weekDays = distributeWeek(
                start = currentStart,
                minutes = weeklyMinutes,
                daysPerWeek = params.trainingDaysPerWeek,
                phase = phase,
                level = user.niveauExperience,
                available = availableDays
            )
            weeks +=WeeklyBlock(weekIndex, phase, weekDays)

            currentStart = currentStart.plusWeeks(1)
            weekIndex += 1
        }

        val endDate = weeks.last().days.last().date
        return TrainingPlan(startDate, endDate, weeks)
    }

    /** Répartition des semaines en phases : Base/Build/Peak/Taper */
    private fun phaseSplit(totalWeeks: Int): Quad {
        val base = max(2, (totalWeeks * 0.40).roundToInt())
        val build = max(2, (totalWeeks * 0.35).roundToInt())
        val peak = max(1, (totalWeeks * 0.15).roundToInt())
        var taper = max(1, totalWeeks - (base + build + peak))
        val sum = base + build + peak + taper
        if (sum != totalWeeks) taper += (totalWeeks - sum)
        return Quad(base, build, peak, taper)
    }

    /** Lundi de la semaine de d */
    private fun weekStart(d: LocalDate): LocalDate {
        // recule jusqu'au lundi
        var x = d
        while (x.dayOfWeek != DayOfWeek.MONDAY) x = x.minusDays(1)
        return x
    }

    /** Date d'un jour de semaine [dow] dans la semaine démarrant au lundi [start] */
    private fun dateOf(start: LocalDate, dow: DayOfWeek): LocalDate {
        val delta = (dow.value - DayOfWeek.MONDAY.value + 7) % 7
        return start.plusDays(delta.toLong())
    }

    /**
     * Construit la semaine : long run ~34%, 1 qualité (TEMPO/INTERVALS) selon phase,
     * le reste en EASY/RECOVERY. Place sur les jours disponibles si fournis.
     */
    private fun distributeWeek(
        start: LocalDate,
        minutes: Int,
        daysPerWeek: Int,
        phase: TrainingPhase,
        level: NiveauExperience,
        available: Set<DayOfWeek>
    ): List<TrainingDay> {
        val longRun = (minutes * 0.34).roundToInt()
        val quality = when (phase) {
            TrainingPhase.BASE  -> if (level == NiveauExperience.DEBUTANT) 0 else (minutes * 0.15).roundToInt()
            TrainingPhase.BUILD -> (minutes * 0.18).roundToInt()
            TrainingPhase.PEAK  -> (minutes * 0.20).roundToInt()
            TrainingPhase.TAPER -> (minutes * 0.10).roundToInt()
        }
        val remaining = (minutes - longRun - quality).coerceAtLeast(0)
        val easySlots = max(0, daysPerWeek - 2) // long + quality + (others)
        val baseEasy = if (easySlots > 0) (remaining / easySlots.toDouble()).roundToInt() else 0

        val ordered = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
        val scheduleOrder = if (available.isNotEmpty()) ordered.filter { it in available } else ordered

        val out = mutableListOf<TrainingDay>()
        var placed = 0

        for (dow in scheduleOrder) {
            val date = dateOf(start, dow)
            if (placed >= daysPerWeek) {
                out +=TrainingDay(
                    date,
                    Workout(TrainingIntensity.REST, 0, "Repos / mobilité 10–15'")
                )
                continue
            }

            val slot = when {
                dow == DayOfWeek.SUNDAY -> Workout(
                    TrainingIntensity.LONG,
                    longRun,
                    "Sortie longue Z2 (derniers 10' en Z3 si avancé)."
                )
                dow == DayOfWeek.TUESDAY && quality > 0 -> {
                    val (kind, note) = if (phase == TrainingPhase.PEAK)
                        TrainingIntensity.INTERVALS to "Intervalles Z4 (ex: 6×800m r:2')"
                    else
                        TrainingIntensity.TEMPO to "Tempo Z3 (ex: 20–30' en continu)"
                    Workout(kind, quality, note)
                }
                else -> Workout(
                    TrainingIntensity.EASY,
                    baseEasy,
                    "Footing Z1–Z2 ; respiration nasale."
                )
            }

            out +=TrainingDay(date, slot)
            if (slot.kind != TrainingIntensity.REST) placed += 1
        }

        // Compléter les jours non listés pour avoir 7 lignes visibles
        val restDays = ordered - scheduleOrder.toSet()
        for (dow in restDays) {
            val date = dateOf(start, dow)
            out +=TrainingDay(date, Workout(TrainingIntensity.REST, 0, "Repos"))
        }

        return out.sortedBy { it.date.dayOfWeek.value }
    }

    private data class Quad(val base: Int, val build: Int, val peak: Int, val taper: Int)
}

/* ============================================================
 *                    NUTRITION PLANNER
 * ============================================================
 */
class NutritionPlanner {

    fun planFor(training: TrainingPlan, user: Utilisateur, params: UserParams): List<DailyNutrition> {
        val baseTdee = tdee(user, params)
        val (deltaKcal, proteinPerKg) = when (params.weightGoal) {
            WeightGoal.LOSE   -> -500 to 2.0
            WeightGoal.GAIN   -> +300 to 1.8
            WeightGoal.STABLE -> 0    to 1.6
        }
        val fatPerKg = 0.8
        val baseKcal = (baseTdee + deltaKcal).coerceAtLeast(1400)

        return training.weeks.flatMap { week ->
            week.days.map { day ->
                val isLong = day.workout.kind == TrainingIntensity.LONG
                val isQuality = day.workout.kind == TrainingIntensity.TEMPO || day.workout.kind == TrainingIntensity.INTERVALS

                val kcal = when {
                    isLong    -> (baseKcal * 1.20).roundToInt()
                    isQuality -> (baseKcal * 1.10).roundToInt()
                    else      -> baseKcal
                }

                val p = (user.poids * proteinPerKg).roundToInt()     // g
                val f = (user.poids * fatPerKg).roundToInt()         // g
                val kcalPF = kcal - (p * 4 + f * 9)
                val c = max(0, kcalPF / 4)                           // g

                DailyNutrition(
                    date = day.date,
                    targets = NutritionTargets(
                        kcal = kcal,
                        proteinGrams = p,
                        carbsGrams = c,
                        fatsGrams = f,
                        notes = if (isLong) "Augmenter les glucides la veille + petit dej riche en glucides" else ""
                    )
                )
            }
        }
    }

    /** TDEE = BMR (Mifflin-St Jeor) × facteur d'activité (proxy = jours/sem) */
    private fun tdee(user: Utilisateur, params: UserParams): Int {
        val age = user.dateDeNaissance?.let { Period.between(it, LocalDate.now()).years } ?: 30
        val base = 10.0 * user.poids + 6.25 * user.taille + (-5.0 * age)
        val bmr = when (user.sexe) {
            Sexe.HOMME -> base + 5.0
            Sexe.FEMME -> base - 161.0
            else       -> base - 78.0 // valeur médiane simple pour AUTRE/NON_SPECIFIE
        }
        val factor = when (params.trainingDaysPerWeek) {
            in 0..2 -> 1.20
            in 3..4 -> 1.55
            in 5..6 -> 1.725
            else    -> 1.90
        }
        return (bmr * factor).roundToInt()
    }
}

/* ============================================================
 *                      SLEEP PLANNER
 * ============================================================
 */
class SleepPlanner {
    fun planFor(training: TrainingPlan): List<DailySleep> {
        return training.weeks.flatMap { week ->
            val heavyWeek = week.phase == TrainingPhase.PEAK || week.phase == TrainingPhase.BUILD
            val baseMin = 7.5
            val baseRec = 8.0
            val bump = if (heavyWeek) 0.5 else 0.25

            week.days.map { day ->
                DailySleep(
                    date = day.date,
                    targets = SleepTargets(
                        minHours = baseMin + if (day.workout.kind == TrainingIntensity.LONG) 0.25 else 0.0,
                        recommendedHours = baseRec + bump,
                        notes = if (day.workout.kind == TrainingIntensity.LONG) "Sieste 20' si possible ; coucher +30’" else "Hygiène de sommeil régulière"
                    )
                )
            }
        }
    }
}

/* ============================================================
 *                    HELPERS DE MAPPING
 * ============================================================
 */

private fun JourSemaine.toDayOfWeek(): DayOfWeek = when (this) {
    JourSemaine.LUNDI     -> DayOfWeek.MONDAY
    JourSemaine.MARDI     -> DayOfWeek.TUESDAY
    JourSemaine.MERCREDI  -> DayOfWeek.WEDNESDAY
    JourSemaine.JEUDI     -> DayOfWeek.THURSDAY
    JourSemaine.VENDREDI  -> DayOfWeek.FRIDAY
    JourSemaine.SAMEDI    -> DayOfWeek.SATURDAY
    JourSemaine.DIMANCHE  -> DayOfWeek.SUNDAY
}
