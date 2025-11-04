package com.example.pllrun.Classes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilisateurDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUtilisateur(utilisateur: Utilisateur)

    @Update
    suspend fun updateUtilisateur(utilisateur: Utilisateur)

    @Delete
    suspend fun deleteUtilisateur(utilisateur: Utilisateur)

    @Query("SELECT * from utilisateur WHERE id = :id")
    fun getItem(id: Long): LiveData<Utilisateur>
    @Query("SELECT * from utilisateur")
    fun getAllUtilisateurs(): LiveData<List<Utilisateur>>

    @Query("SELECT * FROM Utilisateur LIMIT 1")
    fun getFirstUtilisateur(): Flow<Utilisateur?>


    @Query("SELECT * from utilisateur WHERE id = :id")
    fun getUtilisateurByIdFlow(id: Long): Flow<Utilisateur?>


    @Query("SELECT * from utilisateur WHERE id = :id LIMIT 1")
    suspend fun getUtilisateurNow(id: Long): Utilisateur?
}