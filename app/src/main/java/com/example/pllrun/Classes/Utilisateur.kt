package com.example.pllrun.Classes

import java.time.LocalDate

/**
 * Représente un utilisateur de l'application de coaching pour marathon.
 * Contient des informations personnelles, des métriques physiologiques,
 * et les objectifs de course pour permettre la génération d'un plan d'entraînement personnalisé.
 *
 * @property id Identifiant unique de l'utilisateur, généralement fourni par le système d'authentification (ex: Firebase Auth).
 * @property nom Nom de famille de l'utilisateur.
 * @property prenom Prénom de l'utilisateur.
 * @property dateDeNaissance Date de naissance pour calculer l'âge et adapter l'entraînement.
 * @property sexe Sexe biologique de l'utilisateur ("Homme", "Femme", "Autre"), peut influencer les calculs de performance.
 * @property poids Poids de l'utilisateur en kilogrammes (kg). Utile pour le calcul des calories et de la charge d'entraînement.
 * @property taille Taille de l'utilisateur en centimètres (cm).
 * @property vma Vitesse Maximale Aérobie (en km/h). C'est un indicateur clé de la performance en endurance.
 * @property fcm Fréquence Cardiaque Maximale (en bpm). Essentiel pour définir les zones d'entraînement (endurance, seuil, etc.).
 * @property fcr Fréquence Cardiaque au Repos (en bpm). Un bon indicateur du niveau de forme cardiovasculaire.
 * @property niveauExperience Le niveau de course de l'utilisateur (Débutant, Intermédiaire, Avancé).
 * @property joursEntrainementDisponibles Liste des jours de la semaine où l'utilisateur est disponible pour s'entraîner (ex: "Lundi", "Mardi", ...).
 * @property objectifs L'objectif principal de l'utilisateur pour sa prochaine course.
 */
data class Utilisateur(
    // --- Informations d'identification ---
    val id: String,

    // --- Informations personnelles (modifiables) ---
    var nom: String = "",
    var prenom: String = "",
    var dateDeNaissance: LocalDate? = null,
    var sexe: Sexe = Sexe.NON_SPECIFIE,

    // --- Paramètres physiologiques (modifiables) ---
    var poids: Double = 0.0, // en kg
    var taille: Int = 0,     // en cm
    var vma: Double? = 0.0,   // en km/h
    var fcm: Int? = 0,        // Fréquence Cardiaque Maximale (bpm)
    var fcr: Int? = 0,        // Fréquence Cardiaque au Repos (bpm)

    // --- Expérience et disponibilité (modifiables) ---
    var niveauExperience: NiveauExperience = NiveauExperience.DEBUTANT,
    var joursEntrainementDisponibles: List<JourSemaine> = emptyList(),

    // --- Objectifs de course (modifiables) ---
    // --- liste d'objectifs de course ---
    var objectifs: MutableList<Objectif> = mutableListOf(),
    )

/**
 * Énumération pour le sexe de l'utilisateur.
 */
enum class Sexe {
    HOMME,
    FEMME,
    AUTRE,
    NON_SPECIFIE
}

/**
 * Énumération pour le niveau d'expérience en course à pied.
 */
enum class NiveauExperience {
    DEBUTANT,       // Moins de 6 mois de course régulière
    INTERMEDIAIRE,  // Entre 6 mois et 2 ans, a déjà couru des 10km ou semi-marathons
    AVANCE          // Plus de 2 ans, plusieurs marathons à son actif
}

/**
 * Énumération pour les jours de la semaine.
 */
enum class JourSemaine {
    LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI, SAMEDI, DIMANCHE
}

