package com.devappsys.logsplugin.database.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey val logID: String,
    val mnemonic: String?,
    val deviceID: String?,
    val deviceName: String?,
    val deviceModel: String?,
    val deviceOS: String?,
    val deviceOSVersion: String?,
    val appVersion: String?,
    val appName: String?,
    val appPackageName: String?,
    val logMessage: String?,
    val logLevel: String?, // Store enum as String
    val stackTrace: String?,
    val timeStamp: Long?,
    val sessionID: String?,
    val userID: String?,
    val networkStatus: String?,
    val location: String?,
    val customAttributesJson: String? // Map serialized as JSON
)