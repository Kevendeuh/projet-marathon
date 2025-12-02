package com.example.pllrun


import android.util.Log
import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.pllrun.Classes.*
import com.example.pllrun.calculator.ApportsNutritionnels
import com.example.pllrun.calculator.PlannerGenerator
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.Duration
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
import com.example.pllrun.util.toDayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class InventaireViewModel(private val utilisateurDao: UtilisateurDao,
                          private val objectifDao: ObjectifDao,
                          private val repository: InventaireRepository)
    : ViewModel() {

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

    // Dans UtilisateurDao.kt
    fun getUtilisateurByIdFlow(utilisateurId: Long): Flow<Utilisateur?>{
        return utilisateurDao.getUtilisateurByIdFlow(utilisateurId)
    }

    fun getUtilisateurById(utilisateurId: Long): LiveData<Utilisateur?>{
        return utilisateurDao.getUtilisateurById(utilisateurId)
    }


    fun getAllUtilisateurs(): LiveData<List<Utilisateur>> = utilisateurDao.getAllUtilisateurs()

    /**
     * Expose le temps de sommeil recommandé sous forme de LiveData pour l'UI.
     */
    fun getRecommendedSleepTime(utilisateurId: Long): LiveData<Long> {
        // asLiveData transforme le Flow en LiveData, gérant tout automatiquement.
        return repository.getRecommendedSleepTimeFlow(utilisateurId).asLiveData()
    }

    /**
     * Expose l'heure de coucher recommandée sous forme de LiveData pour l'UI.
     */
    fun getRecommendedBedtime(utilisateurId: Long): LiveData<LocalTime> {
        return repository.getRecommendedBedtimeFlow(utilisateurId).asLiveData()
    }

    /**
     * Expose les Nutriments recommandées sous forme de LiveData pour l'UI.
     */
    fun getRecommendedNutriments(utilisateurId: Long): LiveData<ApportsNutritionnels> {
        return repository.getRecommendedNutrimentsFlow(utilisateurId).asLiveData()
    }
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
            if (activite.objectifId != null) {
                recalculateObjectifProgress(activite.objectifId)
            }
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
    fun getActivitesForObjectif(objectifId: Long): LiveData<List<Activite>> {
        return objectifDao.getActivitesForObjectif(objectifId)
    }

    fun getActivitesForDay( date: LocalDate): LiveData<List<Activite>> {
        return objectifDao.getActivitesForDay(date)

    }

    fun getFirstUtilisateur(): LiveData<Utilisateur?> {
        return repository.getFirstUtilisateurFlow().asLiveData()
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
            //    on lance le recalcul de la progression de cet objectif.
            if (activite.objectifId != null) {
                recalculateObjectifProgress(activite.objectifId)
            }
        }
    }
    fun deleteObjectifAndActivites(objectifId: Long) {
        viewModelScope.launch {
            objectifDao.deleteObjectifAndItsActivites(objectifId)

        }
    }

    fun deleteObjectifById(objectifId: Long) {
        viewModelScope.launch {
            objectifDao.unparentActivitesAndDeleteObjectif(objectifId)
        }
    }

    fun recalculateObjectifProgress(objectifId: Long?) {
        // Si l'activité n'est liée à aucun objectif, on ne fait rien.
        if (objectifId == null) return

        viewModelScope.launch {
            val objectif = objectifDao.getObjectifByIdOnce(objectifId)
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
    fun getObjectifsForUtilisateur(utilisateurId: Long): LiveData<List<Objectif>> {
        return objectifDao.getObjectifsForUtilisateur(utilisateurId)
    }

    fun getObjectifsForUtilisateurFlow(utilisateurId: Long): Flow<List<Objectif>> {
        return objectifDao.getObjectifsForUtilisateurFlow(utilisateurId)
    }

    fun getObjectifsForUtilisateurAsLiveData(utilisateurId: Long): LiveData<List<Objectif>> {
        return repository.getObjectifsForUtilisateurFlow(utilisateurId).asLiveData()
    }
    fun getObjectifById(objectifId: Long): LiveData<Objectif> {
        return objectifDao.getObjectifById(objectifId)
    }

    fun getAllActivites(): LiveData<List<Activite>> {
        return objectifDao.getAllActivites()
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
            imageUri = imageUri,
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
        viewModelScope.launch(Dispatchers.IO) {
            // 1) Insère l’objectif et récupère son id
            val objectifId = objectifDao.insertObjectif(obj)

            // On crée une nouvelle instance de l'objectif avec le bon ID
            val savedObjectif = obj.copy(id = objectifId)

            // 2) Récupère l’utilisateur
            val user = utilisateurDao.getUtilisateurNow(savedObjectif.utilisateurId)
                ?: return@launch

            // 2. APPEL UNIQUE AU GENERATEUR
            // Peu importe si c'est Marathon, Cardio ou autre, le PlannerGenerator gère.
            val activities = PlannerGenerator.generatePlan(savedObjectif, user)

            // 3. Insérer les activités générées
            if (activities.isNotEmpty()) {
                activities.forEach { act ->
                    addNewActivite(act)
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

    // ---------------------------------------------------------
    // GESTION DES DÉTAILS DE COURSE (CourseActivite)
    // ---------------------------------------------------------

    /**
     * Insère uniquement les détails de course (si l'activité existe déjà).
     */
    fun insertCourseActivite(courseActivite: CourseActivite) {
        viewModelScope.launch {
            objectifDao.insertCourseActivite(courseActivite)
        }
    }

    /**
     * Transaction complète : Crée l'Activité parent ET les détails CourseActivite en une fois.
     * Recalcule ensuite la progression de l'objectif associé.
     */
    fun addNewActiviteWithCourseDetails(activite: Activite, courseDetails: CourseActivite) {
        viewModelScope.launch {
            // Appel de la transaction dans le DAO
            objectifDao.insertActiviteWithCourseDetails(activite, courseDetails)

            // Mise à jour de la progression de l'objectif parent
            if (activite.objectifId != null) {
                recalculateObjectifProgress(activite.objectifId)
            }
        }
    }

    fun updateCourseActivite(courseActivite: CourseActivite) {
        viewModelScope.launch {
            objectifDao.updateCourseActivite(courseActivite)
        }
    }

    fun deleteCourseActivite(courseActivite: CourseActivite) {
        viewModelScope.launch {
            objectifDao.deleteCourseActivite(courseActivite)
        }
    }

    /**
     * Récupère les détails de course pour une activité donnée (LiveData pour XML/ObserveAsState).
     */
    fun getCourseActiviteByActiviteId(activiteId: Long): LiveData<CourseActivite?> {
        return objectifDao.getCourseActiviteByActiviteId(activiteId)
    }

    /**
     * Récupère les détails de course pour une activité donnée (Flow pour Compose).
     */
    fun getCourseActiviteByActiviteIdFlow(activiteId: Long): Flow<CourseActivite?> {
        return objectifDao.getCourseActiviteByActiviteIdFlow(activiteId)
    }

    /**
     * Récupération unique (Suspend) pour utilisation dans la logique métier.
     */
    suspend fun getCourseActiviteByActiviteIdOnce(activiteId: Long): CourseActivite? {
        return objectifDao.getCourseActiviteByActiviteIdOnce(activiteId)
    }

    /**
     * Récupère la liste de tous les détails de course liés à un objectif spécifique.
     */
    fun getAllCourseDetailsForObjectif(objectifId: Long): Flow<List<CourseActivite>> {
        return objectifDao.getAllCourseDetailsForObjectif(objectifId)
    }

    // 1. LISTE COMPLÈTE (StateFlow)
    // Cette liste se mettra à jour automatiquement dès qu'une donnée arrive de la montre
    val bpmHistory: StateFlow<List<HeartRateMeasurement>> = getBpmForSpecificDay( LocalDate.now())
        // 1. On intercepte les erreurs pour éviter le crash "NoSuchElementException"
        .catch { e ->
            Log.e("InventaireViewModel", "Erreur lors du chargement BPM", e)
            emit(emptyList())
        }
        // 2. On s'assure que tout le travail DB se fait en IO
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. INSERTION (Appelé par le Service ou pour test)
    fun saveHeartRate(bpm: Int, timestamp: Long) {
        viewModelScope.launch {
            val measurement = HeartRateMeasurement(bpm = bpm, timestamp = timestamp)
            repository.insertBpm(measurement)
        }
    }

    // 3. RÉCUPÉRER PAR JOUR (Helper avec LocalDate)
    // Prend une date simple (ex: 2025-11-28) et calcule les timestamps start/end automatiquement
    fun getBpmForSpecificDay(date: LocalDate): Flow<List<HeartRateMeasurement>> {
        val zoneId = ZoneId.systemDefault()

        // Début de la journée (00:00:00)
        val startTimestamp = date.atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Fin de la journée (23:59:59.999)
        val endTimestamp = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        return repository.getBpmMeasurementsForDay(startTimestamp, endTimestamp)
    }

    // 4. SUPPRESSION
    fun clearAllHeartRateData() {
        viewModelScope.launch {
            repository.deleteAllBpm()
        }
    }


}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class InventaireViewModelFactory(private val utilisateurDao: UtilisateurDao,
                                 private val objectifDao: ObjectifDao,
                                 private val InventaireRepository: InventaireRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventaireViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventaireViewModel(utilisateurDao, objectifDao, InventaireRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



