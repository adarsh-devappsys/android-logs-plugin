package com.devappsys.logsplugin

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.devappsys.logsplugin.data.Config
import com.devappsys.logsplugin.data.DeviceInfo
import com.devappsys.logsplugin.data.LogLevel
import com.devappsys.logsplugin.database.LogDatabase
import com.devappsys.logsplugin.database.models.LogEntity
import com.devappsys.logsplugin.grpc.GrpcClient
import com.devappsys.logsplugin.grpc.LogEventBuilder
import com.devappsys.logsplugin.listeners.NetworkListener
import com.devappsys.logsplugin.repository.LogRepository
import com.devappsys.logsplugin.repository.LogRepositoryImpl
import com.devappsys.logsplugin.utils.ConfigPrefs
import com.devappsys.logsplugin.utils.WorkerPrefs
import com.devappsys.logsplugin.worker.LogUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

object LogsPlugin {

    private lateinit var repository: LogRepository
    private var networkListener: NetworkListener? = null
    private var isInitialized = false
    private lateinit var deviceInfo: DeviceInfo
    private var lastSyncTime = 0L


    fun init(context: Context, config: Config, deviceInfo: DeviceInfo) {
        if (isInitialized) return

        // Validate config
        require(config.grpcUri.isNotBlank()) { "grpcUri cannot be blank" }
        require(config.grpcPort > 0) { "grpcPort must be valid" }

        this.deviceInfo = deviceInfo
        val appContext = context.applicationContext

        ConfigPrefs.save(appContext, config)

        val database = LogDatabase.getDatabase(appContext)
        val grpcClient = GrpcClient(config.grpcUri, config.grpcPort)
        repository = LogRepositoryImpl(database.logDao(), grpcClient, appContext)

        // Register listener to auto sync logs
        networkListener = NetworkListener(appContext) { isAvailable ->
            if (isAvailable) {
                val now = System.currentTimeMillis()
                if (now - lastSyncTime > 10_000) { // 10 seconds debounce
                    lastSyncTime = now
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.syncPendingLogs()
                    }
                }
            }
        }
        networkListener?.register()

        // ✅ Schedule log worker only once
        if (!WorkerPrefs.isScheduled(appContext)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val logWorkRequest = PeriodicWorkRequestBuilder<LogUploadWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag("log_upload_worker")
                .build()

            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                "LogUploadWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                logWorkRequest
            )
            WorkerPrefs.markScheduled(appContext)
        }

        isInitialized = true
    }

    fun shutdown(context: Context) {
        if (!isInitialized) return
        networkListener?.unregister()
        WorkManager.getInstance(context).cancelAllWorkByTag("log_upload_worker")
        WorkerPrefs.clear(context)
        isInitialized = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun log(
        context: Context,
        message: String,
        level: LogLevel = LogLevel.INFO,
        tag: String = "LogsPlugin"
    ) {
        if (!isInitialized || !::repository.isInitialized) {
            throw IllegalStateException("LogsPlugin is not initialized.")
        }

        val now = System.currentTimeMillis()
        val packageManager = context.packageManager
        val packageName = context.packageName
        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) {
            "Unknown"
        }

        val entity = LogEntity(
            logID = UUID.randomUUID().toString(),
            mnemonic = tag,
            deviceID = Build.ID,
            deviceName = Build.DEVICE,
            deviceModel = deviceInfo?.model,
            deviceOS = "Android",
            deviceOSVersion = deviceInfo?.osVersion,
            appVersion = deviceInfo?.appVersion,
            appName = appName,
            appPackageName = packageName,
            logMessage = message,
            logLevel = level.name,
            stackTrace = "",
            timeStamp = now,
            sessionID = "",
            userID = "",
            networkStatus = if (repository.isNetworkAvailable()) "Online" else "Offline",
            location = null,
            customAttributesJson = null
        )

        if (repository.isNetworkAvailable()) {
            repository.uploadLogEvent(LogEventBuilder.fromEntity(entity))
        } else {
            repository.saveLogLocally(entity)
        }
    }
}

private fun WorkerPrefs.clear(context: Context) {
    val prefs = context.getSharedPreferences("worker_prefs", Context.MODE_PRIVATE)
    prefs.edit { clear() }
}
