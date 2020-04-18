package com.cometengine.tracktrace

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.cometengine.tracktrace.misc.TRACKING_CHANNEL
import com.cometengine.tracktrace.misc.TRACKING_CHECKING
import com.cometengine.tracktrace.misc.TinyData
import com.cometengine.tracktrace.misc.enqueueWorker
import com.cometengine.tracktrace.workers.TrackingWorker
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.math.abs
import kotlin.math.ceil

class AppInit : Application(),
    Application.ActivityLifecycleCallbacks {

    companion object {

        private val TAG = AppInit::class.java.simpleName

        lateinit var instance: AppInit
        lateinit var tinyData: TinyData

        val displayMetrics = DisplayMetrics()
        var displaySize = Point()
        var density = 1f

        var inBackground = false

        var appTheme = MutableLiveData<@AppCompatDelegate.NightMode Int>()
    }

    override fun onCreate() {

        instance = this

        super.onCreate()

        registerActivityLifecycleCallbacks(this)

        tinyData = TinyData(defaultSharedPreferences)

        appTheme.observeForever {
            if (it != AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.setDefaultNightMode(it)
                tinyData.saveInt("APP_THEME", it)
            }
        }
        val savedTheme = tinyData.getInt("APP_THEME", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        appTheme.value = savedTheme

        checkDisplaySize()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificationChannels()
        }

        enqueueWorker<TrackingWorker>()
    }

    @SuppressLint("NewApi")
    private fun initNotificationChannels() {

        val notificationManager = instance
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            getNotificationChannel(
                TRACKING_CHANNEL,
                "Tracking Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        notificationManager.createNotificationChannel(
            getNotificationChannel(
                TRACKING_CHECKING,
                "Tracking Checking",
                NotificationManager.IMPORTANCE_MIN
            )
        )
    }

    @SuppressLint("NewApi")
    private fun getNotificationChannel(id: String, name: String, importance: Int): NotificationChannel {

        val channel = NotificationChannel(id, name, importance)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lightColor = ContextCompat.getColor(instance, R.color.dark_title)
        channel.description = name

        return channel
    }

    private fun checkDisplaySize() {
        try {
            density = applicationContext.resources.displayMetrics.density
            val configuration = applicationContext.resources.configuration

            val manager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val display = manager.defaultDisplay
            if (display != null) {
                display.getMetrics(displayMetrics)
                display.getSize(displaySize)
            }
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                val newSize = ceil((configuration.screenWidthDp * density).toDouble()).toInt()
                if (abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                val newSize = ceil((configuration.screenHeightDp * density).toDouble()).toInt()
                if (abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityPaused(p0: Activity) {
        inBackground = true
    }

    override fun onActivityResumed(p0: Activity) {
        inBackground = false
    }

    override fun onActivityStarted(p0: Activity) = Unit
    override fun onActivityDestroyed(p0: Activity) = Unit
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit
    override fun onActivityStopped(p0: Activity) = Unit
    override fun onActivityCreated(p0: Activity, p1: Bundle?) = Unit
}