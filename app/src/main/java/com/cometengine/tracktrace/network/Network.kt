package com.cometengine.tracktrace.network

import okhttp3.*
import java.util.concurrent.TimeUnit

object Network {

    val TAG: String = Network::class.java.simpleName

    private val httpClient: OkHttpClient = OkHttpClient()
        .newBuilder()
        .addNetworkInterceptor(OkHttpInterceptor())
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .pingInterval(2, TimeUnit.MINUTES)
        .build()

    fun getTrackingData(trackingId: String, callBack: NetworkCallback) {

        val requestBody: RequestBody = FormBody.Builder()
            .add("query", trackingId)
            .add("vrsta", "1")
            //.add("tracklng", "en")
            .build()

        val request = Request.Builder()
            .url("https://www.posta.hr/chrome.aspx")
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }
}