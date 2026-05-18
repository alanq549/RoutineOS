package com.alan.routineos.core.di

import com.alan.routineos.core.util.DeviceInfoProvider
import com.alan.routineos.core.util.DeviceInfoProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(
        deviceInfoProviderImpl: DeviceInfoProviderImpl
    ): DeviceInfoProvider
}
