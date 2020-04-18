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
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36")
            header("DNT", "1")
        }.build()

        val response = chain.proceed(requestWithUserAgent)

        if (BuildConfig.DEBUG) {
            val sent = response.sentRequestAtMillis
            val received = response.receivedResponseAtMillis
            val reqUrl = request.url.toUrl()
            Log.wtf(
                TAG, "reqUrl: " + reqUrl +
                        ", dif: " + (received - sent).toString() +
                        ", response: " + response.message +
                        ", method: " + request.method
            )
        }

        return response
    }
}