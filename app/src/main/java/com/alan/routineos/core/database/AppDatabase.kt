package com.alan.routineos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alan.routineos.data.local.Converters
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        Node::class,
        NodeType::class,
        NodeMetadataSchema::class,
        NodeFieldValue::class,
        RoutineTemplate::class,
        Schedule::class,
        DayInstance::class,
        NodeOverride::class,
        ScheduleException::class
    ],
    version = 2, // Incrementamos versión por cambios estructurales masivos
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun nodeDao(): NodeDao
    abstract fun nodeTypeDao(): NodeTypeDao
    abstract fun metadataSchemaDao(): MetadataSchemaDao
    abstract fun fieldValueDao(): FieldValueDao
    abstract fun templateDao(): TemplateDao
    abstract fun dayInstanceDao(): DayInstanceDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun nodeOverrideDao(): NodeOverrideDao
    abstract fun scheduleExceptionDao(): ScheduleExceptionDao
}
