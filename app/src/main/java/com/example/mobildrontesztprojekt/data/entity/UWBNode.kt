package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * UWB csomópont típusa:
 *  - ANCHOR     : fix UWB horgony (pozícionálás referencia)
 *  - DROP_POINT : lerakóhely (dron ide hozza / viszi az árut)
 *  - DRONE_BASE : dron home base (töltő + parkológ állomás)
 */
enum class NodeType { ANCHOR, DROP_POINT, DRONE_BASE }

/**
 * UWB csomópont egy raktárban / folyosón.
 * posX/Y/Z: pontos fizikai koordináta méterben (UWB által mért).
 * corridorId: opcionális – melyik folyosóhoz tartozik.
 */
@Entity(
    tableName = "uwb_nodes",
    foreignKeys = [
        ForeignKey(
            entity = Warehouse::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Corridor::class,
            parentColumns = ["id"],
            childColumns = ["corridorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("warehouseId"), Index("corridorId")]
)
data class UWBNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warehouseId: Long,
    val corridorId: Long? = null,
    val name: String,
    val code: String,
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val nodeType: NodeType
)
