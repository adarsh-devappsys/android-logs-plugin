package com.devappsys.logsplugin.data

data class LogEvent(
    val logID: String? = null,                    // Unique log ID, could be UUID
    val mnemonic: String? = null,                 // Short identifier (optional purpose marker)

    // Device Info
    val deviceID: String? = null,
    val deviceName: String? = null,
    val deviceModel: String? = null,
    val deviceOS: String? = null,
    val deviceOSVersion: String? = null,

    // App Info
    val appVersion: String? = null,
    val appName: String? = null,
    val appPackageName: String? = null,

    // Log Content
    val logMessage: String? = null,
    val logLevel: LogLevel? = null,
    val stackTrace: String? = null,

    // Metadata
    val timeStamp: Long? = System.currentTimeMillis(),
    val sessionID: String? = null,
    val userID: String? = null,
    val networkStatus: String? = null,
    val location: String? = null,

    // Custom fields for extensibility
    val customAttributes: Map<String, String> = emptyMap()
)


enum class LogLevel {
    VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL
}
