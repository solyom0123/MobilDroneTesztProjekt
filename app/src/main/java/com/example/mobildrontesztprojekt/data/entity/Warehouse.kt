package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "warehouses",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("companyId")]
)
data class Warehouse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val name: String,
    val address: String
)
