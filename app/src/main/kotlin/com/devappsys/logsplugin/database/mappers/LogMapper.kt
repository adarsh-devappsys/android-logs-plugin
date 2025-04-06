package com.devappsys.logsplugin.database.mappers
import com.devappsys.logsplugin.data.LogEvent
import com.devappsys.logsplugin.data.LogLevel
import com.devappsys.logsplugin.database.models.LogEntity
import com.google.gson.Gson


object LogMapper {
    private val gson = Gson()

    fun fromEvent(event: LogEvent): LogEntity {
        return LogEntity(
            logID = event.logID ?: "",
            mnemonic = event.mnemonic,
            deviceID = event.deviceID,
            deviceName = event.deviceName,
            deviceModel = event.deviceModel,
            deviceOS = event.deviceOS,
            deviceOSVersion = event.deviceOSVersion,
            appVersion = event.appVersion,
            appName = event.appName,
            appPackageName = event.appPackageName,
            logMessage = event.logMessage,
            logLevel = event.logLevel?.name,
            stackTrace = event.stackTrace,
            timeStamp = event.timeStamp,
            sessionID = event.sessionID,
            userID = event.userID,
            networkStatus = event.networkStatus,
            location = event.location,
            customAttributesJson = gson.toJson(event.customAttributes)
        )
    }

    fun toEvent(entity: LogEntity): LogEvent {
        return LogEvent(
            logID = entity.logID,
            mnemonic = entity.mnemonic,
            deviceID = entity.deviceID,
            deviceName = entity.deviceName,
            deviceModel = entity.deviceModel,
            deviceOS = entity.deviceOS,
            deviceOSVersion = entity.deviceOSVersion,
            appVersion = entity.appVersion,
            appName = entity.appName,
            appPackageName = entity.appPackageName,
            logMessage = entity.logMessage,
            logLevel = entity.logLevel?.let { LogLevel.valueOf(it) },
            stackTrace = entity.stackTrace,
            timeStamp = entity.timeStamp,
            sessionID = entity.sessionID,
            userID = entity.userID,
            networkStatus = entity.networkStatus,
            location = entity.location,
            customAttributes = entity.customAttributesJson?.let {
                gson.fromJson(it, Map::class.java) as Map<String, String>
            } ?: emptyMap()
        )
    }
}