package com.macroflow.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.macroflow.data.model.MacroRecording

@Database(entities = [MacroRecording::class], version = 1, exportSchema = false)
@TypeConverters(MacroRoomConverter::class)
abstract class MacroDatabase : RoomDatabase() {

    abstract fun macroDao(): MacroDao

    companion object {
        @Volatile
        private var INSTANCE: MacroDatabase? = null

        fun getInstance(context: Context): MacroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MacroDatabase::class.java,
                    "macro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
