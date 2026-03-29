package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class JobStatus { PENDING, IN_PROGRESS, COMPLETED, CANCELLED }

@Entity(
    tableName = "jobs",
    foreignKeys = [
        ForeignKey(
            entity = Drone::class,
            parentColumns = ["id"],
            childColumns = ["droneId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["assignedUserId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("droneId"), Index("assignedUserId")]
)
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val droneId: Long,
    val assignedUserId: Long? = null,
    val status: JobStatus = JobStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
