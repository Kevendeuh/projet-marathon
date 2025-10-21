package com.example.pllrun.Classes

import androidx.compose.ui.input.key.type
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    // Converter for LocalDate
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // Converter for a list of JourSemaine enums
    @TypeConverter
    fun fromJourSemaineList(value: String): List<JourSemaine> {
        // Example: stores ["LUNDI", "MERCREDI"] as "LUNDI,MERCREDI"
        return value.split(",").map { JourSemaine.valueOf(it) }
    }

    @TypeConverter
    fun toJourSemaineList(list: List<JourSemaine>): String {
        return list.joinToString(",") { it.name }
    }

    // Generic converter for a list of Objects (like Objectif) using Gson
    // ** You will need to add the Gson dependency for this **
    @TypeConverter
    fun fromObjectifList(value: String?): MutableList<Objectif> {
        if (value == null) {
            return mutableListOf()
        }
        val listType = object : TypeToken<MutableList<Objectif>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toObjectifList(list: MutableList<Objectif>?): String {
        return Gson().toJson(list)
    }
}
