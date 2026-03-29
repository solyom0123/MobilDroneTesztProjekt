package com.example.mobildrontesztprojekt.data.dao

import androidx.room.*
import com.example.mobildrontesztprojekt.data.entity.AppKey
import kotlinx.coroutines.flow.Flow

@Dao
interface AppKeyDao {

    @Query("SELECT * FROM app_key WHERE id = 1 LIMIT 1")
    fun observe(): Flow<AppKey?>

    @Query("SELECT * FROM app_key WHERE id = 1 LIMIT 1")
    suspend fun get(): AppKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(key: AppKey)

    @Query("DELETE FROM app_key")
    suspend fun clear()
}