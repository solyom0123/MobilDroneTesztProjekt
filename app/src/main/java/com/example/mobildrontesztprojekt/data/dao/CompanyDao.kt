package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies")
    fun getAll(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getById(id: Long): Company?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company): Long

    @Update
    suspend fun update(company: Company)

    @Delete
    suspend fun delete(company: Company)
}
