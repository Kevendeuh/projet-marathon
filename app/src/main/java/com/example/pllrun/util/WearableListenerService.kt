package com.example.pllrun.util

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import android.util.Log
import com.example.pllrun.Classes.HeartRateMeasurement
import com.example.pllrun.Classes.InventaireRepository
import com.example.pllrun.Classes.InventaireRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {
    // Injectez votre Repository (ou DAO) ici
    private val inventaireRepository: InventaireRepository by lazy {
        // 1. Récupérer l'instance de la base de données (Singleton)
        val database = InventaireRoomDatabase.getDatabase(applicationContext)

        // 2. Créer le repository manuellement en lui passant les DAOs
        InventaireRepository(
            utilisateurDao = database.utilisateurDao(),
            objectifDao = database.objectifDao(),
            heartRateMeasurementDao = database.measurementDao() // Assurez-vous d'avoir ajouté cette méthode dans la DB
        )
    }
    // Créez un scope pour lancer des coroutines (car écrire en BDD est asynchrone)
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/health/delta") {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val heartRate = dataMapItem.dataMap.getInt("last_hr")
                    val timestamp = dataMapItem.dataMap.getLong("ts")

                    Log.d("PhoneReceiver", "Reçu FC: $heartRate bpm à $timestamp")

                    // --- SAUVEGARDE EN BASE DE DONNÉES ---
                    serviceScope.launch {
                        val newMeasure = HeartRateMeasurement(bpm = heartRate, timestamp = timestamp)
                        inventaireRepository.insertBpm(newMeasure)
                    }
                }
            }
        }
    }
}
