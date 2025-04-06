package com.devappsys.logsplugin.worker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devappsys.logsplugin.database.LogDatabase
import com.devappsys.logsplugin.grpc.GrpcClient
import com.devappsys.logsplugin.grpc.LogEventBuilder
import com.devappsys.logsplugin.repository.LogRepositoryImpl
import com.devappsys.logsplugin.utils.ConfigPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext

            // Get config from SharedPreferences
            val config = ConfigPrefs.get(context)
            if (config == null || !config.serviceEnabled) {
                return@withContext Result.success()
            }

            val db = LogDatabase.getDatabase(context)
            val grpcClient = GrpcClient(config.grpcUri, config.grpcPort)
            val repository = LogRepositoryImpl(db.logDao(), grpcClient, context)

            val allLogs = repository.getAllLogs()
            if (allLogs.isEmpty()) return@withContext Result.success()

            // Upload logs in chunks
            val chunkSize = 50
            allLogs.chunked(chunkSize).forEach { chunk ->
                val logEvents = chunk.map { LogEventBuilder.fromEntity(it) }
                val response = repository.uploadLogsEvent(logEvents)
                if (response.success) {
                    val ids = chunk.map { it.logID }
                    repository.deleteLogsByIds(ids)
                } else {
                    return@withContext Result.retry()
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}