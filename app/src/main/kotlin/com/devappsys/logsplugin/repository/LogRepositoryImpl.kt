package com.devappsys.logsplugin.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import com.devappsys.logsplugin.data.Config
import com.devappsys.logsplugin.database.dao.LogDao
import com.devappsys.logsplugin.database.models.LogEntity
import com.devappsys.logsplugin.grpc.GrpcClient
import com.devappsys.logsplugin.grpc.LogEventBuilder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import log.Log.LogEvent
import log.Log.LogResponse

class LogRepositoryImpl constructor(
    private val logDao: LogDao,
    private val grpcClient: GrpcClient,
    private val context: Context
) : LogRepository {

    private var isSyncing = false

    override suspend fun saveLogLocally(event: LogEntity) {
        logDao.insertLog(event)
    }

    override suspend fun saveLogsLocally(events: List<LogEntity>) {
        logDao.insertLogs(events)
    }

    override suspend fun getAllLogs(): List<LogEntity> {
        return logDao.getAllLogs()
    }

    override suspend fun deleteAllLogs() {
        logDao.clearAllLogs()
    }

    override suspend fun deleteLogsByIds(ids: List<String>) {
        logDao.deleteLogsByIds(ids)
    }

    override suspend fun uploadLogEvent(event: LogEvent): LogResponse {
        return withContext(Dispatchers.IO) {
            grpcClient.uploadLog(event)
        }
    }

    override suspend fun uploadLogsEvent(events: List<LogEvent>): LogResponse {
      return withContext(Dispatchers.IO) {
            grpcClient.uploadLogs(events)
        }
    }

    override suspend fun isNetworkAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            checkInternet(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncPendingLogs() {
        if (isSyncing) return
        isSyncing = true
        try {
            if (!isNetworkAvailable()) return // ✅ Prevent wasteful work

            val logs = logDao.getAllLogs()
            if (logs.isNotEmpty()) {
                val logEvents = logs.mapNotNull { logEntity ->
                    try {
                        LogEventBuilder.fromEntity(logEntity)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                if (logEvents.isNotEmpty()) {
                    val response = uploadLogsEvent(logEvents)
                    if (response.success) {
                        deleteLogsByIds(logs.map { it.logID })
                    }
                }
            }
        } finally {
            isSyncing = false
        }
    }

    private val prefs by lazy {
        context.getSharedPreferences("logs_plugin_prefs", Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    override fun saveConfig(config: Config) {
        val json = gson.toJson(config)
        prefs.edit().putString("config", json).apply()
    }

    override fun getConfig(): Config? {
        val json = prefs.getString("config", null) ?: return null
        return gson.fromJson(json, Config::class.java)
    }

    private fun checkInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}