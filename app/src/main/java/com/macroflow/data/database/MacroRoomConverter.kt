package com.macroflow.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.macroflow.data.model.MacroAction

class MacroRoomConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromActionList(actions: List<MacroAction>): String {
        return gson.toJson(actions)
    }

    @TypeConverter
    fun toActionList(json: String): List<MacroAction> {
        val type = object : TypeToken<List<MacroAction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
