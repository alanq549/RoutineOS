package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.session.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
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
