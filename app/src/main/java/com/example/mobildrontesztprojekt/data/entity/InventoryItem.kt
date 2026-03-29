package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Raktarkeszlet elem. Minden termeknek pontos meretadatai vannak.
 * shelfLocationId null lehet, ha nincs meg berakva polcra.
 */
@Entity(
    tableName = "inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = Warehouse::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShelfLocation::class,
            parentColumns = ["id"],
            childColumns = ["shelfLocationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("warehouseId"), Index("shelfLocationId")]
)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val shelfLocationId: Long? = null,
    val name: String,
    val sku: String,
    val widthCm: Float,
    val heightCm: Float,
    val depthCm: Float,
    val weightKg: Float,
    val quantity: Int
)
