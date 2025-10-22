package com.example.pllrun.Classes

import java.time.LocalDate

/**
 * Façade simple pour générer un plan à partir de vos entités existantes.
 *
 * Utilisation :
 *   val params = PlannerService.defaultParamsFor(utilisateur, LocalDate.now().plusWeeks(16))
 *   val plan = PlannerService.generatePlan(utilisateur, params)
 */
object PlannerService {

    private val generator: PlanGenerator by lazy { PlanGenerator() }

    /** Génère le plan complet (training + nutrition + sleep) */
    fun generatePlan(utilisateur: Utilisateur, params: UserParams): FullPlan {
        return generator.generate(utilisateur, params)
    }

    /**
     * Paramètres par défaut raisonnables selon le niveau de l'utilisateur,
     * pour accélérer l'intégration côté UI.
     */
    fun defaultParamsFor(utilisateur: Utilisateur, targetDate: LocalDate): UserParams {
        val suggestedDays = when (utilisateur.niveauExperience) {
            NiveauExperience.DEBUTANT -> 4
            NiveauExperience.INTERMEDIAIRE -> 4
            NiveauExperience.AVANCE -> 5
        }.coerceIn(3, 6)

        return UserParams(
            weightGoal = WeightGoal.STABLE,            // à ajuster: LOSE / GAIN si objectif de poids
            trainingDaysPerWeek = suggestedDays,       // borne 3..6
            equipmentAccess = EquipmentAccess.NONE,    // ou BASIC si du matériel est dispo
            goalType = GoalType.MARATHON,              // autres objectifs à venir
            targetDate = targetDate
        )
    }
}
