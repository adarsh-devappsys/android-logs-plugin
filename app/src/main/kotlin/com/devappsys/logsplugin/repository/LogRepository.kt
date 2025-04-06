package com.devappsys.logsplugin.repository

import com.devappsys.logsplugin.data.Config
import com.devappsys.logsplugin.database.models.LogEntity
import log.Log.LogEvent
import log.Log.LogResponse

interface LogRepository {

    suspend fun saveLogLocally(event: LogEntity)
    suspend fun saveLogsLocally(events: List<LogEntity>)
    suspend fun getAllLogs(): List<LogEntity>
    suspend fun deleteAllLogs()
    suspend fun deleteLogsByIds(ids: List<String>)
    suspend fun uploadLogEvent(event: LogEvent): LogResponse

    suspend fun uploadLogsEvent(events: List<LogEvent>):LogResponse
    suspend fun isNetworkAvailable(): Boolean
   suspend fun syncPendingLogs()
    // 🆕 Config persistence methods
    fun saveConfig(config: Config)
    fun getConfig(): Config?

}