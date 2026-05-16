package com.alan.routineos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alan.routineos.ui.core.startup.AppStartupState
import com.alan.routineos.ui.core.startup.AppStartupViewModel
import com.alan.routineos.ui.navigation.AppNavHost
import com.alan.routineos.ui.theme.RoutineOSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val startupViewModel: AppStartupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Mantener el splash screen nativo hasta que el ViewModel determine el siguiente paso
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.state.value is AppStartupState.Loading
        }

        enableEdgeToEdge()
        setContent {
            RoutineOSTheme {
                AppNavHost(startupViewModel = startupViewModel)
            }
        }
    }
}
