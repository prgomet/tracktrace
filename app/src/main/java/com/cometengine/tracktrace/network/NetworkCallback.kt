package com.cometengine.tracktrace.network

import android.os.Handler
import com.google.gson.JsonObject
import com.google.gson.JsonParser.parseString
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

            val text = reader.readText()

            val jsonElement = try {
                parseString(text)
            } catch (e: Exception) {
                JsonObject().apply { addProperty("data", text) }
            }

            try {

                val jsonObject = if (jsonElement.isJsonObject) {
                    jsonElement.asJsonObject
                } else {
                    JsonObject().apply { add("data", jsonElement.asJsonArray) }
                }

                handleResponse(jsonObject)

            } catch (e: Exception) {
                handleResponse(JsonObject().apply {
                    addProperty("error", e.localizedMessage)
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