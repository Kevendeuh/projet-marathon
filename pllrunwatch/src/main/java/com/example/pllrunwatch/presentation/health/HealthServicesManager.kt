package com.example.pllrunwatch.presentation.health

// wear/src/main/java/.../health/HealthServicesManager.kt

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.DataType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

/**
 * Small wrapper for MeasureClient short-lived measurements (UI / spot measurements).
 * For background long-running monitoring use passive monitoring APIs (see docs).
 */
