package com.alan.routineos.data.local

import androidx.room.TypeConverter
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.ui.features.template_builder.sections.ContextCategory
import com.alan.routineos.ui.features.template_builder.sections.TimeMode

class Converters {
    @TypeConverter
    fun fromFieldType(value: FieldType) = value.name

    @TypeConverter
    fun toFieldType(value: String) = FieldType.valueOf(value)

    @TypeConverter
    fun fromNodeStatus(value: NodeStatus) = value.name

    @TypeConverter
    fun toNodeStatus(value: String) = NodeStatus.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus) = value.name

    @TypeConverter
    fun toSyncStatus(value: String) = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromInstanceStatus(value: InstanceStatus) = value.name

    @TypeConverter
    fun toInstanceStatus(value: String) = InstanceStatus.valueOf(value)

    @TypeConverter
    fun fromOverrideType(value: OverrideType) = value.name

    @TypeConverter
    fun toOverrideType(value: String) = OverrideType.valueOf(value)

    @TypeConverter
    fun fromContextCategory(value: ContextCategory) = value.name

    @TypeConverter
    fun toContextCategory(value: String) = ContextCategory.valueOf(value)

    @TypeConverter
    fun fromTimeMode(value: TimeMode) = value.name

    @TypeConverter
    fun toTimeMode(value: String) = TimeMode.valueOf(value)
}
