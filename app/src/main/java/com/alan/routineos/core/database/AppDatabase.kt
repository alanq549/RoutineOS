package com.alan.routineos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alan.routineos.data.local.Converters
import com.alan.routineos.data.local.dao.DayInstanceDao
import com.alan.routineos.data.local.dao.ExecutionFieldValueDao
import com.alan.routineos.data.local.dao.FieldValueDao
import com.alan.routineos.data.local.dao.MetadataSchemaDao
import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.NodeOverrideDao
import com.alan.routineos.data.local.dao.NodeScheduleDao
import com.alan.routineos.data.local.dao.NodeTypeDao
import com.alan.routineos.data.local.dao.PlanningItemDao
import com.alan.routineos.data.local.dao.ScheduleDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.dao.SyncDao
import com.alan.routineos.data.local.dao.TemplateDao
import com.alan.routineos.data.local.dao.UserDao
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.ExecutionFieldValue
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeOverride
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.data.local.entities.PlanningItemEntity
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        Node::class,
        NodeType::class,
        NodeMetadataSchema::class,
        NodeFieldValue::class,
        ExecutionFieldValue::class,
        RoutineTemplate::class,
        Schedule::class,
        DayInstance::class,
        NodeOverride::class,
        ScheduleException::class,
        NodeSchedule::class,
        PlanningItemEntity::class
    ],
    version = 9, // synchronized with exported schemas
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun nodeDao(): NodeDao
    abstract fun nodeTypeDao(): NodeTypeDao
    abstract fun metadataSchemaDao(): MetadataSchemaDao
    abstract fun fieldValueDao(): FieldValueDao
    abstract fun executionFieldValueDao(): ExecutionFieldValueDao
    abstract fun templateDao(): TemplateDao
    abstract fun dayInstanceDao(): DayInstanceDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun nodeOverrideDao(): NodeOverrideDao
    abstract fun scheduleExceptionDao(): ScheduleExceptionDao
    abstract fun syncDao(): SyncDao
    abstract fun nodeScheduleDao(): NodeScheduleDao
    abstract fun planningItemDao(): PlanningItemDao
}
