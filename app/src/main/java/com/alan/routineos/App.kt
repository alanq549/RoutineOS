package com.alan.routineos

import android.app.Application
import com.alan.routineos.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)

        // 🔥 cargar sesión al iniciar app
        CoroutineScope(Dispatchers.IO).launch {
            container.sessionManager.loadSession()
        }
    }
}