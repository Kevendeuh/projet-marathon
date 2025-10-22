package com.example.pllrun.Classes

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM Objectif WHERE id = :objectifId")
    fun getObjectifById(objectifId: Long): Flow<Objectif>

    // Récupère toutes les activités pour un objectif donné
    @Query("SELECT * FROM activite WHERE objectifId = :objectifId ORDER BY date DESC")
    fun getActivitesForObjectif(objectifId: Long): Flow<List<Activite>>

    @Query("SELECT * FROM Objectif WHERE utilisateurId = :utilisateurId")
    fun getObjectifsForUtilisateur(utilisateurId: Long): Flow<List<Objectif>>


}
