package com.cometengine.tracktrace.network


import android.util.Log
import com.cometengine.tracktrace.misc.currentTimeStamp
import com.cometengine.tracktrace.misc.optString
import com.cometengine.tracktrace.misc.patternHP
import com.google.gson.JsonObject
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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

    fun getYanwenTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getYanwenTrackingData: $trackingId")

        val requestBody: RequestBody = FormBody.Builder()
            .add("InputTrackNumbers", trackingId)
            .build()

        val request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Origin", "https://track.yw56.com.cn")
            .addHeader("Referer", "https://track.yw56.com.cn/en-US")
            .addHeader("Sec-Fetch-Dest", "document")
            .url("https://track.yw56.com.cn/en-US")
            .post(requestBody)
            .build()

        val cb = object : NetworkCallback() {
            override fun onJsonResponse(response: JsonObject?) {

                val data: String = response?.optString("data", "") ?: ""

                if (data.isNotEmpty()) {

                    val matcher = patternHP.matcher(data)

                    if (matcher.find()) {

                        val id = matcher.group()

                        getHPTrackingData(id, callBack)

                    } else {
                        callBack.onJsonResponse(null)
                    }
                } else {
                    callBack.onJsonResponse(null)
                }
            }
        }

        httpClient.newCall(request).enqueue(cb)
    }

    fun getHPTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getHPTrackingData: $trackingId")

        val requestBody: RequestBody = FormBody.Builder()
            .add("query", trackingId)
            .add("vrsta", "1")
            //.add("tracklng", "en")
            .build()

        val request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .url("https://www.posta.hr/chrome.aspx")
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }

    fun getGLSTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getGLSTrackingData: $trackingId")

        val request = Request.Builder()
            .addHeader("Content-Type", "application/json;charset=UTF-8")
            .addHeader("Host", "gls-group.eu")
            .addHeader("Referer", "https://gls-group.eu/HR/hr/pracenje-posiljke")
            .url("https://gls-group.eu/app/service/open/rest/HR/hr/rstt001?match=$trackingId&caller=witt002&milis=$currentTimeStamp")
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }

    fun getOverseasTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getOverseasTrackingData: $trackingId")

        val request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .url("https://tracking.overseas.hr/api/track-and-trace/get-shipment-data//$trackingId")
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }

    fun getDHLTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getDHLTrackingData: $trackingId")

        val request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("Host", "www.dhl.hr")
            .addHeader("Referer", "https://www.dhl.hr/exp-hr/express/pracenje.html?AWB=$trackingId&brand=DHL")
            .url("https://www.dhl.hr/shipmentTracking?AWB=$trackingId&countryCode=hr&languageCode=hr&_=$currentTimeStamp")
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }

    fun getDPDTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getDPDTrackingData: $trackingId")

        val request = Request.Builder()
            .addHeader("Accept", "application/json, text/plain, */*")
            .addHeader("Host", "tracking.dpd.de")
            .url("https://tracking.dpd.de/rest/plc/hr_HR/$trackingId")
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }

    fun getInTimeTrackingData(trackingId: String, callBack: NetworkCallback) {

        Log.wtf(TAG, "getInTimeTrackingData: $trackingId")

        val request = Request.Builder()
            .addHeader("authorization", "rOu56B0ib9qwq8hugkzf08HYdIVktoUB9CoA2v2lMehz6K86d/BCwg==")
            .url("https://in-timetntapi.azurewebsites.net/api/Posiljke?broj=$trackingId")
            .build()

        httpClient.newCall(request).enqueue(callBack)
    }
}