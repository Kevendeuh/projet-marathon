package com.example.pllrun.calculator

import java.time.LocalDate

/**
 * Modèles pour le plan d'entraînement, de nutrition et de sommeil.
 */

/* ---------- Paramètres utilisateur spécifiques au planner ---------- */

enum class WeightGoal { STABLE, LOSE, GAIN }

enum class EquipmentAccess { NONE, BASIC }

/** Pour l’instant seul MARATHON est utilisé. Ajoutez HYROX, SEMI, TRIATHLON plus tard. */
enum class GoalType { MARATHON /* , HYROX, SEMI, TRIATHLON */ }

/**
 * Paramètres complémentaires à vos données Utilisateur pour générer un plan.
 * - trainingDaysPerWeek : 3..6 recommandé pour marathon
 * - targetDate : date objectif (jour du marathon)
 */
data class UserParams(
    val weightGoal: WeightGoal = WeightGoal.STABLE,
    val trainingDaysPerWeek: Int = 4,
    val equipmentAccess: EquipmentAccess = EquipmentAccess.NONE,
    val goalType: GoalType = GoalType.MARATHON,
    val targetDate: LocalDate = LocalDate.now().plusWeeks(16)
)

/* ---------- Entraînement ---------- */

enum class TrainingIntensity {
    EASY,       // footing Z1-Z2
    LONG,       // sortie longue
    TEMPO,      // tempo (seuil)
    INTERVALS,  // intervalles (Z4/Z5)
    RECOVERY,   // footing très léger / marche active
    CROSS,      // cross-training (vélo, rameur…)
    REST        // repos
}

/** Une séance d'entraînement (durée en minutes + notes) */
data class Workout(
    val kind: TrainingIntensity,
    val minutes: Int,
    val notes: String = ""
)

/** Un jour d'entraînement avec une date et sa séance */
data class TrainingDay(
    val date: LocalDate,
    val workout: Workout
)

enum class TrainingPhase { BASE, BUILD, PEAK, TAPER }

/** Un bloc hebdomadaire (semaine indexée 1..N) */
data class WeeklyBlock(
    val weekIndex: Int,
    val phase: TrainingPhase,
    val days: List<TrainingDay>
)

/** Plan d'entraînement complet (sur plusieurs semaines) */
data class TrainingPlan(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val weeks: List<WeeklyBlock>
)

/* ---------- Nutrition ---------- */

/** Cibles nutritionnelles journalières (kcal + macros) */
data class NutritionTargets(
    val kcal: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatsGrams: Int,
    val notes: String = ""
)

/** Ligne de plan nutrition pour un jour donné */
data class DailyNutrition(
    val date: LocalDate,
    val targets: NutritionTargets
)

/* ---------- Sommeil ---------- */

/** Recommandations sommeil pour un jour donné */
data class SleepTargets(
    val minHours: Double,
    val recommendedHours: Double,
    val notes: String = ""
)

/** Ligne de plan sommeil pour un jour donné */
data class DailySleep(
    val date: LocalDate,
    val targets: SleepTargets
)

/* ---------- Agrégat global ---------- */

/** Plan complet : entraînement + nutrition + sommeil */
data class FullPlan(
    val training: TrainingPlan,
    val nutrition: List<DailyNutrition>,
    val sleep: List<DailySleep>
)
