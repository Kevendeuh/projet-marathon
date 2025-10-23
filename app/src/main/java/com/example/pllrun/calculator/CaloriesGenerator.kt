package com.example.pllrun.calculator

import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import java.time.LocalDate
import java.time.Period

suspend fun calculTotalCalories(utilisateur: Utilisateur,
                                viewModel: InventaireViewModel,
                                additionalActivites: List<Activite> = emptyList()
): Float{
    var totalCalores: Float = 0F
    //RMR (resting metabolic rate)
    var RMR: Float=0F
    val age = Period.between(utilisateur.dateDeNaissance, LocalDate.now()).years
    if(utilisateur.sexe == Sexe.HOMME){
        RMR = ( 9.65F * utilisateur.poids.toFloat() )
        + ( 573F * utilisateur.taille*0.01F )
        - ( 5.08F * age ) + 260F
    }

    
    return totalCalores
}