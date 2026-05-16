package com.alan.routineos.ui.core.startup

sealed class AppStartupState {
    data object Loading : AppStartupState()
    data object ShowOnboarding : AppStartupState()
    data object ShowHome : AppStartupState()
}
