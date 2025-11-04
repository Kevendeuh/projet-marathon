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

}
