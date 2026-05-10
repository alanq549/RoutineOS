package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LibraryUiState(
    val templates: List<RoutineTemplate> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val templateRepo: TemplateRepository,
    private val nodeRepo: NodeRepository,
    private val instanceRepo: InstanceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    
    val uiState: StateFlow<LibraryUiState> = combine(
        templateRepo.getAll(),
        _searchQuery
    ) { templates, query ->
        LibraryUiState(
            templates = if (query.isBlank()) templates 
                        else templates.filter { it.name.contains(query, ignoreCase = true) },
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun useTemplateToday(template: RoutineTemplate) {
        viewModelScope.launch {
            val today = DateUtils.getStartOfDay()
            val newInstance = DayInstance(
                id = UUID.randomUUID().toString(),
                templateId = template.id,
                date = today,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            instanceRepo.upsert(newInstance)

            val templateNodes = nodeRepo.getAllByTemplate(template.id)
            val idMap = mutableMapOf<String, String>()
            templateNodes.forEach { idMap[it.id] = UUID.randomUUID().toString() }

            val instanceNodes = templateNodes.map { tNode ->
                tNode.copy(
                    id = idMap[tNode.id]!!,
                    parentId = idMap[tNode.parentId],
                    templateId = tNode.id,
                    instanceId = newInstance.id,
                    status = NodeStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_SYNC
                )
            }
            nodeRepo.insertAll(instanceNodes)
        }
    }

    fun deleteTemplate(template: RoutineTemplate) {
        viewModelScope.launch {
            // Marcar como eliminado localmente para sincronizar el borrado si es necesario, 
            // o simplemente borrar y confiar en el sync de la lista completa.
            // Por simplicidad en Fase 8, borramos:
            templateRepo.delete(template)
        }
    }
}
