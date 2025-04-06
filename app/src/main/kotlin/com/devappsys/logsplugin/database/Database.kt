package com.devappsys.logsplugin.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devappsys.logsplugin.database.dao.LogDao
import com.devappsys.logsplugin.database.models.LogEntity


@Database(
    entities = [LogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: LogDatabase? = null

        fun getDatabase(context: Context): LogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LogDatabase::class.java,
                    "log_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}