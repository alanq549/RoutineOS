package com.alan.routineos.ui.features.today.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayActivityCard(
    id: String,
    time: String,
    title: String,
    subtitle: String? = null,
    statusLabel: String,
    statusColor: Color,
    barColor: Color,
    isCancelled: Boolean = false,
    hasConflict: Boolean = false,
    resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    onNodeToggle: (String) -> Unit = {},
    onNodeClick: (String) -> Unit = {},
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onComplete()
            dismissState.reset()
        } else if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSkip()
            dismissState.reset()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .alpha(if (isCancelled) 0.4f else 1f)
    ) {
        // 1. TIEMPO
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(top = 16.dp, end = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = time,
                style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = ColorText
            )
        }

        // 2. EJE
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(barColor, CircleShape)
                    .border(2.dp, ColorBg, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(ColorBorder.copy(alpha = 0.4f))
            )
        }

        // 3. CONTENIDO
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val bgColor = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFF333333)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(
                        if (direction == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Check else Icons.Default.Close,
                        null, tint = Color.White
                    )
                }
            },
            modifier = Modifier.weight(1f).padding(bottom = 12.dp, end = 16.dp)
        ) {
            Surface(
                color = ColorSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNodeClick(id) },
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasConflict) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Conflict",
                                tint = Color.Yellow,
                                modifier = Modifier.size(16.dp).padding(end = 6.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = TitleNode.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = ColorText,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = statusLabel.uppercase(),
                                style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        if (resolvedNodes.isNotEmpty()) {
                            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    null, tint = ColorTextDim, modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (subtitle != null) {
                        Text(subtitle, style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // DESGLOSE DE BLOQUES INTERNOS
                    AnimatedVisibility(visible = expanded && resolvedNodes.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            resolvedNodes.forEach { node ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = ((node.depth - 1) * 12).dp)
                                        .background(if (node.isCompleted) Color.White.copy(alpha = 0.03f) else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable(enabled = node.fields.isNotEmpty()) {
                                            Log.d("TODAY_DEBUG", "TODAY OPEN EXECUTE nodeId=${node.id} name=${node.name} depth=${node.depth}")
                                            onNodeClick(node.id)
                                        }
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (node.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            null, 
                                            tint = if (node.isCompleted) ColorExec else ColorTextDim, 
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onNodeToggle(node.id) }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = node.name,
                                            modifier = Modifier.weight(1f),
                                            style = TitleNode.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, textDecoration = if (node.isCompleted) TextDecoration.LineThrough else null),
                                            color = if (node.isCompleted) ColorTextDim else ColorText
                                        )
                                        if (node.timeLabel != null) {
                                            Text(node.timeLabel, style = MetaMono.copy(fontSize = 9.sp), color = ColorExec)
                                        }
                                    }
                                    
                                    if (node.fields.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier.padding(top = 6.dp, start = 26.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            node.fields.forEach { field ->
                                                Column {
                                                    Text(field.label.uppercase(), style = MetaMono.copy(fontSize = 7.sp, letterSpacing = 0.5.sp), color = ColorTextDim)
                                                    Text(field.value, style = MetaMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = if (node.isCompleted) ColorTextDim else Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
