package com.alan.routineos.ui.components
import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.NeonEmerald
import kotlinx.coroutines.delay

@Composable
fun NotificationHost(
    message: String?,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    // Usamos AnimatedVisibility para que la entrada y salida sea fluida
    AnimatedVisibility(
        visible = visible && message != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(NeonEmerald, Color.White.copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                color = NeonEmerald.copy(alpha = 0.95f), // Un poco más sólido para legibilidad
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = message ?: "",
                        color = BgDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Lógica de auto-ocultado
    LaunchedEffect(visible, message) {
        if (visible && message != null) {
            delay(3000) // 3 segundos es el estándar para lectura cómoda
            onDismiss()
        }
    }
}