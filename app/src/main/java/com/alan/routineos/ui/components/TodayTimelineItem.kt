package com.alan.routineos.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayTimelineItem(
    node: Node,
    depth: Int = 0,
    onToggleCompletion: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isCompleted = node.status == NodeStatus.COMPLETED
    val isActive = node.status == NodeStatus.ACTIVE
    
    val alpha = if (isCompleted) 0.55f else 1.0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 24).dp)
            .animateContentSize()
            .alpha(alpha)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator (Checkmark circle)
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isCompleted) ColorExec else Color.Transparent,
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isCompleted) ColorExec else ColorBorder,
                    shape = CircleShape
                )
                .clickable { onToggleCompletion() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Node Content
        Surface(
            modifier = Modifier.weight(1f),
            color = if (isActive) ColorExec.copy(alpha = 0.05f) else Color.Transparent,
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .background(ColorExec)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = node.name,
                        style = TitleNode,
                        color = ColorText,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                    node.scheduledTime?.let {
                        Text(
                            text = it,
                            style = MetaMono,
                            color = ColorTextDim
                        )
                    }
                }
            }
        }
    }
}
