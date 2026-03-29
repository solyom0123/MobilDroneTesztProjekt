package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.NodeType
import com.example.mobildrontesztprojekt.data.entity.UWBNode
import kotlinx.coroutines.flow.Flow

@Dao
interface UWBNodeDao {
    @Query("SELECT * FROM uwb_nodes")
    fun getAll(): Flow<List<UWBNode>>

    @Query("SELECT * FROM uwb_nodes WHERE warehouseId = :warehouseId")
    fun getByWarehouse(warehouseId: Long): Flow<List<UWBNode>>

    @Query("SELECT * FROM uwb_nodes WHERE corridorId = :corridorId")
    fun getByCorridor(corridorId: Long): Flow<List<UWBNode>>

    @Query("SELECT * FROM uwb_nodes WHERE nodeType = :nodeType")
    fun getByType(nodeType: NodeType): Flow<List<UWBNode>>

    @Query("SELECT * FROM uwb_nodes WHERE id = :id")
    suspend fun getById(id: Long): UWBNode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: UWBNode): Long

    @Update
    suspend fun update(node: UWBNode)

    @Delete
    suspend fun delete(node: UWBNode)
}
