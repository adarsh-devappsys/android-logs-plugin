package com.devappsys.logsplugin.data

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val appVersion: String,
    val sdkVersion: Int,
    val isEmulator: Boolean
)