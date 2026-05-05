package com.alan.routineos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alan.routineos.core.di.AppContainer
import com.alan.routineos.ui.navigation.AppNavHost
import com.alan.routineos.ui.theme.RoutineOSTheme
import com.alan.routineos.ui.viewmodel.AuthViewModel
import com.alan.routineos.ui.viewmodel.UserViewModel


class MainActivity : ComponentActivity() {

    private val container by lazy {
        (application as App).container
    }

    private val authViewModel by lazy {
        AuthViewModel(
            registerUseCase = container.registerUseCase,
            loginUseCase = container.loginUseCase,
            verifyEmailUseCase = container.verifyEmailCodeUseCase,
            sessionManager = container.sessionManager,
            userManager = container.userManager
        )
    }

    private val userViewModel by lazy {
        UserViewModel(
            userManager = container.userManager,

        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            RoutineOSTheme {
                AppNavHost(
                    authViewModel = authViewModel,
                    sessionManager = container.sessionManager,
                    userViewModel = userViewModel
                )
            }
        }
    }
}