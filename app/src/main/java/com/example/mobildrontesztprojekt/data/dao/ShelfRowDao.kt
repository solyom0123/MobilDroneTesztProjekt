package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.ShelfRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfRowDao {
    @Query("SELECT * FROM shelf_rows")
    fun getAll(): Flow<List<ShelfRow>>

    @Query("SELECT * FROM shelf_rows WHERE corridorId = :corridorId")
    fun getByCorridor(corridorId: Long): Flow<List<ShelfRow>>

    @Query("SELECT * FROM shelf_rows WHERE id = :id")
    suspend fun getById(id: Long): ShelfRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shelfRow: ShelfRow): Long

    @Update
    suspend fun update(shelfRow: ShelfRow)

    @Delete
    suspend fun delete(shelfRow: ShelfRow)
}
