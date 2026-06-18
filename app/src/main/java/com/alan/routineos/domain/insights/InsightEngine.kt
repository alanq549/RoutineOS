package com.alan.routineos.domain.insights

import com.alan.routineos.domain.observability.DailySummary
import com.alan.routineos.domain.observability.WeeklySummary
import java.util.UUID

object InsightEngine {

    fun generateDailyInsights(summary: DailySummary): List<RoutineInsight> {
        val insights = mutableListOf<RoutineInsight>()

        if (summary.totalItems == 0) {
            insights.add(
                RoutineInsight(
                    id = "daily_no_activities",
                    title = "Sin actividad",
                    message = "No hay actividades planeadas para hoy.",
                    category = InsightCategory.COMPLETION,
                    severity = InsightSeverity.INFO
                )
            )
            return insights
        }

        if (summary.completionRate >= 0.8f) {
            insights.add(
                RoutineInsight(
                    id = "daily_completion_high",
                    title = "Gran progreso",
                    message = "Has completado la gran mayoría de tus actividades. ¡Sigue así!",
                    category = InsightCategory.COMPLETION,
                    severity = InsightSeverity.POSITIVE
                )
            )
        } else if (summary.completionRate < 0.5f) {
            insights.add(
                RoutineInsight(
                    id = "daily_completion_low",
                    title = "Ritmo bajo",
                    message = "Tu nivel de cumplimiento hoy es bajo. Intenta retomar tus prioridades.",
                    category = InsightCategory.COMPLETION,
                    severity = InsightSeverity.WARNING
                )
            )
        }

        if (summary.delayedCount >= 2) {
            insights.add(
                RoutineInsight(
                    id = "daily_delay_high",
                    title = "Varias actividades retrasadas",
                    message = "Has pospuesto 2 o más actividades hoy. Considera ajustar tus horarios.",
                    category = InsightCategory.DELAY,
                    severity = InsightSeverity.WARNING
                )
            )
        }

        if (summary.modifiedCount >= 3) {
            insights.add(
                RoutineInsight(
                    id = "daily_modification_high",
                    title = "Muchos ajustes",
                    message = "Tu plan ha sufrido varias modificaciones. Es normal, pero mantén el enfoque.",
                    category = InsightCategory.MODIFICATION,
                    severity = InsightSeverity.INFO
                )
            )
        }

        if (summary.actualDurationMinutes > summary.plannedDurationMinutes && summary.plannedDurationMinutes > 0) {
            insights.add(
                RoutineInsight(
                    id = "daily_duration_over",
                    title = "Tiempo excedido",
                    message = "Tus actividades están tomando más tiempo del planeado.",
                    category = InsightCategory.DURATION,
                    severity = InsightSeverity.INFO
                )
            )
        }

        return insights
    }

    fun generateWeeklyInsights(summary: WeeklySummary): List<RoutineInsight> {
        val insights = mutableListOf<RoutineInsight>()

        if (summary.averageCompletionRate >= 0.75f) {
            insights.add(
                RoutineInsight(
                    id = "weekly_consistency_high",
                    title = "Consistencia sólida",
                    message = "Has mantenido un gran ritmo durante toda la semana.",
                    category = InsightCategory.CONSISTENCY,
                    severity = InsightSeverity.POSITIVE
                )
            )
        } else if (summary.averageCompletionRate < 0.5f) {
            insights.add(
                RoutineInsight(
                    id = "weekly_consistency_low",
                    title = "Consistencia irregular",
                    message = "Esta semana tu cumplimiento ha sido inestable. Busca recuperar el hábito.",
                    category = InsightCategory.CONSISTENCY,
                    severity = InsightSeverity.WARNING
                )
            )
        }

        if (summary.totalSkippedCount >= 3) {
            insights.add(
                RoutineInsight(
                    id = "weekly_skip_high",
                    title = "Actividades omitidas",
                    message = "Has omitido 3 o más actividades esta semana. Evalúa si son necesarias.",
                    category = InsightCategory.COMPLETION,
                    severity = InsightSeverity.WARNING
                )
            )
        }

        if (summary.totalModifiedCount >= 5) {
            insights.add(
                RoutineInsight(
                    id = "weekly_modification_high",
                    title = "Plan flexible",
                    message = "Has realizado muchos cambios esta semana. Revisa si tu rutina es realista.",
                    category = InsightCategory.MODIFICATION,
                    severity = InsightSeverity.INFO
                )
            )
        }

        if (summary.totalActualDurationMinutes > summary.totalPlannedDurationMinutes && summary.totalPlannedDurationMinutes > 0) {
            insights.add(
                RoutineInsight(
                    id = "weekly_duration_over",
                    title = "Sobrecarga de tiempo",
                    message = "En total, has invertido más tiempo del estimado en tus rutinas.",
                    category = InsightCategory.DURATION,
                    severity = InsightSeverity.INFO
                )
            )
        }

        return insights
    }
}
