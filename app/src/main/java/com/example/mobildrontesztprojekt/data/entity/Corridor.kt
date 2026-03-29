package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Folyosó egy raktárban.
 * A startX/Y → endX/Y a folyosó tengelyének két végpontja (méterben).
 * widthCm: folyosó fizikai szélessége, heightCm: belmagasság.
 */
@Entity(
    tableName = "corridors",
    foreignKeys = [
        ForeignKey(
            entity = Warehouse::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("warehouseId")]
)
data class Corridor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val name: String,
    val code: String,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val widthCm: Float,
    val heightCm: Float
)
