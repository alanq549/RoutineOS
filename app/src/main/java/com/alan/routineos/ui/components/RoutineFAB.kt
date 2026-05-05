package com.alan.routineos.ui.components


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.NeonEmerald

@Composable
fun RoutineFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = NeonEmerald,
        contentColor = BgDark,
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Añadir actividad")
    }
}