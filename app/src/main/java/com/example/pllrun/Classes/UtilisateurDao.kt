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
    suspend fun insert(utilisateur: Utilisateur)

    @Update
    suspend fun update(utilisateur: Utilisateur)

    @Delete
    suspend fun delete(utilisateur: Utilisateur)

    @Query("SELECT * from utilisateur WHERE id = :id")
    fun getItem(id: Long): Flow<Utilisateur>
    @Query("SELECT * from utilisateur")
    fun getAllUtilisateurs(): Flow<List<Utilisateur>>

}