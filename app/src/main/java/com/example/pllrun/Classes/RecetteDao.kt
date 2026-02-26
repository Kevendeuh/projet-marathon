package com.example.pllrun.Classes

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecetteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecette(recette: Recette)

    @Update
    suspend fun updateRecette(recette: Recette)

    @Delete
    suspend fun deleteRecette(recette: Recette)

    @Query("SELECT * FROM Recette WHERE id = :id")
    fun getRecetteById(id: Long): Flow<Recette?>

    @Query("SELECT * FROM Recette ORDER BY titre ASC")
    fun getAllRecettes(): Flow<List<Recette>>
}
