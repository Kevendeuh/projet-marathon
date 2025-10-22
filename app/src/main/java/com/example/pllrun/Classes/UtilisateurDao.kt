package com.example.pllrun.Classes

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
    fun getItem(id: Long): Flow<Utilisateur>
    @Query("SELECT * from utilisateur")
    fun getAllUtilisateurs(): Flow<List<Utilisateur>>

}