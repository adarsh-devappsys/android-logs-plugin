package com.devappsys.logsplugin.grpc

import android.os.Build
import androidx.annotation.RequiresApi
import com.devappsys.logsplugin.database.models.LogEntity
import com.google.protobuf.Timestamp
import log.Log.*
import java.time.Instant
import java.util.UUID

object LogEventBuilder {
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(
        message: String,
        level: LogLevel = LogLevel.INFO,
        stackTrace: String = "",
        userID: String = "",
        customAttributes: Map<String, String> = emptyMap()
    ):LogEvent {
        val now = Instant.now()

        return LogEvent.newBuilder()
            .setLogID(java.util.UUID.randomUUID().toString())
            .setMnemonic("sample")
            .setDeviceID("DEVICE_ID")
            .setDeviceName("Pixel")
            .setDeviceModel("Pixel 7")
            .setDeviceOS("Android")
            .setDeviceOSVersion("14")
            .setAppVersion("1.0.0")
            .setAppName("SampleApp")
            .setAppPackageName("com.devappsys.samplekotline")
            .setLogMessage(message)
            .setLogLevel(level)
            .setStackTrace(stackTrace)
            .setUserID(userID)
            .setSessionID("session-123")
            .setNetworkStatus("Online")
            .setLocation("Unknown")
            .setTimeStamp(
                Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build()
            )
            .apply {
                customAttributes.forEach { (k, v) ->
                    putCustomAttributes(k, v)
                }
            }
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromEntity(entity: LogEntity): LogEvent {
        val timestamp = entity.timeStamp?.let {
            val instant = Instant.ofEpochMilli(it)
            Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
                .build()
        } ?: Timestamp.getDefaultInstance()

        val builder = LogEvent.newBuilder()
            .setLogID(entity.logID.ifEmpty { UUID.randomUUID().toString() })
            .setMnemonic(entity.mnemonic ?: "")
            .setDeviceID(entity.deviceID ?: "")
            .setDeviceName(entity.deviceName ?: "")
            .setDeviceModel(entity.deviceModel ?: "")
            .setDeviceOS(entity.deviceOS ?: "")
            .setDeviceOSVersion(entity.deviceOSVersion ?: "")
            .setAppVersion(entity.appVersion ?: "")
            .setAppName(entity.appName ?: "")
            .setAppPackageName(entity.appPackageName ?: "")
            .setLogMessage(entity.logMessage ?: "")
            .setLogLevel(LogLevel.valueOf(entity.logLevel ?: LogLevel.INFO.name))
            .setStackTrace(entity.stackTrace ?: "")
            .setUserID(entity.userID ?: "")
            .setSessionID(entity.sessionID ?: "")
            .setNetworkStatus(entity.networkStatus ?: "")
            .setLocation(entity.location ?: "")
            .setTimeStamp(timestamp)

        // Deserialize customAttributesJson and put into map
        val attributesMap = parseJsonToMap(entity.customAttributesJson ?: "{}")
        attributesMap.forEach { (k, v) ->
            builder.putCustomAttributes(k, v)
        }

        return builder.build()
    }

    private fun parseJsonToMap(json: String): Map<String, String> {
        return try {
            val gson = com.google.gson.Gson()
            gson.fromJson(json, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
