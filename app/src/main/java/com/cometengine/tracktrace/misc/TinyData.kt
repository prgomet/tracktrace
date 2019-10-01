package com.cometengine.tracktrace.misc

import android.content.SharedPreferences
import java.util.*

class TinyData(private val sharedPref: SharedPreferences) {

    companion object {
        private val stringMap = HashMap<String, String?>()
        private val intMap = HashMap<String, Int>()
        private val floatMap = HashMap<String, Float>()
        private val longMap = HashMap<String, Long>()
        private val booleanMap = HashMap<String, Boolean>()
    }

    fun saveString(key: String, value: String?) {
        stringMap[key] = value
        sharedPref.edit().putString(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        intMap[key] = value
        sharedPref.edit().putInt(key, value).apply()
    }

    fun saveFloat(key: String, value: Float) {
        floatMap[key] = value
        sharedPref.edit().putFloat(key, value).apply()
    }

    fun saveLong(key: String, value: Long) {
        longMap[key] = value
        sharedPref.edit().putLong(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        booleanMap[key] = value
        sharedPref.edit().putBoolean(key, value).apply()
    }

    fun getString(key: String, default: String?): String? {
        if (stringMap[key] == null) stringMap[key] = sharedPref.getString(key, default)
        return stringMap[key]
    }

    fun getInt(key: String, default: Int): Int {
        if (intMap[key] == null) intMap[key] = sharedPref.getInt(key, default)
        return intMap[key]!!
    }

    fun getFloat(key: String, default: Float): Float {
        if (floatMap[key] == null) floatMap[key] = sharedPref.getFloat(key, default)
        return floatMap[key]!!
    }

    fun getLong(key: String, default: Long): Long {
        if (longMap[key] == null) longMap[key] = sharedPref.getLong(key, default)
        return longMap[key]!!
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        if (booleanMap[key] == null) booleanMap[key] = sharedPref.getBoolean(key, default)
        return booleanMap[key]!!
    }

    fun removeString(key: String) {
        stringMap.remove(key)
        sharedPref.edit().remove(key).apply()
    }

    fun removeInt(key: String) {
        intMap.remove(key)
        sharedPref.edit().remove(key).apply()
    }

    fun removeLong(key: String) {
        longMap.remove(key)
        sharedPref.edit().remove(key).apply()
    }

    fun removeBoolean(key: String) {
        booleanMap.remove(key)
        sharedPref.edit().remove(key).apply()
    }
}