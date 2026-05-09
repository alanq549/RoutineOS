package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class PlannerUiState(
    val selectedDate: Long = DateUtils.getStartOfDay(),
    val weekDays: List<Long> = emptyList(),
    val activeSchedules: List<ScheduleWithTemplate> = emptyList(),
    val templates: List<RoutineTemplate> = emptyList(),
    val exceptions: List<ScheduleException> = emptyList(),
    val instanceForSelectedDate: DayInstance? = null,
    val isLoading: Boolean = true
)

data class ScheduleWithTemplate(
    val schedule: Schedule,
    val template: RoutineTemplate?
)

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val scheduleRepo: ScheduleRepository,
    private val templateRepo: TemplateRepository,
    private val exceptionRepo: ScheduleExceptionRepository,
    private val instanceRepo: InstanceRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getStartOfDay())
    val selectedDate = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlannerUiState> = _selectedDate.flatMapLatest { date ->
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val weekday = DateUtils.getDayOfWeek(Date(date))
        
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val week = (0..6).map {
            val d = startOfWeek.timeInMillis
            startOfWeek.add(Calendar.DAY_OF_YEAR, 1)
            d
        }

        combine(
            scheduleRepo.getActiveForWeekday(weekday, date),
            templateRepo.getAll(),
            exceptionRepo.getActiveForDate(date),
            instanceRepo.getByDate(date)
        ) { schedules, templates, exceptions, instance ->
            PlannerUiState(
                selectedDate = date,
                weekDays = week,
                activeSchedules = schedules.map { s -> 
                    ScheduleWithTemplate(s, templates.find { it.id == s.templateId }) 
                },
                templates = templates,
                exceptions = exceptions,
                instanceForSelectedDate = instance,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlannerUiState())

    fun selectDate(date: Long) {
        _selectedDate.value = date
    }

    fun addSchedule(templateId: String, weekday: Int, startTime: String) {
        viewModelScope.launch {
            scheduleRepo.upsert(
                Schedule(
                    id = UUID.randomUUID().toString(),
                    templateId = templateId,
                    weekday = weekday,
                    startTime = startTime
                )
            )
        }
    }

    fun addException(label: String, from: Long, to: Long) {
        viewModelScope.launch {
            exceptionRepo.upsert(
                ScheduleException(
                    id = UUID.randomUUID().toString(),
                    label = label,
                    dateFrom = from,
                    dateTo = to,
                    affectsGeneration = true
                )
            )
        }
    }
}
