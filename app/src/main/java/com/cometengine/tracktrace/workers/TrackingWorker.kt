package com.cometengine.tracktrace.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cometengine.tracktrace.*
import com.cometengine.tracktrace.database.AppDatabase
import com.cometengine.tracktrace.database.ItemDescription
import com.cometengine.tracktrace.database.TrackingItem
import com.cometengine.tracktrace.misc.*
import com.cometengine.tracktrace.network.Network
import com.cometengine.tracktrace.network.NetworkCallback
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.*

import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList


class TrackingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        private val TAG: String = TrackingWorker::class.java.simpleName
    }

    override fun doWork(): Result {

        val showNotification = inputData.getBoolean("notifications", true)

        val forTracking = if (inputData.getString("trackingId") != null) {
            arrayListOf(inputData.getString("trackingId")!!)
        } else {
            AppDatabase.getInstance().getTrackingDao().getTrackingItemsForSync()
        }

        var workerResult = Result.retry()

        if (forTracking.isNotEmpty()) {
            val countDownLatch = CountDownLatch(forTracking.size)

            forTracking.forEach { trackingId ->
                Network.getTrackingData(trackingId,
                    object : NetworkCallback() {

                        override fun onJsonResponse(response: JsonObject?) {
                            Log.wtf(TAG, response.toString())

                            val results = response?.optJsonArray("results")
                            val list = ArrayList<ItemDescription>()

                            var markAsDelivered = false

                            val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!
                            val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                            results?.apply {

                                var index = size()

                                while (index-- > 0) {

                                    val itemDescription = ItemDescription()
                                    val cur = results.get(index).asJsonObject

                                    val calendar = Calendar.getInstance(Locale.getDefault())

                                    try {
                                        //2.9.2019,08:23:00

                                        val df = SimpleDateFormat("d.M.yyyy,HH:mm:ss", Locale.getDefault())
                                        val date = cur.optString("datum", "").trim()
                                        val time = cur.optString("vrijeme", "").trim()

                                        calendar.time = df.parse("$date,$time") ?: Date()

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        calendar.timeInMillis = currentTimeStamp
                                    }

                                    itemDescription.id = md5(cur.toString() + trackingId)
                                    itemDescription.trackingId = trackingId
                                    itemDescription.location = cur.optString("lokacija", "")
                                    itemDescription.description = cur.optString("status", "")
                                    itemDescription.created = calendar.timeInMillis
                                    itemDescription.seen = 0

                                    list.add(itemDescription)

                                    if (itemDescription.description.contains("uručena", true)) {
                                        markAsDelivered = true
                                    }
                                }
                            }

                            if ((list.isEmpty() || results == null) && descriptionItemsCount == 0) {
                                val itemDescription = ItemDescription()
                                itemDescription.id = md5(trackingId + trackingId)
                                itemDescription.trackingId = trackingId
                                itemDescription.location = "UNKNOWN"
                                itemDescription.description = "Nema podataka za uneseni broj pošiljke"
                                itemDescription.created = currentTimeStamp
                                itemDescription.seen = 0

                                list.add(itemDescription)
                            }

                            if (markAsDelivered) {
                                trackingItem.status = TrackingItem.DELIVERED
                                trackingItem.upd = currentTimeStamp
                                AppDatabase.getInstance().getTrackingDao().update(trackingItem)
                            }

                            AppDatabase.getInstance().getTrackingDao().setUpd(trackingId, currentTimeStamp)

                            val items = AppDatabase.getInstance().getTrackingInfoDao().insert(list)

                            if (!AppInit.tinyData.getBoolean("NOTIFICATIONS_MUTED", false)) {

                                var hasNew = false
                                var limit = 0

                                items.forEach { r ->
                                    if (r > -1) {
                                        limit++
                                        hasNew = true
                                    }
                                }

                                if (hasNew && showNotification && !AppInit.inBackground) {
                                    if (limit > 4) {
                                        limit = 4
                                    }
                                    TrackingNotification(limit).execute(trackingId)
                                }
                            }

                            workerResult = Result.success()
                            countDownLatch.countDown()
                        }
                    })
            }

            try {
                countDownLatch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        return workerResult
    }
}