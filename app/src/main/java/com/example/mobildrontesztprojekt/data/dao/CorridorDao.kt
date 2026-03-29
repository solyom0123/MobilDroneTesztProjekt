package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.Corridor
import kotlinx.coroutines.flow.Flow

@Dao
interface CorridorDao {
    @Query("SELECT * FROM corridors")
    fun getAll(): Flow<List<Corridor>>

    @Query("SELECT * FROM corridors WHERE warehouseId = :warehouseId")
    fun getByWarehouse(warehouseId: Long): Flow<List<Corridor>>

    @Query("SELECT * FROM corridors WHERE id = :id")
    suspend fun getById(id: Long): Corridor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(corridor: Corridor): Long

    @Update
    suspend fun update(corridor: Corridor)

    @Delete
    suspend fun delete(corridor: Corridor)
}
