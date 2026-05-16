package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.ui.features.template_builder.components.AddDashedButton
import com.alan.routineos.ui.features.template_builder.components.NodeItem
import com.alan.routineos.ui.features.template_builder.components.SectionHeader

fun LazyListScope.nodeStructureSection(
    nodes: List<Node>,
    nodeSchedules: Map<String, List<NodeSchedule>>,
    onAddNode: (parentId: String?) -> Unit,
    onDeleteNode: (nodeId: String) -> Unit,
    onScheduleClick: (Node) -> Unit
) {
    item {
        SectionHeader(title = "Estructura de nodos", onAdd = { onAddNode(null) })
    }

    if (nodes.isEmpty()) {
        item {
            NodeItem("Pecho", depth = 0, hasChildren = true)
            NodeItem("Press banca", depth = 1, meta = "4×8")
            NodeItem("Press inclinado", depth = 1, meta = "3×10")
            NodeItem("Hombro", depth = 0, hasChildren = true)
            NodeItem("Elevaciones laterales", depth = 1, meta = "3×15")
        }
    } else {
        val rootNodes = nodes.filter { it.parentId == null }
        items(rootNodes) { node ->
            NodeHierarchy(
                node = node,
                allNodes = nodes,
                nodeSchedules = nodeSchedules,
                depth = 0,
                onAddChild = onAddNode,
                onDeleteNode = onDeleteNode,
                onScheduleClick = onScheduleClick
            )
        }
    }

    item {
        Spacer(modifier = Modifier.height(12.dp))
        AddDashedButton(text = "agregar grupo", onClick = { onAddNode(null) })
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@androidx.compose.runtime.Composable
private fun NodeHierarchy(
    node: Node,
    allNodes: List<Node>,
    nodeSchedules: Map<String, List<NodeSchedule>>,
    depth: Int,
    onAddChild: (String) -> Unit,
    onDeleteNode: (String) -> Unit,
    onScheduleClick: (Node) -> Unit
) {
    val children = allNodes.filter { it.parentId == node.id }
    val schedules = nodeSchedules[node.id] ?: emptyList()
    
    NodeItem(
        name = node.name,
        depth = depth,
        hasChildren = children.isNotEmpty(),
        hasSchedules = schedules.isNotEmpty(),
        onAddClick = { onAddChild(node.id) },
        onDeleteClick = { onDeleteNode(node.id) },
        onScheduleClick = { onScheduleClick(node) }
    )
    children.forEach { child ->
        NodeHierarchy(child, allNodes, nodeSchedules, depth + 1, onAddChild, onDeleteNode, onScheduleClick)
    }
}
