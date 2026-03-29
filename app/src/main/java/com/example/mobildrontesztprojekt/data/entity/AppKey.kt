package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DJI App Key tárolása az adatbázisban.
 * Mindig csak 1 rekord létezik (id = 1).
 */
@Entity(tableName = "app_key")
data class AppKey(
    @PrimaryKey val id: Int = 1,
    val djiAppKey: String
)