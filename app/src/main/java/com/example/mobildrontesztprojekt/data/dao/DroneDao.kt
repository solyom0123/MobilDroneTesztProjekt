package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.Drone
import kotlinx.coroutines.flow.Flow

@Dao
interface DroneDao {
    @Query("SELECT * FROM drones WHERE companyId = :companyId")
    fun getByCompany(companyId: Long): Flow<List<Drone>>

    @Query("SELECT * FROM drones")
    fun getAll(): Flow<List<Drone>>

    @Query("SELECT * FROM drones WHERE id = :id")
    suspend fun getById(id: Long): Drone?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drone: Drone): Long

    @Update
    suspend fun update(drone: Drone)

    @Delete
    suspend fun delete(drone: Drone)
}
