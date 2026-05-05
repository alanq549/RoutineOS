package com.alan.routineos.ui.components

enum class ActivityType { Habit, Task, Workout }

data class ActivityModel(
    val id: String,
    val time: String,
    val duration: String,
    val title: String,
    val description: String,
    val type: ActivityType,
    val isDone: Boolean = false
)

val mockActivities = listOf(
    ActivityModel("1", "06:30", "15m", "Morning Routine", "Meditación + Hidratación", ActivityType.Habit, true),
    ActivityModel("2", "08:00", "1h 30m", "Deep Work: API Auth", "Implementar SharedFlows", ActivityType.Task, true),
    ActivityModel("3", "12:00", "30m", "Lectura Técnica", "Clean Architecture en Android", ActivityType.Habit, true),
    ActivityModel("4", "18:00", "1h", "Gimnasio: Pecho", "Bench Press, Incline, Flyes", ActivityType.Workout, false),
    ActivityModel("5", "20:30", "45m", "Review PRs", "Feedback del equipo", ActivityType.Task, false)
)