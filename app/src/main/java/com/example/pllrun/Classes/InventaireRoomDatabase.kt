package com.example.pllrun.Classes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Utilisateur::class,
        Objectif::class,
        Activite::class,
        CourseActivite::class,
        HeartRateMeasurement::class]
    , version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class InventaireRoomDatabase : RoomDatabase() {

    abstract fun utilisateurDao(): UtilisateurDao
    abstract fun objectifDao(): ObjectifDao
    abstract fun measurementDao(): HeartRateMeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: InventaireRoomDatabase? = null
        fun getDatabase(context: Context): InventaireRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventaireRoomDatabase::class.java,
                    "Inventaire_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
