package com.devappsys.logsplugin.utils

import android.content.Context
import android.content.SharedPreferences
import com.devappsys.logsplugin.data.Config

object ConfigPrefs {
    private const val PREF_NAME = "logs_config"
    private const val KEY_GRPC_URI = "grpc_uri"
    private const val KEY_GRPC_PORT = "grpc_port"
    private const val KEY_ENABLED = "service_enabled"

    fun save(context: Context, config: Config) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_GRPC_URI, config.grpcUri)
            putInt(KEY_GRPC_PORT, config.grpcPort)
            putBoolean(KEY_ENABLED, config.serviceEnabled)
            apply()
        }
    }

    fun get(context: Context): Config? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val uri = prefs.getString(KEY_GRPC_URI, null) ?: return null
        val port = prefs.getInt(KEY_GRPC_PORT, -1)
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        if (port == -1) return null
        return Config(uri, port, enabled)
    }
}