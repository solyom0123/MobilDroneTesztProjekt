package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses")
    fun getAll(): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE companyId = :companyId")
    fun getByCompany(companyId: Long): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getById(id: Long): Warehouse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouse: Warehouse): Long

    @Update
    suspend fun update(warehouse: Warehouse)

    @Delete
    suspend fun delete(warehouse: Warehouse)
}
