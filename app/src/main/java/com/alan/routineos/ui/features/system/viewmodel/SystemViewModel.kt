package com.alan.routineos.ui.features.system.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.data.repository.ScheduleExceptionRepository
import com.alan.routineos.ui.features.system.state.SystemUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val exceptionRepo: ScheduleExceptionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getStartOfDay())
    private val _currentWeekStart = MutableStateFlow(DateUtils.getStartOfWeek())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SystemUiState> = combine(
        _selectedDate,
        _currentWeekStart,
        _selectedDate.flatMapLatest { date -> exceptionRepo.getActiveForDate(date) }
    ) { selectedDate, weekStart, adaptations ->
        Log.d("ADAPTATION_DEBUG", "ADAPTATIONS LOAD selectedDate=$selectedDate")
        Log.d("ADAPTATION_DEBUG", "ADAPTATIONS FOUND count=${adaptations.size}")
        
        SystemUiState(
            selectedDate = selectedDate,
            currentWeekStart = weekStart,
            adaptations = adaptations,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SystemUiState(isLoading = true)
    )

    fun selectDate(date: Long) {
        _selectedDate.value = DateUtils.getStartOfDay(date)
        Log.d("ADAPTATION_DEBUG", "CALENDAR DAY SELECTED date=${_selectedDate.value}")
    }

    fun nextWeek() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _currentWeekStart.value
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        _currentWeekStart.value = calendar.timeInMillis
        Log.d("ADAPTATION_DEBUG", "CALENDAR WEEK CHANGE start=${_currentWeekStart.value}")
    }

    fun prevWeek() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _currentWeekStart.value
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        _currentWeekStart.value = calendar.timeInMillis
        Log.d("ADAPTATION_DEBUG", "CALENDAR WEEK CHANGE start=${_currentWeekStart.value}")
    }

    fun createAdaptation(label: String, type: String, rangeType: Int) {
        viewModelScope.launch {
            val from: Long
            val to: Long
            
            when (rangeType) {
                0 -> { // Solo este día
                    from = _selectedDate.value
                    to = _selectedDate.value + (24 * 60 * 60 * 1000L) - 1
                }
                1 -> { // Toda esta semana
                    from = _currentWeekStart.value
                    to = _currentWeekStart.value + (7 * 24 * 60 * 60 * 1000L) - 1
                }
                else -> {
                    from = _selectedDate.value
                    to = _selectedDate.value
                }
            }

            val exception = ScheduleException(
                label = label,
                dateFrom = from,
                dateTo = to,
                affectsGeneration = true
            )
            exceptionRepo.upsert(exception)
            Log.d("ADAPTATION_DEBUG", "ADAPTATION CREATE type=$type from=$from to=$to")
        }
    }

    fun deleteAdaptation(adaptation: ScheduleException) {
        viewModelScope.launch {
            exceptionRepo.delete(adaptation)
            Log.d("ADAPTATION_DEBUG", "ADAPTATION DELETE id=${adaptation.id}")
        }
    }
}
