package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Polchely egy raktárban.
 *
 * Koordináták (UWB-kalibrált, méterben a raktár origójától):
 *   posX, posY, posZ
 *
 * Fizikai méretek:
 *   widthCm, heightCm, depthCm, maxWeightKg
 *
 * Kapcsolatok:
 *   shelfRowId  → a polcsor amelyikbe tartozik (opcionális)
 *   uwbAnchorId → a legközelebbi / kalibráló UWB csomópont (opcionális)
 */
@Entity(
    tableName = "shelf_locations",
    foreignKeys = [
        ForeignKey(
            entity = Warehouse::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShelfRow::class,
            parentColumns = ["id"],
            childColumns = ["shelfRowId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UWBNode::class,
            parentColumns = ["id"],
            childColumns = ["uwbAnchorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("warehouseId"),
        Index("shelfRowId"),
        Index("uwbAnchorId")
    ]
)
data class ShelfLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val shelfRowId: Long? = null,
    val uwbAnchorId: Long? = null,
    val code: String,
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val widthCm: Float,
    val heightCm: Float,
    val depthCm: Float,
    val maxWeightKg: Float
)
