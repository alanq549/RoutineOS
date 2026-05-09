package com.alan.routineos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.theme.TextSecondary

@Composable
fun UserMenu(
    userName: String?,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = NeonEmerald,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(BgDark.copy(alpha = 0.95f))
                .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp)
        ) {
            if (userName != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "MI CUENTA",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                        expanded = false
                        onProfileClick()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
            }

            DropdownMenuItem(
                text = {
                    Text(
                        "CERRAR SESIÓN",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}
