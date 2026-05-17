package com.alan.routineos.ui.features.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayActivityCard(
    time: String,
    title: String,
    subtitle: String? = null,
    statusLabel: String,
    statusColor: Color,
    barColor: Color,
    isCancelled: Boolean = false,
    hasConflict: Boolean = false,
    resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
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
        // 1. TIMELINE TIME (OPERATIVE STYLE)
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(top = 16.dp, end = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            val parts = time.split(" ")
            Text(
                text = parts[0],
                style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = ColorText
            )
            if (parts.size > 1) {
                Text(
                    text = parts[1],
                    style = MetaMono.copy(fontSize = 8.sp),
                    color = ColorTextDim
                )
            }
        }

        // 2. TIMELINE AXIS
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

        // 3. OPERATIVE CONTENT CARD
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val color = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFF333333)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(
                        if (direction == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp, end = 16.dp)
        ) {
            Surface(
                color = ColorSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = TitleNode.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = ColorText,
                            modifier = Modifier.weight(1f)
                        )
                        if (resolvedNodes.isNotEmpty()) {
                            IconButton(
                                onClick = { expanded = !expanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = ColorTextDim,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MetaMono.copy(fontSize = 10.sp),
                            color = ColorTextDim
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Minimal Status Tag
                        Surface(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = statusLabel.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }

                        if (hasConflict) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFE65100).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "CONFLICT",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            resolvedNodes.forEach { node ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = (node.depth * 12).dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val prefix = if (node.depth > 0) "• " else ""
                                    Text(
                                        text = "$prefix${node.name}",
                                        style = TitleNode.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (node.depth == 0) FontWeight.Medium else FontWeight.Normal
                                        ),
                                        color = if (node.depth == 0) ColorText else ColorTextDim,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (node.valueSummary != null) {
                                        Text(
                                            node.valueSummary,
                                            style = MetaMono.copy(fontSize = 9.sp),
                                            color = Color(0xFF444444)
                                        )
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
