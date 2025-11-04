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
import java.time.DayOfWeek
import java.time.Duration
import com.example.pllrun.util.toDayOfWeek


/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventaireViewModel(private val utilisateurDao: UtilisateurDao, private val objectifDao: ObjectifDao) : ViewModel() {

    /**
     * Inserts the new Utilisateur into database.
     */
    fun addNewUtilisateur(nom: String ="", prenom: String = "", dateDeNaissance: LocalDate? = null, sexe: Sexe = Sexe.NON_SPECIFIE, poids: Double = 0.0, poidsCible: Double=0.0, taille: Int = 0, vma: Double? = 0.0, fcm: Int? = 0, fcr: Int? = 0, niveauExperience: NiveauExperience = NiveauExperience.DEBUTANT, joursEntrainementDisponibles: List<JourSemaine> = emptyList()) {
        val newUtilisateur = getNewUtilisateurEntry( nom = nom,
            prenom =prenom,
            dateDeNaissance = dateDeNaissance,
            sexe = sexe,
            poids = poids,
            poidsCible = poidsCible,
            taille = taille,
            vma = vma,
            fcm = fcm,
            fcr = fcr,
            niveauExperience = niveauExperience,
            joursEntrainementDisponibles = joursEntrainementDisponibles )
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

    fun addNewObjectif(
        objectif: Objectif,
        generateActivities: Boolean = false
    ) {
        if (generateActivities) {
            addNewObjectifAndGenerateActivities(objectif)
        } else {
            viewModelScope.launch { objectifDao.insertObjectif(objectif) }
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

    fun getActivitesForDay( date: LocalDate): Flow<List<Activite>> {
        return objectifDao.getActivitesForDay(date)

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

    fun recalculateObjectifProgress(objectifId: Long?) {
        // Si l'activité n'est liée à aucun objectif, on ne fait rien.
        if (objectifId == null) return

        viewModelScope.launch {
            // Récupère le nombre total d'activités et le nombre d'activités complétées
            val totalActivites = objectifDao.countTotalActivitesForObjectif(objectifId)
            val completedActivites = objectifDao.countCompletedActivitesForObjectif(objectifId)

            // Évite la division par zéro si un objectif n'a pas encore d'activités
            if (totalActivites > 0) {
                // Calcule le ratio d'activités complétées (une valeur entre 0.0 et 1.0)
                val completionRatio = completedActivites.toDouble() / totalActivites.toDouble()

                // Applique ce ratio à la progression maximale de 80%
                val newProgress = completionRatio * 80.0

                // Met à jour la base de données avec le nouveau taux de progression
                objectifDao.updateObjectifProgress(objectifId, newProgress)
            }
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

    fun getObjectifById(objectifId: Long): Flow<Objectif> {
        return objectifDao.getObjectifById(objectifId)
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
        poidsCible: Double,
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
            poidsCible = poidsCible,
            taille = taille,
            vma = vma,
            fcm = fcm,
            fcr = fcr,
            niveauExperience = niveauExperience,
            joursEntrainementDisponibles = joursEntrainementDisponibles,
        )
    }
    fun addNewObjectifAndGenerateActivities(obj: Objectif) {
        viewModelScope.launch {
            // 1) Insère l’objectif et récupère son id
            val objectifId = objectifDao.insertObjectif(obj)

            // 2) Récupère l’utilisateur pour connaître ses jours d’entraînement
            val user = utilisateurDao.getUtilisateurNow(obj.utilisateurId)
                ?: return@launch

            val trainingDays: Set<DayOfWeek> =
                if (user.joursEntrainementDisponibles.isNotEmpty())
                    user.joursEntrainementDisponibles.map { it.toDayOfWeek() }.toSet()
                else emptySet()

            // 3) Liste des dates entre début et fin sur les jours choisis
            val dates = enumerateDates(obj.dateDeDebut, obj.dateDeFin, trainingDays)

            // 4) Crée et insère les Activite “placeholder”
            for (date in dates) {
                val (nom, desc) = defaultLabelFor(date.dayOfWeek, obj.type)
                val activite = Activite(
                    id = 0,
                    objectifId = objectifId,
                    nom = nom,
                    description = desc,
                    date = date,
                    distanceEffectuee = 0.0,
                    tempsEffectue = Duration.ZERO,
                    typeActivite = obj.type,
                    estComplete = false,
                    niveau = obj.niveau
                )
                objectifDao.insertActivite(activite)
            }

            recalculateObjectifProgress(objectifId)
        }
    }

    // Helpers privés à coller dans le ViewModel
    private fun enumerateDates(
        start: LocalDate,
        end: LocalDate,
        allowed: Set<DayOfWeek>
    ): List<LocalDate> {
        if (start.isAfter(end) || allowed.isEmpty()) return emptyList()
        val out = mutableListOf<LocalDate>()
        var d = start
        while (!d.isAfter(end)) {
            if (d.dayOfWeek in allowed) out += d
            d = d.plusDays(1)
        }
        return out
    }

    private fun defaultLabelFor(
        dow: DayOfWeek,
        type: com.example.pllrun.Classes.TypeObjectif
    ): Pair<String, String> = when (dow) {
        DayOfWeek.SUNDAY   -> "Sortie longue"  to "Endurance Z2 ; adaptée à l’objectif ${type.libelle}"
        DayOfWeek.TUESDAY  -> "Séance qualité" to "Tempo/Intervalles léger selon niveau"
        else               -> "Footing"        to "Z1–Z2 ; mobilité légère"
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