package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.session.UserManager
import kotlinx.coroutines.launch

class UserViewModel(
    private val userManager: UserManager
) : ViewModel() {

    val userState = userManager.state

    init {
        viewModelScope.launch {
            userManager.loadLocal()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            userManager.fetchUser()
        }
    }
}