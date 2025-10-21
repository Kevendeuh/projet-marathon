package com.example.pllrun.Classes

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.Duration
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
    fun fromJourSemaineList(value: String?): List<JourSemaine> {
        // 1. Handle null or blank input string
        if (value.isNullOrBlank()) {
            return emptyList()
        }
        // 2. Filter out any potential empty strings after splitting
        return value.split(",").filter { it.isNotEmpty() }.map { JourSemaine.valueOf(it.trim()) }
    }

    @TypeConverter
    fun toJourSemaineList(list: List<JourSemaine>): String {
        return list?.joinToString(",") { it.name } ?: ""
    }

    @TypeConverter
    fun fromTypeObjectif(typeObjectif: TypeObjectif): String {
        return typeObjectif.name // .name retourne le nom de la constante enum ("COURSE")
    }

    @TypeConverter
    fun toTypeObjectif(value: String): TypeObjectif {
        return TypeObjectif.valueOf(value) // .valueOf() fait la conversion inverse
    }

    @TypeConverter
    fun fromDurationString(value: String?): Duration? {
        return value?.let { Duration.parse(it) }
    }

    @TypeConverter
    fun toDurationString(duration: Duration?): String? {
        return duration?.toString()
    }


}
