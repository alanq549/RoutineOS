package com.alan.routineos.core.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun getStartOfDay(date: Date = Date()): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Devuelve el día de la semana actual mapeado a la lógica de RoutineOS.
     * Lunes=1, Martes=2, Miércoles=3, Jueves=4, Viernes=5, Sábado=6, Domingo=7
     */
    fun getDayOfWeek(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar: SUNDAY=1, MONDAY=2, TUESDAY=3, WEDNESDAY=4, THURSDAY=5, FRIDAY=6, SATURDAY=7
        // Mapping:
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }

    fun formatHeaderDate(date: Date = Date()): String {
        val locale = Locale.getDefault()
        val pattern = if (locale.language == "es") "EEEE, d 'de' MMMM" else "EEEE, MMMM d"
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(date).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
        }
    }
    
    fun formatHeaderMonth(date: Date = Date()): String {
        val locale = Locale.getDefault()
        val formatter = SimpleDateFormat("MMMM yyyy", locale)
        return formatter.format(date).uppercase()
    }
}
