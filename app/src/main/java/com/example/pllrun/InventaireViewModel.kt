package com.example.pllrun


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pllrun.Classes.Activite
import com.example.pllrun.Classes.JourSemaine
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Objectif
import com.example.pllrun.Classes.ObjectifDao
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.Classes.UtilisateurDao
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventaireViewModel(private val utilisateurDao: UtilisateurDao, private val objectifDao: ObjectifDao) : ViewModel() {

    /**
     * Inserts the new Utilisateur into database.
     */
    fun addNewUtilisateur(nom: String ="", prenom: String = "", dateDeNaissance: LocalDate? = null, sexe: Sexe = Sexe.NON_SPECIFIE, poids: Double = 0.0, taille: Int = 0, vma: Double? = 0.0, fcm: Int? = 0, fcr: Int? = 0, niveauExperience: NiveauExperience = NiveauExperience.DEBUTANT, joursEntrainementDisponibles: List<JourSemaine> = emptyList()) {
        val newUtilisateur = getNewUtilisateurEntry( nom, prenom, dateDeNaissance, sexe, poids, taille, vma, fcm,fcr,niveauExperience,joursEntrainementDisponibles )
        insertUtilisateur(newUtilisateur)
    }

    fun getAllUtilisateurs(): Flow<List<Utilisateur>> = utilisateurDao.getAllUtilisateurs()
    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertUtilisateur(utilisateur: Utilisateur) {
        viewModelScope.launch {
            utilisateurDao.insertUtilisateur(utilisateur)
        }
    }

    fun addNewObjectif(objectif: Objectif) {
        viewModelScope.launch {
            objectifDao.insertObjectif(objectif)
        }
    }

    fun insertAndGetObjectifId(objectif: Objectif): Deferred<Long> {
        return viewModelScope.async {
            objectifDao.insertObjectif(objectif)
        }
    }

    fun deleteObjectif(objectif: Objectif) {
        viewModelScope.launch {
            objectifDao.deleteObjectif(objectif)
        }
    }

    fun deleteActivite(activite: Activite) {
        viewModelScope.launch {
            objectifDao.deleteActivite(activite)
        }
    }

    fun deleteUtilisateur(utilisateur: Utilisateur) {
        viewModelScope.launch {
            utilisateurDao.deleteUtilisateur(utilisateur)
        }
    }

    /**
     * Insère une nouvelle activité dans la base de données.
     */
    fun addNewActivite(activite: Activite) {
        viewModelScope.launch {
            objectifDao.insertActivite(activite)
        }
    }

    /**
     * Récupère toutes les activités pour un objectif spécifique.
     */
    fun getActivitesForObjectif(objectifId: Long): Flow<List<Activite>> {
        return objectifDao.getActivitesForObjectif(objectifId)
    }

    fun updateUtilisateur(utilisateur: Utilisateur) {
        viewModelScope.launch {
            utilisateurDao.updateUtilisateur(utilisateur)
        }
    }

    fun updateObjectif(objectif: Objectif) {
        viewModelScope.launch {
            objectifDao.updateObjectif(objectif)
        }
    }

    fun updateActivite(activite: Activite) {
        viewModelScope.launch {
            objectifDao.updateActivite(activite)
        }
    }
    fun deleteObjectifAndActivites(objectif: Objectif) {
        viewModelScope.launch {
            objectifDao.deleteObjectifAndItsActivites(objectif.id)
        }
    }

    fun deleteObjectifButKeepActivites(objectif: Objectif) {
        viewModelScope.launch {
            objectifDao.unparentActivitesAndDeleteObjectif(objectif.id)
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
     * Retourne tout les objectifs de l'utilisateur
     */
    fun getObjectifsForUtilisateur(utilisateurId: Long): Flow<List<Objectif>> {
        return objectifDao.getObjectifsForUtilisateur(utilisateurId)
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
        )
    }



}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class InventaireViewModelFactory(private val utilisateurDao: UtilisateurDao, private val objectifDao: ObjectifDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventaireViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventaireViewModel(utilisateurDao, objectifDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}