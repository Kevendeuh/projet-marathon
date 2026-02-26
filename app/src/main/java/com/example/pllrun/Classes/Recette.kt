package com.example.pllrun.Classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une recette de cuisine avec ses informations nutritionnelles.
 *
 * @property id Identifiant unique de la recette, généré automatiquement.
 * @property titre Le nom de la recette (ex: "Poulet grillé et quinoa").
 * @property calories Nombre de calories pour une portion.
 * @property proteines Quantité de protéines en grammes (g).
 * @property glucides Quantité de glucides en grammes (g).
 * @property lipides Quantité de lipides en grammes (g).
 * @property imageUri URI vers une image de la recette (optionnel).
 */
@Entity(tableName = "Recette")
data class Recette(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "titre")
    var titre: String,

    @ColumnInfo(name = "texte")
    var texte: String,

    @ColumnInfo(name = "calories")
    var calories: Float,

    @ColumnInfo(name = "proteines")
    var proteines: Float,

    @ColumnInfo(name = "glucides")
    var glucides: Float,

    @ColumnInfo(name = "lipides")
    var lipides: Float,

    @ColumnInfo(name = "imageUri")
    var imageUri: String? = null
)
