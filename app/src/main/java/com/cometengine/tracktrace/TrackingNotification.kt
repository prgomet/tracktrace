package com.cometengine.tracktrace

import android.app.PendingIntent
import android.content.Intent
import android.os.AsyncTask
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.cometengine.tracktrace.database.AppDatabase
import com.cometengine.tracktrace.database.TrackingItem.Companion.DELIVERED
import com.cometengine.tracktrace.misc.TRACKING_CHANNEL
import com.cometengine.tracktrace.misc.currentTimeStamp
import com.cometengine.tracktrace.misc.postNotification
import org.jetbrains.anko.collections.forEachReversedByIndex

class TrackingNotification(private val limit: Int) : AsyncTask<String, Int, Int>() {

    override fun doInBackground(vararg params: String): Int {
        val db = AppDatabase.getInstance()
        val trackingId = params[0]
        val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!

        val items = db.getTrackingInfoDao().getTracking(trackingItem.id, limit)

        var markAsDelivered = false

        if (items.isNotEmpty()) {

            val intent = Intent(AppInit.instance, MainActivity::class.java)

            val pIntent = PendingIntent.getActivity(
                AppInit.instance,
                System.currentTimeMillis().toInt(), intent, 0
            )

            val title = if (trackingItem.title.isEmpty()) {
                trackingItem.id
            } else {
                trackingItem.title
            }

            val person = Person.Builder().setName(title).build()

            val messageStyle = NotificationCompat.MessagingStyle(person)

            messageStyle.conversationTitle = title

            items.forEachReversedByIndex { itemDescription ->

                val per = Person.Builder()
                    .setName(itemDescription.location)
                    .build()

                val msg = NotificationCompat.MessagingStyle.Message(
                    itemDescription.description,
                    itemDescription.created, per
                )

                if (msg.text.contains("uručena", true)) {
                    markAsDelivered = true
                }

                messageStyle.addMessage(msg)
            }

            val mBuilder = NotificationCompat.Builder(AppInit.instance, TRACKING_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setStyle(messageStyle)
                .setContentIntent(pIntent)
                .setShowWhen(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            postNotification(trackingId, mBuilder.build())
        }

        if (markAsDelivered) {
            trackingItem.status = DELIVERED
            trackingItem.upd = currentTimeStamp
            db.getTrackingDao().update(trackingItem)
        }
        return 1
    }
}