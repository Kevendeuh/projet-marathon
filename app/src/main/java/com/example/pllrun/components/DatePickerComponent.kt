package com.example.pllrun.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerComponent(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Crée et mémorise l'état du DatePicker.
    // On l'initialise avec la date fournie pour que le calendrier s'ouvre sur le bon jour.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = { onDismiss() }, // Action à faire quand on ferme le dialogue
        confirmButton = {
            Button(
                onClick = {
                    // On récupère la date sélectionnée (en millisecondes)
                    datePickerState.selectedDateMillis?.let { millis ->
                        // On la convertit en objet LocalDate
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        // On appelle la fonction de rappel avec la nouvelle date
                        onDateSelected(selectedDate)
                    }
                    onDismiss() // On ferme le dialogue
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Annuler")
            }
        }
    ) {
        // Le composant visuel du calendrier
        DatePicker(state = datePickerState)
    }
}
