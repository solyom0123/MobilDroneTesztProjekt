package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE warehouseId = :warehouseId")
    fun getByWarehouse(warehouseId: Long): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE shelfLocationId = :shelfLocationId")
    fun getByShelfLocation(shelfLocationId: Long): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: Long): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)
}
