package com.cometengine.tracktrace.misc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Notification
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import androidx.work.*
import com.cometengine.tracktrace.AppInit
import com.cometengine.tracktrace.BuildConfig
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

const val ACCOUNT_TYPE = "${BuildConfig.APPLICATION_ID}.account"
const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

const val TRACKING_CHANNEL = "${BuildConfig.APPLICATION_ID}.tracking"
const val TRACKING_CHECKING = "${BuildConfig.APPLICATION_ID}.checking"

val WORK_MANAGER = WorkManager.getInstance(AppInit.instance)

fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    val distinctLiveData = MediatorLiveData<T>()

    distinctLiveData.addSource(this, object : Observer<T> {

        private var initialized = false
        private var last: T? = null

        override fun onChanged(t: T) {
            if (!initialized) {
                initialized = true
                last = t
                distinctLiveData.postValue(last)
            } else if ((t == null && last != null) || t != last) {
                last = t
                distinctLiveData.postValue(last)
            }
        }
    })
    return distinctLiveData
}

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

fun getDrawableWithColor(drawableId: Int, colorId: Int): Drawable? {
    val drawable = AppCompatResources.getDrawable(AppInit.instance, drawableId)
    return if (drawable != null) {
        tintDrawable(drawable, getColorFromRes(colorId))
    } else {
        null
    }
}

private fun getColorFromRes(resId: Int): Int = ContextCompat.getColor(AppInit.instance, resId)

private fun tintDrawable(d: Drawable, @ColorInt color: Int): Drawable {
    val draw: Drawable = DrawableCompat.wrap(d.mutate())
    DrawableCompat.setTint(draw, color)
    return draw
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

inline fun <reified W : Worker> enqueueWorker() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    WORK_MANAGER.enqueue(
        OneTimeWorkRequest
            .Builder(W::class.java)
            .setConstraints(constraints)
            .build()
    )
}

inline fun <reified W : Worker> enqueueWorker(data: Data) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    WORK_MANAGER.enqueue(
        OneTimeWorkRequest
            .Builder(W::class.java)
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