package com.devappsys.logsplugin.utils

import android.content.Context
import androidx.core.content.edit


object WorkerPrefs {
    private const val PREFS_NAME = "worker_prefs"
    private const val KEY_SCHEDULED = "is_scheduled"

    private var inMemoryFlag: Boolean? = null

    fun isScheduled(context: Context): Boolean {
        if (inMemoryFlag != null) return inMemoryFlag!!
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getBoolean(KEY_SCHEDULED, false)
        inMemoryFlag = value
        return value
    }

    fun markScheduled(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_SCHEDULED, true)
        }
        inMemoryFlag = true
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            clear()
        }
        inMemoryFlag = false
    }
}