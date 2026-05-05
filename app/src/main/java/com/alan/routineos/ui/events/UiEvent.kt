package com.alan.routineos.ui.events

sealed class UiEvent {

    data class ShowToast(val message: String) : UiEvent()

    data class ShowSnackbar(val message: String) : UiEvent()

    data class Navigate(val route: String) : UiEvent()
}