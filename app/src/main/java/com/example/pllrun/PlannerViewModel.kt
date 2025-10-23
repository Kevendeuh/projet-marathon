package com.example.pllrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pllrun.Classes.*  // Utilisateur, UserParams, PlannerService, FullPlan
import com.example.pllrun.calculator.FullPlan
import com.example.pllrun.calculator.PlannerService
import com.example.pllrun.calculator.UserParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/** États exposés à l'UI (Compose/Fragment) */
sealed class PlannerUiState {
    data object Idle : PlannerUiState()
    data object Loading : PlannerUiState()
    data class Ready(val plan: FullPlan) : PlannerUiState()
    data class Error(val message: String) : PlannerUiState()
}

/**
 * ViewModel MVVM : orchestre la génération du plan en appelant le domaine (PlannerService),
 * sans dépendance à l'UI. L'UI observe simplement `state`.
 */
class PlannerViewModel(
    // injection simple par défaut ; remplaçable par Hilt si besoin
    private val service: PlannerService = PlannerService
) : ViewModel() {

    private val _state = MutableStateFlow<PlannerUiState>(PlannerUiState.Idle)
    val state: StateFlow<PlannerUiState> = _state

    /** Génère un plan à partir d'un utilisateur + paramètres fournis. */
    fun buildPlan(utilisateur: Utilisateur, params: UserParams) {
        _state.value = PlannerUiState.Loading
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val fixed = normalize(params) // borne 3..6 j/sem
                val plan = service.generatePlan(utilisateur, fixed)
                _state.value = PlannerUiState.Ready(plan)
            } catch (t: Throwable) {
                _state.value = PlannerUiState.Error(t.message ?: "Erreur inconnue")
            }
        }
    }

    /** Génère avec des paramètres par défaut (jours/sem selon niveau, etc.) et une date cible. */
    fun buildPlanForTarget(utilisateur: Utilisateur, targetDate: LocalDate) {
        val def = service.defaultParamsFor(utilisateur, targetDate)
        buildPlan(utilisateur, def)
    }

    /** Remet l'écran à l'état initial. */
    fun reset() {
        _state.value = PlannerUiState.Idle
    }

    private fun normalize(p: UserParams): UserParams {
        val days = p.trainingDaysPerWeek.coerceIn(3, 6)
        return if (days != p.trainingDaysPerWeek) p.copy(trainingDaysPerWeek = days) else p
    }
}
