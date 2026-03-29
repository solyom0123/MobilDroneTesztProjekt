package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun findByCredentials(email: String, passwordHash: String): User?

    @Query("SELECT * FROM users WHERE companyId = :companyId")
    fun getByCompany(companyId: Long): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
