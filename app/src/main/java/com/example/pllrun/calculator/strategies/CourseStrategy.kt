package com.example.pllrun.calculator.strategies

import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.Utilisateur

class CourseStrategy : IPlanStrategy {
    override fun generate(objectif: Objectif, user: Utilisateur): List<Activite> {
        // TODO: Implémenter la logique pour un 10km ou Semi
        return emptyList()
    }
}

class CardioStrategy : IPlanStrategy {
    override fun generate(objectif: Objectif, user: Utilisateur): List<Activite> {
        // TODO: Implémenter la logique Cardio
        return emptyList()
    }
}

class DefaultStrategy : IPlanStrategy {
    override fun generate(objectif: Objectif, user: Utilisateur): List<Activite> {
        // Pas de génération auto pour Etirements ou Autre pour l'instant
        return emptyList()
    }
}
