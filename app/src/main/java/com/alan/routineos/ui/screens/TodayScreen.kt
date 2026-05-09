package com.alan.routineos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.ui.components.TodayTimelineItem
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.TodayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel(),
    onNavigateToExecute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddNodeSheet by remember { mutableStateOf(false) }
    var selectedNodeForOptions by remember { mutableStateOf<Node?>(null) }

    Scaffold(
        topBar = {
            TodayHeader(
                currentTime = uiState.currentTime,
                progress = if (uiState.nodes.isEmpty()) 0f 
                          else uiState.nodes.count { it.status == NodeStatus.COMPLETED }.toFloat() / uiState.nodes.size
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNodeSheet = true },
                containerColor = ColorExec,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add node")
            }
        },
        containerColor = ColorBg
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorExec)
            }
        } else if (uiState.instance == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No hay rutina para hoy", color = ColorTextDim, style = TitleNode)
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues)) {
                CurrentActivityBanner(uiState.nodes, uiState.currentTime)
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val rootNodes = uiState.nodes.filter { it.parentId == null }
                    
                    items(rootNodes, key = { it.id }) { node ->
                        NodeWithChildren(
                            node = node,
                            allNodes = uiState.nodes,
                            depth = 0,
                            onToggleCompletion = { viewModel.toggleNodeCompletion(it) },
                            onClick = { 
                                val hasChildren = uiState.nodes.any { child -> child.parentId == node.id }
                                if (!hasChildren) onNavigateToExecute(node.id)
                            },
                            onLongClick = { selectedNodeForOptions = it }
                        )
                    }
                }
            }
        }

        if (showAddNodeSheet) {
            AddNodeBottomSheet(
                uiState = uiState,
                onDismiss = { showAddNodeSheet = false },
                onAdd = { name, typeId ->
                    viewModel.addAdHocNode(name, typeId, null)
                    showAddNodeSheet = false
                }
            )
        }

        if (selectedNodeForOptions != null) {
            NodeOptionsBottomSheet(
                node = selectedNodeForOptions!!,
                onDismiss = { selectedNodeForOptions = null },
                onStatusChange = { status ->
                    viewModel.updateNodeStatus(selectedNodeForOptions!!, status)
                    selectedNodeForOptions = null
                }
            )
        }
    }
}

@Composable
fun NodeWithChildren(
    node: Node,
    allNodes: List<Node>,
    depth: Int,
    onToggleCompletion: (Node) -> Unit,
    onClick: (Node) -> Unit,
    onLongClick: (Node) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val children = allNodes.filter { it.parentId == node.id }

    Column {
        TodayTimelineItem(
            node = node,
            depth = depth,
            onToggleCompletion = { onToggleCompletion(node) },
            onClick = { 
                if (children.isNotEmpty()) expanded = !expanded
                else onClick(node)
            },
            onLongClick = { onLongClick(node) }
        )
        
        if (expanded && children.isNotEmpty()) {
            children.forEach { child ->
                NodeWithChildren(child, allNodes, depth + 1, onToggleCompletion, onClick, onLongClick)
            }
        }
    }
}

@Composable
fun TodayHeader(currentTime: String, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorSurface)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hoy",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ColorText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentTime,
                    style = MonoTimer.copy(fontSize = 24.sp),
                    color = ColorTextDim
                )
            }
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(48.dp),
                    color = ColorExec,
                    trackColor = ColorBorder,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MetaMono,
                    color = ColorText
                )
            }
        }
    }
}

@Composable
fun CurrentActivityBanner(nodes: List<Node>, currentTime: String) {
    val currentActivity = nodes.filter { it.scheduledTime != null }
        .sortedBy { it.scheduledTime }
        .lastOrNull { it.scheduledTime!! <= currentTime }

    if (currentActivity != null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = ColorExec.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ColorExec)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AHORA", style = MetaMono, color = ColorExec)
                    Text(currentActivity.name, style = TitleNode, color = ColorText)
                }
                Text(currentActivity.scheduledTime ?: "", style = MonoTimer.copy(fontSize = 18.sp), color = ColorText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeBottomSheet(
    uiState: com.alan.routineos.ui.viewmodel.TodayUiState,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTypeId by remember { mutableStateOf(uiState.nodeTypes.firstOrNull()?.id ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        contentColor = ColorText
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Agregar actividad", style = MaterialTheme.typography.headlineSmall, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Tipo", style = TitleNode, color = ColorTextDim)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                uiState.nodeTypes.forEach { type ->
                    FilterChip(
                        selected = selectedTypeId == type.id,
                        onClick = { selectedTypeId = type.id },
                        label = { Text(type.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onAdd(name, selectedTypeId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                enabled = name.isNotBlank() && selectedTypeId.isNotBlank()
            ) {
                Text("Agregar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeOptionsBottomSheet(
    node: Node,
    onDismiss: () -> Unit,
    onStatusChange: (NodeStatus) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        contentColor = ColorText
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(node.name, style = MaterialTheme.typography.headlineSmall, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("Saltar") },
                modifier = Modifier.clickable { onStatusChange(NodeStatus.SKIPPED) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            ListItem(
                headlineContent = { Text("Posponer") },
                modifier = Modifier.clickable { onStatusChange(NodeStatus.POSTPONED) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            ListItem(
                headlineContent = { Text("Pendiente") },
                modifier = Modifier.clickable { onStatusChange(NodeStatus.PENDING) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
