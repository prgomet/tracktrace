package com.cometengine.tracktrace.misc

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.cometengine.tracktrace.AppInit.Companion.tinyData
import kotlin.reflect.KProperty

class TinyString(private val key: String, private val default: String?) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = tinyData.getString(key, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        tinyData.saveString(key, value)
    }
}

class TinyInt(private val key: String, private val default: Int) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = tinyData.getInt(key, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        tinyData.saveInt(key, value)
    }
}

class TinyFloat(private val key: String, private val default: Float) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = tinyData.getFloat(key, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        tinyData.saveFloat(key, value)
    }
}

class TinyLong(private val key: String, private val default: Long) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Long = tinyData.getLong(key, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        tinyData.saveLong(key, value)
    }
}

class TinyBoolean(private val key: String, private val default: Boolean) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = tinyData.getBoolean(key, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        tinyData.saveBoolean(key, value)
    }
}

class TinyList(private val key: String) : HashSet<String>() {

    @Volatile
    var list: HashSet<String> = TextUtils.split(tinyData.getString(key, ""), "‚‗‚").toHashSet()

    override fun add(element: String): Boolean {
        list.add(element)
        tinyData.saveString(key, TextUtils.join("‚‗‚", list))
        return true
    }

    override fun remove(element: String): Boolean {
        list.remove(element)
        tinyData.saveString(key, TextUtils.join("‚‗‚", list))
        return true
    }

    override fun contains(element: String): Boolean {
        val contains = list.contains(element)
        if (contains) remove(element)
        return contains
    }

    override fun clear() {
        list.clear()
        tinyData.removeString(key)
    }
}

class TinyFriendImageMap : HashMap<String, MutableLiveData<Int>>() {

    @Volatile
    var list: HashMap<String, MutableLiveData<Int>> = HashMap()

    override fun get(key: String): MutableLiveData<Int> {
        var data: MutableLiveData<Int>? = list[key]

        if (data == null) {
            data = MutableLiveData()
            list[key] = data

            val value = tinyData.getInt("$key-imgV", 0)
            data.postValue(value)
        }

        return data
    }

    fun add(key: String, imgV: Int) {
        val data: MutableLiveData<Int> = get(key)
        tinyData.saveInt("$key-imgV", imgV)
        data.postValue(imgV)
    }
}