package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.ui.features.template_builder.components.AddDashedButton
import com.alan.routineos.ui.features.template_builder.components.NodeItem
import com.alan.routineos.ui.features.template_builder.components.SectionHeader
import com.alan.routineos.core.util.ScheduleResolver

fun LazyListScope.nodeStructureSection(
    nodes: List<Node>,
    nodeSchedules: Map<String, List<NodeSchedule>>,
    fieldValues: Map<String, List<NodeFieldValue>>,
    nodeTypes: List<NodeType>,
    metadataSchemas: Map<String, List<NodeMetadataSchema>>,
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
            title = "PASOS DE LA ACTIVIDAD", 
            onAdd = { onAddNode(null) },
            addLabel = "+ paso"
        )
        
        AddDashedButton(
            text = "configurar detalles de los pasos",
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
        AddDashedButton(text = "agregar paso principal", onClick = { onAddNode(null) })
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

    val effectiveSchedules = ScheduleResolver.resolveEffectiveSchedules(node.id, allNodes, nodeSchedules)
    val hasOwnSchedule = schedules.isNotEmpty()
    val isInherited = !hasOwnSchedule && effectiveSchedules.isNotEmpty()
    
    val parentId = node.parentId
    val isOutside = if (hasOwnSchedule && parentId != null) {
        val parentEffective = ScheduleResolver.resolveEffectiveSchedules(parentId, allNodes, nodeSchedules)
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
