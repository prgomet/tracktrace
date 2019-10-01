package com.cometengine.tracktrace.network

import android.os.Handler
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import org.jetbrains.anko.doAsync
import java.io.IOException


abstract class NetworkCallback(private val handler: Handler?) : Callback {

    constructor() : this(null)

    override fun onFailure(call: Call, e: IOException) {
        handleResponse(JsonObject().apply {
            addProperty("error", e.localizedMessage)
        })
    }

    override fun onResponse(call: Call, response: okhttp3.Response) {

        response.body?.charStream()?.use { reader ->

            try {

                val jsonElement = JsonParser().parse(reader)

                Log.wtf("jsonElement", jsonElement.toString())

                val jsonObject = jsonElement.asJsonObject

                if (jsonObject == null) {
                    handleResponse(null)
                } else {
                    if (jsonObject.has("error")) {
                        handleResponse(jsonObject)
                    } else {
                        handleResponse(jsonObject)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handleResponse(JsonObject().apply {
                    addProperty("error", response.message)
                })
            }

        } ?: handleResponse(JsonObject().apply {
            addProperty("error", "Response body is empty")
        })
    }

    private fun handleResponse(response: JsonObject?) {

        if (handler != null) {
            handler.post { onJsonResponse(response) }
        } else {
            doAsync { onJsonResponse(response) }
        }
    }

    abstract fun onJsonResponse(response: JsonObject?)
}