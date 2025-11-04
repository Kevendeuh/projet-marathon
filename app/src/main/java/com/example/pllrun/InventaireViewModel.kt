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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.Duration
import com.example.pllrun.util.toDayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt
import com.example.pllrun.util.TimeMapping.minutesPreset
import com.example.pllrun.util.TimeMapping.qualityCount
import com.example.pllrun.util.TimeMapping.pickLongDay
import com.example.pllrun.util.TimeMapping.pickQualityDays
import com.example.pllrun.util.TimeMapping.longNote
import com.example.pllrun.util.TimeMapping.qualityNote
import com.example.pllrun.util.TimeMapping.easyNote
/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventaireViewModel(private val utilisateurDao: UtilisateurDao, private val objectifDao: ObjectifDao) : ViewModel() {

    /**
     * Inserts the new Utilisateur into database.
     */
    fun addNewUtilisateur(nom: String ="", prenom: String = "",imageUri: String?, dateDeNaissance: LocalDate? = null, sexe: Sexe = Sexe.NON_SPECIFIE, poids: Double = 0.0, poidsCible: Double=0.0, taille: Int = 0, vma: Double? = 0.0, fcm: Int? = 0, fcr: Int? = 0, niveauExperience: NiveauExperience = NiveauExperience.DEBUTANT, joursEntrainementDisponibles: List<JourSemaine> = emptyList()) {
        val newUtilisateur = getNewUtilisateurEntry( nom = nom,
            prenom = prenom,
            imageUri = imageUri,
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

            recalculateObjectifProgress(objectif.id)
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
            val objectif = objectifDao.getObjectifById(objectifId).firstOrNull()
            if (objectif == null) return@launch // Sortir si l'objectif n'existe pas

            // Récupère le nombre total d'activités et le nombre d'activités complétées
            val newProgress: Double
            if (objectif.estValide) {
                // Si l'objectif est validé, forcer la progression à 100%.
                newProgress = 100.0
            }
            else {
                val totalActivites = objectifDao.countTotalActivitesForObjectif(objectifId)
                val completedActivites = objectifDao.countCompletedActivitesForObjectif(objectifId)
                // Évite la division par zéro si un objectif n'a pas encore d'activités
                if (totalActivites > 0) {
                    // Calcule le ratio d'activités complétées (une valeur entre 0.0 et 1.0)
                    val completionRatio = completedActivites.toDouble() / totalActivites.toDouble()

                    // Applique ce ratio à la progression maximale de 80%
                    newProgress = completionRatio * 80.0

                    // Met à jour la base de données avec le nouveau taux de progression
                }
                else{
                    newProgress = 0.0
                }

            }
            objectifDao.updateObjectifProgress(objectifId, newProgress)

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
        imageUri: String?,
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

            // 2) Récupère l’utilisateur (si tu n’as pas getUtilisateurNow, utilise la ligne commentée)
            val user = utilisateurDao.getUtilisateurNow(obj.utilisateurId)
            // val user = utilisateurDao.getItem(obj.utilisateurId).firstOrNull()
                ?: return@launch

            // 3) Jours d’entraînement choisis
            val trainingDays: List<DayOfWeek> =
                if (user.joursEntrainementDisponibles.isNotEmpty())
                    user.joursEntrainementDisponibles.map { it.toDayOfWeek() }.sortedBy { it.value }
                else listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY) // fallback 3j/sem

            // 4) Toutes les dates dans l’intervalle qui tombent sur ces jours
            val dates = enumerateDates(obj.dateDeDebut, obj.dateDeFin, trainingDays.toSet())
            if (dates.isEmpty()) return@launch

            // 5) Groupes par semaine ISO (année + semaine)
            val wf = WeekFields.of(Locale.FRANCE) // ISO
            val byWeek = dates.groupBy { d -> d.get(wf.weekBasedYear()) to d.get(wf.weekOfWeekBasedYear()) }

            // 6) Presets en minutes selon le niveau
            val (baseLong, baseQual, baseEasy) = minutesPreset(user.niveauExperience)

            // 7) Pour chaque semaine, distribuer LONG / QUALITÉ / EASY
            byWeek.toSortedMap(compareBy<Pair<Int, Int>>({ it.first }, { it.second })).forEach { (_, weekDatesUnsorted) ->
                val weekDates = weekDatesUnsorted.sorted()
                val sessions = weekDates.size

                // combien de qualités selon niveau & nb séances
                val qCount = qualityCount(user.niveauExperience, sessions)

                // choisir la journée de sortie longue (dimanche si possible, sinon la dernière)
                val longDate = pickLongDay(weekDates)

                // choisir 0-2 journées qualité (mardi/jeudi si possibles, sinon le plus tôt)
                val qualityDates = pickQualityDays(weekDates, longDate, qCount)

                // crées les activités
                weekDates.forEach { d ->
                    val (label, minutes, note) = when {
                        d == longDate -> Triple("[LONG] Sortie longue", baseLong, longNote(user.niveauExperience))
                        qualityDates.contains(d) -> Triple("[QUALITÉ] Séance", baseQual, qualityNote(user.niveauExperience))
                        else -> Triple("[EASY] Footing", baseEasy, easyNote(user.niveauExperience))
                    }

                    val nom = "$label • ~${minutes} min"
                    val desc = "Niveau: ${user.niveauExperience.libelle} • $note"

                    objectifDao.insertActivite(
                        Activite(
                            id = 0,
                            objectifId = objectifId,
                            nom = nom,
                            description = desc,
                            date = d,
                            distanceEffectuee = 0.0,      // prévu: 0 -> sera rempli après séance
                            tempsEffectue = Duration.ZERO, // prévu: 0 -> sera rempli après séance
                            typeActivite = obj.type,       // MARATHON
                            estComplete = false,
                            niveau = obj.niveau
                        )
                    )
                }
            }

            // 8) Progression basée sur nb d’activités complétées
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