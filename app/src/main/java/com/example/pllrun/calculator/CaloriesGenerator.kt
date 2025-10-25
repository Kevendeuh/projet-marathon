package com.example.pllrun.calculator

import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.Period

suspend fun calculTotalCalories(utilisateur: Utilisateur,
                                viewModel: InventaireViewModel,
                                additionalActivites: List<Activite> = emptyList()
): Float{
    var totalCalores: Float = 0F
    //RMR (resting metabolic rate)
    var RMR: Float=0F
    val age = utilisateur.dateDeNaissance?.let {
        Period.between(it, LocalDate.now()).years
    } ?: 25 // Âge par défaut si la date est nulle

    when(utilisateur.sexe.name){
        "HOMME" -> {
            RMR = (( 9.65F * utilisateur.poids.toFloat() )
            + ( 5.73F * utilisateur.taille.toFloat() )
            - ( 5.08F * age.toFloat() ) + 260F)
        }
        "FEMME" -> {
            RMR = ((  7.38F * utilisateur.poids.toFloat() )
            + ( 6.07F * utilisateur.taille.toFloat() )
            - (  2.31F * age.toFloat() ) + 43F)
        }
        else -> {RMR = 1500F}
    }

    var facteurPoidsCible = 1.0F
    if (utilisateur.poidsCible > 0.0 && utilisateur.poids > 0.0) {
        // Le ratio est appliqué seulement si le poids cible est défini et valide
        facteurPoidsCible = (utilisateur.poids / utilisateur.poidsCible).toFloat()

        // Plafonnage facteur pour éviter des résultats extrêmes
        if (facteurPoidsCible < 0.7F) facteurPoidsCible = 0.7F // Perte de poids agressive
        if (facteurPoidsCible > 1.3F) facteurPoidsCible = 1.3F // Prise de poids agressive
    }
    var coefficient : Float = 1.2F
    viewModel.getActivitesForDay(LocalDate.now()).first().forEach { activite ->
        coefficient += activite.tempsEffectue.toMinutes()*0.001F * activite.niveau.facteur
    }
    if(coefficient > 2.5F ) {
        coefficient=2.5F
    }

    totalCalores = RMR * coefficient * facteurPoidsCible


    return totalCalores
}