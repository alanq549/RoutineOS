package com.alan.routineos.core.di

import android.content.Context
import androidx.room.Room
import com.alan.routineos.core.database.AppDatabase
import com.alan.routineos.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideNodeDao(db: AppDatabase): NodeDao = db.nodeDao()

    @Provides
    fun provideNodeTypeDao(db: AppDatabase): NodeTypeDao = db.nodeTypeDao()

    @Provides
    fun provideMetadataSchemaDao(db: AppDatabase): MetadataSchemaDao = db.metadataSchemaDao()

    @Provides
    fun provideFieldValueDao(db: AppDatabase): FieldValueDao = db.fieldValueDao()

    @Provides
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideDayInstanceDao(db: AppDatabase): DayInstanceDao = db.dayInstanceDao()

    @Provides
    fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideNodeOverrideDao(db: AppDatabase): NodeOverrideDao = db.nodeOverrideDao()

    @Provides
    fun provideScheduleExceptionDao(db: AppDatabase): ScheduleExceptionDao = db.scheduleExceptionDao()

    @Provides
    fun provideSyncDao(db: AppDatabase): SyncDao = db.syncDao()

    @Provides
    fun provideNodeScheduleDao(db: AppDatabase): NodeScheduleDao = db.nodeScheduleDao()

    @Provides
    fun providePlanningItemDao(db: AppDatabase): PlanningItemDao = db.planningItemDao()
}
