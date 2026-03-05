package com.example.pllrun.Classes

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.Duration
import java.time.LocalTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime

class Converters {
    private val gson = Gson()

    // --- Convertisseur pour Map<Int, Double> (Distance par Zone) ---
    @TypeConverter
    fun fromDistanceMap(value: Map<Int, Double>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDistanceMap(value: String?): Map<Int, Double> {
        return value?.let {
            val type = object : TypeToken<Map<Int, Double>>() {}.type
            gson.fromJson(it, type)
        } ?: emptyMap()
    }

    @TypeConverter
    fun fromDuration(duration: Duration?): Long? {
        return duration?.toMillis()
    }

    @TypeConverter
    fun toDuration(millis: Long?): Duration? {
        return millis?.let { Duration.ofMillis(it) }
    }
    // --- Convertisseur pour Map<Int, Long> (Temps par Zone) ---
    @TypeConverter
    fun fromTimeMap(value: Map<Int, Long>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTimeMap(value: String?): Map<Int, Long> {
        return value?.let {
            val type = object : TypeToken<Map<Int, Long>>() {}.type
            gson.fromJson(it, type)
        } ?: emptyMap()
    }

    // Converter for LocalDate
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun toLocalDateTime(date: LocalDateTime?): String? {
        return date?.toString()
    }


    @TypeConverter
    fun fromTimestampToLocalTime(value: Long?): LocalTime? {
        return value?.let { LocalTime.ofSecondOfDay(it) }
    }

    @TypeConverter
    fun localTimeToTimestamp(localTime: LocalTime?): Long? {
        return localTime?.toSecondOfDay()?.toLong()
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
    fun toJourSemaineList(list: List<JourSemaine>?): String {
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

    // --- Convertisseur pour List<Muscle> ---
    @TypeConverter
    fun fromMuscleList(muscles: List<Muscle>?): String {
        return muscles?.joinToString(",") { it.name } ?: ""
    }

    @TypeConverter
    fun toMuscleList(data: String?): List<Muscle> {
        if (data.isNullOrBlank()) {
            return emptyList()
        }
        return data.split(",").map { Muscle.valueOf(it.trim()) }
    }

    // --- Convertisseur pour EffortRessenti ---
    @TypeConverter
    fun fromEffortRessenti(effort: EffortRessenti?): String? {
        return effort?.name
    }

    @TypeConverter
    fun toEffortRessenti(name: String?): EffortRessenti? {
        return name?.let { EffortRessenti.valueOf(it) }
    }

    // --- Convertisseur pour List<Int> (Répétitions par série) ---
    @TypeConverter
    fun fromIntList(repetitions: List<Int>?): String {
        return repetitions?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrBlank()) {
            return emptyList()
        }
        return data.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromDoubleList(repetitions: List<Double>?): String {
        return repetitions?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toDoubleList(data: String?): List<Double> {
        if (data.isNullOrBlank()) {
            return emptyList()
        }
        return data.split(",").map { it.toDouble() }
    }
    @TypeConverter
    fun fromTypeDecoupage(typeDecoupage: TypeDecoupage): String {
        return typeDecoupage.name // .name retourne le nom de la constante enum ("UNIQUE")
    }

    @TypeConverter
    fun toTypeDecoupage(value: String): TypeDecoupage {
        return TypeDecoupage.valueOf(value) // .valueOf() fait la conversion inverse
    }

    @TypeConverter
    fun fromSerieMusculationList(value: List<SerieMusculation>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<SerieMusculation>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSerieMusculationList(value: String?): List<SerieMusculation> {
        if (value == null) return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<SerieMusculation>>() {}.type
        return gson.fromJson(value, type)
    }

    // --- Convertisseur pour List<String> (Souvent oublié pour les tags/images) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.let { gson.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return try {
            if (value.isNullOrBlank()) emptyList()
            else {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(value, type)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


}
