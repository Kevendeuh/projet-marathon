package com.example.pllrun


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pllrun.Classes.JourSemaine
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.Classes.UtilisateurDao
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventoryViewModel(private val utilisateurDao: UtilisateurDao) : ViewModel() {

    /**
     * Inserts the new Utilisateur into database.
     */
    fun addNewUtilisateur(nom: String ="", prenom: String = "", dateDeNaissance: LocalDate? = null, sexe: Sexe = Sexe.NON_SPECIFIE, poids: Double = 0.0, taille: Int = 0, vma: Double? = 0.0, fcm: Int? = 0, fcr: Int? = 0, niveauExperience: NiveauExperience = NiveauExperience.DEBUTANT, joursEntrainementDisponibles: List<JourSemaine> = emptyList(), objectifs: MutableList<Objectif> = mutableListOf()  ) {
        val newUtilisateur = getNewUtilisateurEntry( nom, prenom, dateDeNaissance, sexe, poids, taille, vma, fcm,fcr,niveauExperience,joursEntrainementDisponibles, objectifs)
        insertUtilisateur(newUtilisateur)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertUtilisateur(utilisateur: Utilisateur) {
        viewModelScope.launch {
            utilisateurDao.insert(utilisateur)
        }
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(nom: String ="", prenom: String = "", dateDeNaissance: LocalDate? = null, sexe: Sexe = Sexe.NON_SPECIFIE, poids: Double = 0.0, taille: Int = 0): Boolean {
        if (nom.isBlank() || prenom.isBlank() || poids.isNaN() ) {
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewUtilisateurEntry(

        nom: String,
        prenom: String,
        dateDeNaissance: LocalDate?,
        sexe: Sexe,
        poids: Double,
        taille: Int,
        vma: Double?,
        fcm: Int?,
        fcr: Int?,
        niveauExperience: NiveauExperience,
        joursEntrainementDisponibles: List<JourSemaine>,
        objectifs: MutableList<Objectif>
    ): Utilisateur {
        return Utilisateur(

            nom = nom,
            prenom = prenom,
            dateDeNaissance = dateDeNaissance,
            sexe = sexe,
            poids = poids,
            taille = taille,
            vma = vma,
            fcm = fcm,
            fcr = fcr,
            niveauExperience = niveauExperience,
            joursEntrainementDisponibles = joursEntrainementDisponibles,
            objectifs = objectifs
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class InventoryViewModelFactory(private val utilisateurDao: UtilisateurDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(utilisateurDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}