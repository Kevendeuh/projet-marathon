package com.example.pllrun.Classes

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Duration

/**
 * Classe de base ouverte représentant un objectif d'entraînement.
 * 'open' permet à d'autres classes d'en hériter.
 *
 * @property nom Le nom ou la description de l'objectif (ex: "Préparation Marathon de Paris").
 * @property dateDeDebut Date de début du plan d'entraînement.
 * @property dateDeFin Date de fin du plan ou date de l'événement.
 * @property intensite Le niveau d'intensité global du plan d'entraînement.
 */
open class Objectif(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    @ColumnInfo(name = "nom")
    val nom: String,
    @ColumnInfo(name = "dateDeDebut")
    val dateDeDebut: LocalDate,
    @ColumnInfo(name = "dateDeFin")
    val dateDeFin: LocalDate,
    @ColumnInfo(name = "Niveau")
    val Niveau: NiveauExperience
) {
    /**
     * Propriété calculée pour obtenir la durée totale de l'objectif en jours.
     */
    val duree: Long
        get() = Duration.between(dateDeDebut.atStartOfDay(), dateDeFin.atStartOfDay()).toDays()
}


/**
 * Sous-classe spécialisée pour un objectif de course (ex: un marathon, un 10km).
 * Hérite de la classe [Objectif].
 *
 * @property distance La distance de la course en kilomètres.
 * @property objectifTemps Le temps visé pour terminer la course, en minutes.
 * @property trajetOptionnel Le parcours spécifique de la course, s'il est connu.
 */
class ObjectifCourse(
    id: String,
    nom: String,
    dateDeDebut: LocalDate,
    dateDeFin: LocalDate,
    Niveau: NiveauExperience,
    val distance: Double, // en km
    val objectifTemps: Int, // en minutes
) : Objectif(id,nom, dateDeDebut, dateDeFin, Niveau)