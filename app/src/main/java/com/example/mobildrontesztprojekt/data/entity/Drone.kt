package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DroneStatus { IDLE, ACTIVE, CHARGING, MAINTENANCE }

/**
 * Dron entitás.
 * homeBaseId → a dron saját DRONE_BASE típusú UWB csomópontja (opcionális).
 */
@Entity(
    tableName = "drones",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UWBNode::class,
            parentColumns = ["id"],
            childColumns = ["homeBaseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("companyId"), Index("homeBaseId")]
)
data class Drone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val model: String,
    val serialNumber: String,
    val status: DroneStatus = DroneStatus.IDLE,
    val homeBaseId: Long? = null
)
