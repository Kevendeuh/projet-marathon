package com.example.pllrun.Classes

import com.example.pllrun.calculator.ApportsNutritionnels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalTime
import com.example.pllrun.calculator.CaloriesGenerator
import com.example.pllrun.calculator.SleepGenerator

@OptIn(ExperimentalCoroutinesApi::class)
class InventaireRepository(
    private val objectifDao: ObjectifDao,
    private val utilisateurDao: UtilisateurDao,
    private val heartRateMeasurementDao: HeartRateMeasurementDao
) {
    // Insérer une mesure
    suspend fun insertBpm(measurement: HeartRateMeasurement) {
        heartRateMeasurementDao.insertBpm(measurement)
    }

    // Récupérer tout l'historique (Flow pour mise à jour temps réel)
    fun getAllBpmMeasurements(): Flow<List<HeartRateMeasurement>> {
        return heartRateMeasurementDao.getAllBpmMeasurements()
    }

    // Récupérer les données d'un jour précis
    fun getBpmMeasurementsForDay(start: Long, end: Long): Flow<List<HeartRateMeasurement>> {
        return heartRateMeasurementDao.getBpmMeasurementsForDay(start, end)
    }

    // Supprimer tout
    suspend fun deleteAllBpm() {
        heartRateMeasurementDao.deleteAllBpm()
    }

    // --- TEMPS DE SOMMEIL ---
    fun getRecommendedSleepTimeFlow(utilisateurId: Long): Flow<Long> {
        val objectifsFlow = objectifDao.getObjectifsForUtilisateurFlow(utilisateurId)

        return objectifsFlow.flatMapLatest { objectifs ->
            if (objectifs.isEmpty()) {
                flowOf(Pair(emptyList<Objectif>(), emptyList<Activite>()))
            } else {
                val activiteFlows = objectifs.map { objectifDao.getActivitesForObjectifFlow(it.id) }

                // Combine tous les flows d'activités manuellement
                val combinedActivitesFlow: Flow<List<Activite>> = activiteFlows
                    .reduce { acc, flow -> acc.combine(flow) { a, b -> a + b } }

                combinedActivitesFlow.map { allActivites ->
                    Pair(objectifs, allActivites)
                }
            }
        }.map { (objectifs, activites) ->
            val today = LocalDate.now()
            val activitesDuJour = activites.filter { it.date == today }
            val objectifsFinissantLeJour = objectifs.filter { it.dateDeFin == today }

            SleepGenerator.calculateTotalMinutesSleep(activitesDuJour, objectifsFinissantLeJour)
        }
    }

    // --- HEURE DE COUCHER ---
    fun getRecommendedBedtimeFlow(utilisateurId: Long): Flow<LocalTime> {
        val utilisateurFlow = utilisateurDao.getUtilisateurByIdFlow(utilisateurId)
        val objectifsFlow = objectifDao.getObjectifsForUtilisateurFlow(utilisateurId)

        val activitesFlow: Flow<List<Activite>> = objectifsFlow.flatMapLatest { objectifs ->
            if (objectifs.isEmpty()) {
                flowOf(emptyList())
            } else {
                val activiteFlows = objectifs.map { objectifDao.getActivitesForObjectifFlow(it.id) }
                activiteFlows.reduce { acc, flow -> acc.combine(flow) { a, b -> a + b } }
            }
        }

        return combine(utilisateurFlow, activitesFlow) { utilisateur, activites ->
            val activitesDuJour = activites.filter { it.date == LocalDate.now() }

            val heureFinDerniereActivite = activitesDuJour
                .map { it.heureDeDebut.plus(it.tempsEffectue) }
                .maxOrNull()

            SleepGenerator.calculateHeureDeCouche(
                heureFinDerniereActivite = heureFinDerniereActivite,
                heureDeCoucheParDefaut = LocalTime.of(22, 0)
            )
        }
    }

    // --- CALORIES ---
    fun getRecommendedNutrimentsFlow(utilisateurId: Long): Flow<ApportsNutritionnels> {
        val utilisateurFlow = utilisateurDao.getUtilisateurByIdFlow(utilisateurId)
        val objectifsFlow = objectifDao.getObjectifsForUtilisateurFlow(utilisateurId)

        val activitesFlow: Flow<List<Activite>> = objectifsFlow.flatMapLatest { objectifs ->
            if (objectifs.isEmpty()) {
                flowOf(emptyList())
            } else {
                val activiteFlows = objectifs.map { objectifDao.getActivitesForObjectifFlow(it.id) }
                activiteFlows.reduce { acc, flow -> acc.combine(flow) { a, b -> a + b } }
            }
        }

        return combine(utilisateurFlow, activitesFlow) { utilisateur, activites ->
            val activitesDuJour = activites.filter { it.date == LocalDate.now() }
            CaloriesGenerator.calculateTotalNutriments(utilisateur, activitesDuJour)
        }
    }

    fun getObjectifsForUtilisateurFlow(utilisateurId: Long): Flow<List<Objectif>> {
        return objectifDao.getObjectifsForUtilisateurFlow(utilisateurId)
    }

    fun getFirstUtilisateurFlow(): Flow<Utilisateur?> = utilisateurDao.getFirstUtilisateur()

}
