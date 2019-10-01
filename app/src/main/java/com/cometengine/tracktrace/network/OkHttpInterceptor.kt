package com.cometengine.tracktrace.network

import android.util.Log
import com.cometengine.tracktrace.BuildConfig
import com.cometengine.tracktrace.misc.convertToSec
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class OkHttpInterceptor : Interceptor {

    companion object {
        val TAG: String = OkHttpInterceptor::class.java.simpleName
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val request: Request = chain.request()

        val requestWithUserAgent: Request = request.newBuilder().apply {
            header("Accept", "*/*")
            header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            header("DNT", "1")
            //header("Origin", "chrome-extension://emljkbgckigdjdfijbjfobbdaedhogdb")
            header("Sec-Fetch-Mode", "cors")
        }.build()

        val response = chain.proceed(requestWithUserAgent)

        if (BuildConfig.DEBUG) {
            val sent = response.sentRequestAtMillis
            val received = response.receivedResponseAtMillis
            val reqUrl: String = java.lang.String.valueOf(request.url)
            Log.wtf(
                TAG, "reqUrl: " + reqUrl + ", sent: " + convertToSec(sent) +
                        ", received: " + convertToSec(received) +
                        ", dif: " + (received - sent).toString() +
                        ", response: " + response.message + " method: " + request.method
            )
        }

        return response
    }
}