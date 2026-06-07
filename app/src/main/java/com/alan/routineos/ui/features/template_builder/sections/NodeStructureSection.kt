package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.ui.features.template_builder.components.AddDashedButton
import com.alan.routineos.ui.features.template_builder.components.NodeItem
import com.alan.routineos.ui.features.template_builder.components.SectionHeader
import com.alan.routineos.core.util.ScheduleResolver
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.ColorTextDim

fun LazyListScope.nodeStructureSection(
    nodes: List<Node>,
    nodeSchedules: Map<String, List<NodeSchedule>>,
    fieldValues: Map<String, List<NodeFieldValue>>,
    nodeTypes: List<NodeType>,
    metadataSchemas: Map<String, List<NodeMetadataSchema>>,
    activityName: String = "",
    activityDays: Set<Int> = emptySet(),
    activityStart: String? = null,
    activityEnd: String? = null,
    onAddNode: (parentId: String?) -> Unit,
    onUpdateNodeName: (nodeId: String, name: String) -> Unit,
    onUpdateNodeType: (nodeId: String, typeId: String) -> Unit,
    onUpdateFieldValue: (nodeId: String, schemaId: String, fieldName: String, value: String) -> Unit,
    onAddNodeFull: (name: String, typeId: String, parentId: String?) -> Unit = { _, _, _ -> },
    onDeleteNode: (nodeId: String) -> Unit,
    onScheduleClick: (Node) -> Unit,
    onManageDetailsClick: () -> Unit
) {
    item {
        SectionHeader(
            title = "ESTRUCTURA DE LA ACTIVIDAD", 
            onAdd = { onAddNode(null) },
            addLabel = "+ bloque"
        )
        
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(
                "Divide esta actividad en bloques, pasos o materias.",
                style = MetaMono.copy(fontSize = 8.sp),
                color = ColorTextDim
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        AddDashedButton(
            text = "Agregar bloques o subbloques",
            onClick = onManageDetailsClick
        )
        
        Spacer(modifier = Modifier.height(12.dp))
    }

    val rootNodes = nodes.filter { it.parentId == null }
    items(rootNodes, key = { it.id }) { node ->
        NodeHierarchy(
            node = node,
            allNodes = nodes,
            nodeSchedules = nodeSchedules,
            fieldValues = fieldValues,
            nodeTypes = nodeTypes,
            metadataSchemas = metadataSchemas,
            depth = 0,
            activityName = activityName,
            activityDays = activityDays,
            activityStart = activityStart,
            activityEnd = activityEnd,
            onAddChild = onAddNode,
            onUpdateName = onUpdateNodeName,
            onUpdateType = onUpdateNodeType,
            onUpdateFieldValue = onUpdateFieldValue,
            onDeleteNode = onDeleteNode,
            onScheduleClick = onScheduleClick,
            onManageDetailsClick = onManageDetailsClick
        )
    }

    item {
        Spacer(modifier = Modifier.height(12.dp))
        AddDashedButton(text = "agregar bloque principal", onClick = { onAddNode(null) })
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@androidx.compose.runtime.Composable
private fun NodeHierarchy(
    node: Node,
    allNodes: List<Node>,
    nodeSchedules: Map<String, List<NodeSchedule>>,
    fieldValues: Map<String, List<NodeFieldValue>>,
    nodeTypes: List<NodeType>,
    metadataSchemas: Map<String, List<NodeMetadataSchema>>,
    depth: Int,
    activityName: String,
    activityDays: Set<Int>,
    activityStart: String?,
    activityEnd: String?,
    onAddChild: (String) -> Unit,
    onUpdateName: (String, String) -> Unit,
    onUpdateType: (String, String) -> Unit,
    onUpdateFieldValue: (String, String, String, String) -> Unit,
    onDeleteNode: (String) -> Unit,
    onScheduleClick: (Node) -> Unit,
    onManageDetailsClick: () -> Unit
) {
    val children = allNodes.filter { it.parentId == node.id }
    val schedules = nodeSchedules[node.id] ?: emptyList()
    val values = fieldValues[node.id] ?: emptyList()
    val schemas = metadataSchemas[node.typeId] ?: emptyList()

    val inheritanceInfo = ScheduleResolver.resolveInheritanceInfo(
        nodeId = node.id,
        allNodes = allNodes,
        nodeSchedules = nodeSchedules,
        activityName = activityName,
        activityDays = activityDays,
        activityStart = activityStart,
        activityEnd = activityEnd
    )
    val hasOwnSchedule = schedules.isNotEmpty()
    val isInherited = !hasOwnSchedule && inheritanceInfo != null
    
    val parentId = node.parentId
    val isOutside = if (hasOwnSchedule) {
        val parentEffective = if (parentId != null) {
            ScheduleResolver.resolveEffectiveSchedules(
                nodeId = parentId,
                allNodes = allNodes,
                nodeSchedules = nodeSchedules,
                activityName = activityName,
                activityDays = activityDays,
                activityStart = activityStart,
                activityEnd = activityEnd
            )
        } else {
             // Si no hay padre, el "rango superior" es la actividad
             if (activityDays.isNotEmpty() && activityStart != null) {
                 activityDays.map { day -> 
                     NodeSchedule(nodeId = node.id, dayOfWeek = day, startTime = activityStart, endTime = activityEnd ?: activityStart)
                 }
             } else emptyList()
        }
        ScheduleResolver.isOutsideRange(schedules, parentEffective)
    } else {
        false
    }
    
    NodeItem(
        name = node.name,
        onNameChange = { newName: String -> onUpdateName(node.id, newName) },
        depth = depth,
        hasSchedules = hasOwnSchedule,
        isInherited = isInherited,
        inheritedFrom = inheritanceInfo?.first,
        effectiveSchedules = if (hasOwnSchedule) schedules else (inheritanceInfo?.second ?: emptyList()),
        isOutsideRange = isOutside,
        nodeTypes = nodeTypes,
        selectedTypeId = node.typeId,
        onTypeChange = { newTypeId: String -> onUpdateType(node.id, newTypeId) },
        schemas = schemas,
        fieldValues = values,
        onFieldValueChange = { schemaId: String, fieldName: String, value: String ->
            onUpdateFieldValue(node.id, schemaId, fieldName, value) 
        },
        onAddClick = { onAddChild(node.id) },
        onDeleteClick = { onDeleteNode(node.id) },
        onScheduleClick = { onScheduleClick(node) },
        onManageDetailsClick = onManageDetailsClick
    )
    children.forEach { child ->
        NodeHierarchy(
            node = child,
            allNodes = allNodes,
            nodeSchedules = nodeSchedules,
            fieldValues = fieldValues,
            nodeTypes = nodeTypes,
            metadataSchemas = metadataSchemas,
            depth = depth + 1,
            activityName = activityName,
            activityDays = activityDays,
            activityStart = activityStart,
            activityEnd = activityEnd,
            onAddChild = onAddChild,
            onUpdateName = onUpdateName,
            onUpdateType = onUpdateType,
            onUpdateFieldValue = onUpdateFieldValue,
            onDeleteNode = onDeleteNode,
            onScheduleClick = onScheduleClick,
            onManageDetailsClick = onManageDetailsClick
        )
    }
}
