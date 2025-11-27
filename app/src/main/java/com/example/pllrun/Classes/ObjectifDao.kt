package com.example.pllrun.Classes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ObjectifDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectif(objectif: Objectif): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivite(activite: Activite)

    @Update
    suspend fun updateObjectif(objectif: Objectif)

    @Delete
    suspend fun deleteObjectif(objectif: Objectif)

    @Update
    suspend fun updateActivite(activite: Activite)

    @Delete
    suspend fun deleteActivite(activite: Activite)


    @Transaction
    suspend fun deleteObjectifAndItsActivites(objectifId: Long) {
        deleteActivitesByObjectifId(objectifId) // Supprime les activités liées
        deleteObjectifById(objectifId) // Supprime l'objectif

    }

    // 2. Fonction pour dissocier les activités puis supprimer l'objectif
    @Transaction
    suspend fun unparentActivitesAndDeleteObjectif(objectifId: Long) {
        unparentActivitesFromObjectif(objectifId) // Met objectifId à null
        deleteObjectifById(objectifId) // Supprime l'objectif
    }

    @Query("SELECT * FROM Objectif WHERE id = :objectifId")
    fun getObjectifById(objectifId: Long): LiveData<Objectif>

    @Query("SELECT * FROM Objectif WHERE id = :objectifId")
    suspend fun getObjectifByIdOnce(objectifId: Long): Objectif?


    // Récupère toutes les activités pour un objectif donné
    @Query("SELECT * FROM activite WHERE objectifId = :objectifId ORDER BY date DESC")
    fun getActivitesForObjectif(objectifId: Long): LiveData<List<Activite>>

    @Query("SELECT * FROM Objectif WHERE utilisateurId = :utilisateurId")
    fun getObjectifsForUtilisateur(utilisateurId: Long): LiveData<List<Objectif>>

    @Query("SELECT * FROM activite WHERE objectifId = :objectifId ORDER BY date DESC")
    fun getActivitesForObjectifFlow(objectifId: Long): Flow<List<Activite>>

    @Query("SELECT * FROM Objectif WHERE utilisateurId = :utilisateurId")
    fun getObjectifsForUtilisateurFlow(utilisateurId: Long): Flow<List<Objectif>>


    @Query("DELETE FROM Objectif WHERE id = :objectifId")
    suspend fun deleteObjectifById(objectifId: Long)

    @Query("DELETE FROM activite WHERE objectifId = :objectifId")
    suspend fun deleteActivitesByObjectifId(objectifId: Long)

    @Query("UPDATE activite SET objectifId = NULL WHERE objectifId = :objectifId")
    suspend fun unparentActivitesFromObjectif(objectifId: Long)

    @Query("SELECT * FROM activite WHERE date = :date")
    fun getActivitesForDay(date: LocalDate): LiveData<List<Activite>>

    @Query("SELECT COUNT(*) FROM activite WHERE objectifId = :objectifId")
    suspend fun countTotalActivitesForObjectif(objectifId: Long): Int

    /**
     * Compte le nombre d'activités déjà complétées pour un objectif.
     */
    @Query("SELECT COUNT(*) FROM activite WHERE objectifId = :objectifId AND est_complete = 1") // 1 pour 'true' en SQLite
    suspend fun countCompletedActivitesForObjectif(objectifId: Long): Int

    /**
     * Met à jour uniquement le taux de progression d'un objectif spécifique.
     * C'est plus efficace que de mettre à jour l'objet entier.
     */
    @Query("UPDATE Objectif SET taux_de_progression = :newProgress WHERE id = :objectifId")
    suspend fun updateObjectifProgress(objectifId: Long, newProgress: Double)

    @Query("SELECT * FROM Objectif WHERE utilisateurId = :utilisateurId AND est_valide = 1")
    fun getActifObjectifsByUserAsLiveData(utilisateurId: Long): LiveData<List<Objectif>>

    @Query("SELECT * FROM activite")
    fun getAllActivites(): LiveData<List<Activite>>

    // ---------------------------------------------------------
    // GESTION DE L'ENTITÉ CourseActivite
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseActivite(courseActivite: CourseActivite)

    @Update
    suspend fun updateCourseActivite(courseActivite: CourseActivite)

    @Delete
    suspend fun deleteCourseActivite(courseActivite: CourseActivite)

    // Récupération par ID de l'activité parente (LiveData - pour l'UI XML ou observeAsState)
    @Query("SELECT * FROM course_activite WHERE activiteId = :activiteId")
    fun getCourseActiviteByActiviteId(activiteId: Long): LiveData<CourseActivite?>

    // Récupération par ID de l'activité parente (Flow - recommandé pour Compose)
    @Query("SELECT * FROM course_activite WHERE activiteId = :activiteId")
    fun getCourseActiviteByActiviteIdFlow(activiteId: Long): Flow<CourseActivite?>

    // Récupération unique (Suspend - pour la logique métier ou les workers)
    @Query("SELECT * FROM course_activite WHERE activiteId = :activiteId")
    suspend fun getCourseActiviteByActiviteIdOnce(activiteId: Long): CourseActivite?

    // Récupérer toutes les données de courses associées à un objectif (via jointure)
// Utile pour calculer des stats globales sur un objectif de type "Course"
    @Query("""SELECT CA.* FROM course_activite CA INNER JOIN activite A ON CA.activiteId = A.id WHERE A.objectifId = :objectifId""")
    fun getAllCourseDetailsForObjectif(objectifId: Long): Flow<List<CourseActivite>>

    /**
     * TRANSACTION : Insère une activité ET ses détails de course en même temps.
     * 1. Insère l'activité et récupère son nouvel ID.
     * 2. Assigne cet ID à l'objet CourseActivite.
     * 3. Insère le CourseActivite.
     */
    /**
     * TRANSACTION : Insère une activité ET ses détails de course en même temps.
     */
    @Transaction
    suspend fun insertActiviteWithCourseDetails(activite: Activite, courseDetails: CourseActivite) {
        // On insère d'abord l'activité parente et on récupère l'ID généré
        val activiteId = insertActiviteReturnId(activite)

        // On crée une copie des détails avec le bon ID parent
        val detailsWithId = courseDetails.copy(activiteId = activiteId)

        // On insère les détails
        insertCourseActivite(detailsWithId)
    }

    // Helper pour la transaction ci-dessus (si votre insertActivite actuel ne retourne rien)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiviteReturnId(activite: Activite): Long

}
