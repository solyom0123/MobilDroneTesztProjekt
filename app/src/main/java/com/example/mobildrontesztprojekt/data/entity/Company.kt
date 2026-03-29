package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val phone: String
)
