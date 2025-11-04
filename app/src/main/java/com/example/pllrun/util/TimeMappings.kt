package com.example.pllrun.util

import com.example.pllrun.Classes.JourSemaine
import java.time.DayOfWeek

// Public et réutilisable partout
fun JourSemaine.toDayOfWeek(): DayOfWeek = when (this) {
    JourSemaine.LUNDI     -> DayOfWeek.MONDAY
    JourSemaine.MARDI     -> DayOfWeek.TUESDAY
    JourSemaine.MERCREDI  -> DayOfWeek.WEDNESDAY
    JourSemaine.JEUDI     -> DayOfWeek.THURSDAY
    JourSemaine.VENDREDI  -> DayOfWeek.FRIDAY
    JourSemaine.SAMEDI    -> DayOfWeek.SATURDAY
    JourSemaine.DIMANCHE  -> DayOfWeek.SUNDAY
}

// (Optionnel) sens inverse si tu en as besoin ailleurs :
fun DayOfWeek.toJourSemaine(): JourSemaine = when (this) {
    DayOfWeek.MONDAY    -> JourSemaine.LUNDI
    DayOfWeek.TUESDAY   -> JourSemaine.MARDI
    DayOfWeek.WEDNESDAY -> JourSemaine.MERCREDI
    DayOfWeek.THURSDAY  -> JourSemaine.JEUDI
    DayOfWeek.FRIDAY    -> JourSemaine.VENDREDI
    DayOfWeek.SATURDAY  -> JourSemaine.SAMEDI
    DayOfWeek.SUNDAY    -> JourSemaine.DIMANCHE
}
