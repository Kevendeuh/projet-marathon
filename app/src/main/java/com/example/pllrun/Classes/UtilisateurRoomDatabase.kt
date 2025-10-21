package com.example.pllrun.Classes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Utilisateur::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class UtilisateurRoomDatabase : RoomDatabase() {

    abstract fun utilisateurDao(): UtilisateurDao

    companion object {
        @Volatile
        private var INSTANCE: UtilisateurRoomDatabase? = null
        fun getDatabase(context: Context): UtilisateurRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UtilisateurRoomDatabase::class.java,
                    "utilisateur_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
