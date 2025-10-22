package com.example.pllrun.Classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
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
 * @property niveau Le niveau d'intensité global du plan d'entraînement.
 * @property tauxDeProgression Le pourcentage de progression actuel de l'objectif.
 * @property type Le type d'objectif (ex: "Marathon").
 * @property utilisateurId L'ID de l'utilisateur associé à cet objectif.
 */
@Entity(tableName = "Objectif",
        foreignKeys = [
    ForeignKey(
        entity = Utilisateur::class,
        parentColumns = ["id"],
        childColumns = ["utilisateurId"],
        onDelete = ForeignKey.CASCADE // If the parent Utilisateur is deleted, delete this objectif
    )
])
data class Objectif(
    @PrimaryKey(autoGenerate = true)
    val id: Long=0,
    @ColumnInfo(name = "nom")
    var nom: String,
    @ColumnInfo(name = "dateDeDebut")
    var dateDeDebut: LocalDate,
    @ColumnInfo(name = "dateDeFin")
    var dateDeFin: LocalDate,
    @ColumnInfo(name = "niveau")
    var niveau: NiveauExperience,
    @ColumnInfo(name = "taux_de_progression")
    var tauxDeProgression: Double,
    @ColumnInfo(name = "type")
    var type: TypeObjectif,
    @ColumnInfo(index = true) // Indexing foreign keys is good for performance
    var utilisateurId: Long,
) {
    /**
     * Propriété calculée pour obtenir la durée totale de l'objectif en jours.
     */
    val duree: Long
        get() = Duration.between(dateDeDebut.atStartOfDay(), dateDeFin.atStartOfDay()).toDays()
}

enum class TypeObjectif {
    COURSE,MARATHON,ETIREMENT,CARDIO,AUTRE
}

/**
* Represents a single training activity (e.g., a specific run) linked to a parent Objectif.
*
* @param objectifId The ID of the parent Objectif this activity belongs to.
* @param distanceEffectuee The actual distance covered during the activity.
* @param tempsEffectue The actual time taken for the activity.
* @param typeActivite The type of activity (e.g., "Marathon").
 * @param estComplete A flag indicating whether the activity has been completed.
 * @param id The unique identifier for the activity.
 * @param nom The name of the activity.
 * @param description An optional description of the activity.
 * @param date The date this activity took place.
*/
@Entity(
    tableName = "activite",
    foreignKeys = [
        ForeignKey(
            entity = Objectif::class,
            parentColumns = ["id"],
            childColumns = ["objectifId"],
            onDelete = ForeignKey.CASCADE // If the parent Objectif is deleted, delete this activity
        )
    ]
)
data class Activite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(index = true)
    val objectifId: Long, // Foreign key linking to the Objectif table

    // Activity-specific properties
    @ColumnInfo(name = "nom")
    var nom: String, // e.g., "Sortie Longue Semaine 1"
    @ColumnInfo(name = "description")
    var description: String = "", // Optional description of the activity
    @ColumnInfo(name = "date")
    var date: LocalDate, // The date this activity took place
    @ColumnInfo(name = "distance_effectuee")
    var distanceEffectuee: Double, // in km
    @ColumnInfo(name = "temps_effectue")
    var tempsEffectue: Duration,   // actual duration of the run
    @ColumnInfo(name = "type_activite")
    var typeActivite: TypeObjectif,
    @ColumnInfo(name = "est_complete")
    var estComplete: Boolean = false

)