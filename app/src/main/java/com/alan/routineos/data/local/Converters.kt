package com.alan.routineos.data.local

import androidx.room.TypeConverter
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.InstanceStatus
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.OverrideType
import com.alan.routineos.data.local.entities.SyncStatus
import com.alan.routineos.data.local.entities.TemporalMode

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
    fun fromTemporalMode(value: TemporalMode) = value.name

    @TypeConverter
    fun toTemporalMode(value: String) = TemporalMode.valueOf(value)
}
