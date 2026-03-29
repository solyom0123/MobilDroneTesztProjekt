package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Melyik oldalon van a polcsor a folyosóhoz képest. */
enum class ShelfSide { LEFT, RIGHT }

/**
 * Polcsor: egy folyosó egyik oldalán lévő polchelyek összefoglaló csoportja.
 */
@Entity(
    tableName = "shelf_rows",
    foreignKeys = [
        ForeignKey(
            entity = Corridor::class,
            parentColumns = ["id"],
            childColumns = ["corridorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("corridorId")]
)
data class ShelfRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val corridorId: Long,
    val code: String,
    val side: ShelfSide,
    val description: String = ""
)
