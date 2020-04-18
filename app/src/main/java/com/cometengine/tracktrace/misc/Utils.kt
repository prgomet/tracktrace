package com.cometengine.tracktrace.misc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Notification
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.BindingAdapter
import androidx.work.*
import com.cometengine.tracktrace.AppInit
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil

const val ACCOUNT_TYPE = "com.cometengine.tracktrace.account"
const val AUTHORITY = "com.cometengine.tracktrace.provider"

const val TRACKING_CHANNEL = "com.cometengine.tracktrace.tracking"
const val TRACKING_CHECKING = "com.cometengine.tracktrace.checking"

val patternHP: Pattern = Pattern.compile("([a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2})", Pattern.CASE_INSENSITIVE)
val patternYanwen: Pattern = Pattern.compile("([a-zA-Z][0-9]{14})", Pattern.CASE_INSENSITIVE)
val patternInTime: Pattern = Pattern.compile("([a-zA-Z]{2}[0-9]{12})", Pattern.CASE_INSENSITIVE)
val patternGLS: Pattern = Pattern.compile("((000|919)[0-9]{8})", Pattern.CASE_INSENSITIVE)
val patternOverseas: Pattern = Pattern.compile("(191001[0-9]{8})", Pattern.CASE_INSENSITIVE)
val patternDPD: Pattern = Pattern.compile("([0-9]{14})", Pattern.CASE_INSENSITIVE)
val patternDHL: Pattern = Pattern.compile("([0-9]{10})", Pattern.CASE_INSENSITIVE)

val WORK_MANAGER = WorkManager.getInstance(AppInit.instance)

val currentTimeStamp: Long
    get() {
        val date = Date()
        return date.time
    }

fun md5(toEncrypt: String): String {
    return try {
        val digest = MessageDigest.getInstance("md5")
        digest.update(toEncrypt.toByteArray())
        val bytes = digest.digest()
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append(String.format("%02X", bytes[i]))
        }
        sb.toString().toLowerCase(Locale.getDefault())
    } catch (exc: Exception) {
        ""
    }
}

fun convertToSec(date: Long?): String = try {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    dateFormat.format(Date(date!!))
} catch (ignore: Exception) {
    ""
}

fun JsonObject.optString(key: String, fallback: String): String =
    if (has(key)) get(key).asString else fallback

fun JsonObject.optJsonArray(key: String): JsonArray? =
    if (has(key)) get(key).asJsonArray else null

fun JsonObject.optJsonObject(key: String): JsonObject? =
    if (has(key)) get(key).asJsonObject else null

inline fun <reified W : Worker> enqueueWorker() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workPolicy = ExistingWorkPolicy.REPLACE

    WORK_MANAGER.enqueueUniqueWork(
        W::class.java.simpleName, workPolicy,
        OneTimeWorkRequestBuilder<W>()
            .setConstraints(constraints)
            .build()
    )
}

inline fun <reified W : Worker> enqueueWorker(data: Data) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workPolicy = ExistingWorkPolicy.REPLACE

    WORK_MANAGER.enqueueUniqueWork(
        W::class.java.simpleName, workPolicy,
        OneTimeWorkRequestBuilder<W>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()
    )
}

fun convertToTimeStamp(date: Long?): String = try {
    val df = SimpleDateFormat("dd.MMM.yyyy, HH:mm", Locale.getDefault())
    df.format(date)
} catch (ignore: Exception) {
    ""
}

@BindingAdapter("isVisible")
fun bindIsVisible(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("imgDrawable")
fun bindImageFromDrawable(view: AppCompatImageButton, imgSource: Int) {
    view.setImageDrawable(AppCompatResources.getDrawable(view.context, imgSource))
}

@BindingAdapter("imgDrawable")
fun bindImageFromDrawable(view: AppCompatImageView, imgSource: Int) {
    view.setImageDrawable(AppCompatResources.getDrawable(view.context, imgSource))
}

fun dp(value: Float): Int = if (value == 0f) {
    0
} else ceil((AppInit.density * value).toDouble()).toInt()

fun dpf2(value: Float): Float = if (value == 0f) {
    0f
} else AppInit.density * value

fun View.fadeOutAndMoveY() {

    val va = ValueAnimator.ofInt(0, dp(56f))

    va.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        override fun onAnimationEnd(animation: Animator) {
            setLayerType(View.LAYER_TYPE_NONE, null)
        }
    })

    va.addUpdateListener {
        run {
            val params = this.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = it.animatedValue as Int * -1
            this.layoutParams = params
        }
    }

    va.interpolator = AccelerateDecelerateInterpolator()
    va.duration = 300
    va.start()
}

fun View.fadeInAndMoveY() {

    val va = ValueAnimator.ofInt(dp(56f), 0)

    va.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        override fun onAnimationEnd(animation: Animator) {
            setLayerType(View.LAYER_TYPE_NONE, null)
        }
    })

    va.addUpdateListener {
        run {
            val params = this.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = it.animatedValue as Int * -1
            this.layoutParams = params
        }
    }

    va.interpolator = AccelerateDecelerateInterpolator()
    va.duration = 300
    va.start()
}

fun postNotification(id: String, notification: Notification) {
    NotificationManagerCompat.from(AppInit.instance).notify(getNotificationId(id), notification)
}

fun cancelNotification(id: String) {
    NotificationManagerCompat.from(AppInit.instance).cancel(getNotificationId(id))
}

fun getNotificationId(id: String): Int = id.hashCode()