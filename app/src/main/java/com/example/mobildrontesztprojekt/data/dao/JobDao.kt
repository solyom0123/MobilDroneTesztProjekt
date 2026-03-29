package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.Job
import com.example.mobildrontesztprojekt.data.entity.JobItem
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE droneId = :droneId ORDER BY createdAt DESC")
    fun getByDrone(droneId: Long): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE assignedUserId = :userId ORDER BY createdAt DESC")
    fun getByUser(userId: Long): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getById(id: Long): Job?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job): Long

    @Update
    suspend fun updateJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)

    // JobItems
    @Query("SELECT * FROM job_items WHERE jobId = :jobId")
    fun getItemsByJob(jobId: Long): Flow<List<JobItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItem(jobItem: JobItem): Long

    @Delete
    suspend fun deleteJobItem(jobItem: JobItem)
}
