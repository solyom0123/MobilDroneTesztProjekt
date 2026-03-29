package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_items",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventoryItem::class,
            parentColumns = ["id"],
            childColumns = ["inventoryItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("jobId"), Index("inventoryItemId")]
)
data class JobItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val inventoryItemId: Long,
    val quantity: Int
)
