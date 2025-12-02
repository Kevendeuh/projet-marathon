package com.example.pllrun.calculator

import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.TypeObjectif
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.calculator.strategies.*

/**
 * Générateur central de planning.
 * Dispatch la demande vers la stratégie appropriée selon le type d'objectif.
 */
object PlannerGenerator {

    /**
     * Point d'entrée unique pour générer un plan d'entraînement.
     * @param objectif L'objectif contenant les dates et le type.
     * @param user Les données physiologiques et disponibilités de l'utilisateur.
     * @return Une liste d'activités planifiées prêtes à être insérées en BDD.
     */
    fun generatePlan(objectif: Objectif, user: Utilisateur): List<Activite> {

        // 1. Sélection de la stratégie
        val strategy: IPlanStrategy = when (objectif.type) {
            TypeObjectif.MARATHON -> MarathonStrategy()
            TypeObjectif.COURSE -> CourseStrategy()
            TypeObjectif.CARDIO -> CardioStrategy()
            TypeObjectif.ETIREMENT,
            TypeObjectif.AUTRE -> DefaultStrategy()
        }

        // 2. Exécution de la génération
        return strategy.generate(objectif, user)
    }
}
