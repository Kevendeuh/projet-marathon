package com.example.pllrun.calculator.strategies

import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.Utilisateur

/**
 * Interface définissant la stratégie de génération de plan.
 */
interface IPlanStrategy {
    fun generate(objectif: Objectif, user: Utilisateur): List<Activite>
}
