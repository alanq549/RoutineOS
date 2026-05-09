package com.alan.routineos.core.util

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

    fun getDayOfWeek(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        // Calendar.MONDAY is 2, SUNDAY is 1. 
        // Spec says 1=Lunes ... 7=Domingo
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }
}
