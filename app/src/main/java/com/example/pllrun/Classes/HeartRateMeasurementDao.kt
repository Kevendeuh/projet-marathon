package com.example.pllrun.Classes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateMeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBpm(measurement: HeartRateMeasurement)

    @Query("SELECT * FROM heart_rate_measurements ORDER BY timestamp DESC")
    fun getAllBpmMeasurements(): Flow<List<HeartRateMeasurement>>

    // Récupère les BPM pour une journée spécifique (entre startOfDay et endOfDay)
    @Query("SELECT * FROM heart_rate_measurements WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp ASC")
    fun getBpmMeasurementsForDay(startOfDay: Long, endOfDay: Long): Flow<List<HeartRateMeasurement>>

    // Optionnel : Supprimer tout (utile pour le debug/reset)
    @Query("DELETE FROM heart_rate_measurements")
    suspend fun deleteAllBpm()
}