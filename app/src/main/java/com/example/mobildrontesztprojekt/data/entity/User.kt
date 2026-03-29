package com.example.mobildrontesztprojekt.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole { ADMIN, MANAGER, TECHNICAL }

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("companyId"), Index("email", unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val companyId: Long? = null
)
