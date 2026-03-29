package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.ShelfLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfLocationDao {
    @Query("SELECT * FROM shelf_locations")
    fun getAll(): Flow<List<ShelfLocation>>

    @Query("SELECT * FROM shelf_locations WHERE warehouseId = :warehouseId")
    fun getByWarehouse(warehouseId: Long): Flow<List<ShelfLocation>>

    @Query("SELECT * FROM shelf_locations WHERE id = :id")
    suspend fun getById(id: Long): ShelfLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shelfLocation: ShelfLocation): Long

    @Update
    suspend fun update(shelfLocation: ShelfLocation)

    @Delete
    suspend fun delete(shelfLocation: ShelfLocation)
}
