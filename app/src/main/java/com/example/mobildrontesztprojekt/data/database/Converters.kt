package com.example.mobildrontesztprojekt.data.database

import androidx.room.TypeConverter
import com.example.mobildrontesztprojekt.data.entity.*

class Converters {
    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = UserRole.valueOf(v)

    @TypeConverter fun fromDroneStatus(v: DroneStatus): String = v.name
    @TypeConverter fun toDroneStatus(v: String): DroneStatus = DroneStatus.valueOf(v)

    @TypeConverter fun fromJobStatus(v: JobStatus): String = v.name
    @TypeConverter fun toJobStatus(v: String): JobStatus = JobStatus.valueOf(v)

    @TypeConverter fun fromNodeType(v: NodeType): String = v.name
    @TypeConverter fun toNodeType(v: String): NodeType = NodeType.valueOf(v)

    @TypeConverter fun fromShelfSide(v: ShelfSide): String = v.name
    @TypeConverter fun toShelfSide(v: String): ShelfSide = ShelfSide.valueOf(v)
}
