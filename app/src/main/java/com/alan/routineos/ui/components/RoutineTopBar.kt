package com.alan.routineos.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.state.UserState
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.JetBrainsMono
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.theme.TextPrimary
import com.alan.routineos.ui.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineTopBar(
    userName: String?,
    onLogout: () -> Unit
) {
    val currentDate = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    )

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = currentDate.uppercase(),
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    color = NeonEmerald
                )

                Text(
                    text = "RoutineOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-1).sp
                )

                if (userName != null) {
                    Text(
                        text = "Hola $userName",
                        fontSize = 12.sp,
                        color = TextPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                UserMenu(onLogout = onLogout,
                    userName = userName)
            }
        },
        actions = { Spacer(modifier = Modifier.size(48.dp)) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = BgDark.copy(alpha = 0.9f)
        )
    )
}