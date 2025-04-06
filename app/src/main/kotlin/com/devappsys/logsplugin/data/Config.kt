package com.devappsys.logsplugin.data

data class Config (
    val grpcUri: String,
    val grpcPort: Int,
    val serviceEnabled: Boolean

)