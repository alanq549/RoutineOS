package com.alan.routineos.ui.features.library.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.ScheduleRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val templateRepo: TemplateRepository,
    private val nodeRepo: NodeRepository,
    private val scheduleRepo: ScheduleRepository,
    private val instanceRepo: InstanceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<LibraryUiState> = combine(
        templateRepo.getAll(),
        nodeRepo.getAllTemplateNodes(),
        scheduleRepo.getAll(),
        _searchQuery
    ) { templates: List<RoutineTemplate>, allNodes: List<Node>, allSchedules: List<Schedule>, query: String ->
        val filtered = if (query.isBlank()) templates
        else templates.filter { it.name.contains(query, ignoreCase = true) }

        LibraryUiState(
            definitions = filtered.map { def ->
                val templateNodes = allNodes.filter { it.templateId == def.id }
                val templateSchedules = allSchedules.filter { it.templateId == def.id }
                
                ActivityDefinitionUi(
                    id = def.id,
                    name = def.name,
                    colorHex = def.colorHex,
                    blocksSummary = if (templateNodes.isEmpty()) "Sin bloques" else "${templateNodes.size} bloques",
                    activeDays = templateSchedules.map { it.weekday - 1 },
                    timeLabel = formatHumanTimeLabel(def.timeMode, templateSchedules),
                    timeMode = def.timeMode
                )
            },
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(isLoading = true)
    )

    private fun formatHumanTimeLabel(mode: TimeMode, schedules: List<Schedule>): String? {
        if (schedules.isEmpty()) return if (mode == TimeMode.FLEXIBLE) "Horario flexible" else null
        val first = schedules.first()
        return when (mode) {
            TimeMode.FIXED_START -> "Inicia: ${first.startTime}"
            TimeMode.RANGE -> "Rango: ${first.startTime} – ${first.endTime}"
            TimeMode.DURATION -> "Inicia: ${first.startTime}"
            TimeMode.FLEXIBLE -> "Flexible"
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun useTemplateToday(id: String) {
        viewModelScope.launch {
            try {
                Log.d("TODAY_DEBUG", "LIBRARY USE TODAY REQUEST templateId=$id")
                val today = DateUtils.getStartOfDay()
                val instance = instanceRepo.useTemplateForDate(id, today, forceRegenerate = false)
                
                if (instance != null) {
                    Log.d("TODAY_DEBUG", "LIBRARY USE TODAY SUCCESS templateId=$id")
                } else {
                    Log.d("TODAY_DEBUG", "LIBRARY USE TODAY SKIPPED/NO_ACTION templateId=$id")
                }
            } catch (e: Exception) {
                Log.e("TODAY_DEBUG", "LIBRARY USE TODAY ERROR templateId=$id error=${e.message}", e)
            }
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            templateRepo.getById(id)?.let {
                templateRepo.delete(it)
                scheduleRepo.deleteByTemplate(it.id)
            }
        }
    }
}
