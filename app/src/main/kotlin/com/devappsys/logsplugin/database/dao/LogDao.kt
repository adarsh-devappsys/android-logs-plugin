package com.devappsys.logsplugin.database.dao

import androidx.room.*
import com.devappsys.logsplugin.database.models.LogEntity

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<LogEntity>)

    @Query("SELECT * FROM logs ORDER BY timeStamp ASC")
    suspend fun getAllLogs(): List<LogEntity>

    @Query("DELETE FROM logs WHERE logID IN (:ids)")
    suspend fun deleteLogsByIds(ids: List<String>)

    @Query("DELETE FROM logs")
    suspend fun clearAllLogs()
}