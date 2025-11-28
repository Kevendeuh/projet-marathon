package com.example.pllrun.Classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate_measurements")
data class HeartRateMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bpm: Int,
    val timestamp: Long // Le moment de la mesure
)