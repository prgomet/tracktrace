package com.cometengine.tracktrace.workers

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cometengine.tracktrace.AppInit
import com.cometengine.tracktrace.TrackingNotification
import com.cometengine.tracktrace.database.AppDatabase
import com.cometengine.tracktrace.database.ItemDescription
import com.cometengine.tracktrace.database.TrackingItem
import com.cometengine.tracktrace.misc.*
import com.cometengine.tracktrace.network.Network
import com.cometengine.tracktrace.network.NetworkCallback
import com.google.gson.JsonArray
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

        if (forTracking.isNotEmpty()) {
            val countDownLatch = CountDownLatch(forTracking.size)

            forTracking.forEach { trackingId ->

                val hpMatcher = patternHP.matcher(trackingId)
                val yanwenMatcher = patternYanwen.matcher(trackingId)
                val inTimeMatcher = patternInTime.matcher(trackingId)
                val glsMatcher = patternGLS.matcher(trackingId)
                val overseasMatcher = patternOverseas.matcher(trackingId)
                val dpdMatcher = patternDPD.matcher(trackingId)
                val dhlMatcher = patternDHL.matcher(trackingId)

                when {
                    hpMatcher.find() -> {

                        val id = hpMatcher.group()

                        Network.getHPTrackingData(id, object : NetworkCallback() {

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

                                        try {

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())

                                            //2.9.2019,08:23:00

                                            val df = SimpleDateFormat("d.M.yyyy,HH:mm:ss", Locale.getDefault())
                                            val date = cur.get("datum").asString.trim()
                                            val time = cur.get("vrijeme").asString.trim()

                                            calendar.time = df.parse("$date,$time") ?: Date()

                                            val itemDescription = ItemDescription()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId
                                            itemDescription.location = cur.get("lokacija").asString.trim()
                                            itemDescription.description = cur.get("status").asString
                                                .trim().replace("  ", " ")
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            list.add(itemDescription)

                                            if (itemDescription.description.contains("uručena", true)) {
                                                markAsDelivered = true
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })
                    }
                    yanwenMatcher.find() -> {

                        val id = yanwenMatcher.group()

                        Network.getYanwenTrackingData(id, object : NetworkCallback() {

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

                                        try {

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())

                                            //2.9.2019,08:23:00

                                            val df = SimpleDateFormat("d.M.yyyy,HH:mm:ss", Locale.getDefault())
                                            val date = cur.get("datum").asString.trim()
                                            val time = cur.get("vrijeme").asString.trim()

                                            calendar.time = df.parse("$date,$time") ?: Date()

                                            val itemDescription = ItemDescription()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId
                                            itemDescription.location = cur.get("lokacija").asString.trim()
                                            itemDescription.description = cur.get("status").asString
                                                .trim().replace("  ", " ")
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            list.add(itemDescription)

                                            if (itemDescription.description.contains("uručena", true)) {
                                                markAsDelivered = true
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })
                    }
                    inTimeMatcher.find() -> {

                        val id = inTimeMatcher.group()

                        Network.getInTimeTrackingData(id, object : NetworkCallback() {

                            override fun onJsonResponse(response: JsonObject?) {
                                Log.wtf(TAG, response.toString())

                                val list = ArrayList<ItemDescription>()

                                var markAsDelivered = false

                                val results: JsonArray? = response?.optJsonArray("data")

                                val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!
                                val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                                results?.apply {

                                    var index = size()

                                    while (index-- > 0) {

                                        try {

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())
                                            //2020-02-10T16:10:26.077

                                            val df = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS", Locale.getDefault())
                                            val date = cur.get("date").asString.trim().replace("T", "-")

                                            calendar.time = df.parse(date) ?: Date()

                                            val itemDescription = ItemDescription()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId
                                            itemDescription.location = cur.get("distributivniCentarNaziv").asString
                                            itemDescription.description = cur.get("nazivStatus").asString
                                                .trim().replace("  ", " ")
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            if (itemDescription.description.contains("Isporučeno", true)) {
                                                markAsDelivered = true
                                            }

                                            list.add(itemDescription)

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })
                    }
                    glsMatcher.find() -> {

                        var id = glsMatcher.group()

                        if (id.startsWith("000")) {
                            id = id.replaceFirst("000", "919")
                        }

                        Network.getGLSTrackingData(id, object : NetworkCallback() {

                            override fun onJsonResponse(response: JsonObject?) {
                                Log.wtf(TAG, response.toString())

                                var results: JsonArray? = null
                                val list = ArrayList<ItemDescription>()

                                var markAsDelivered = false

                                val tuStatus = response?.optJsonArray("tuStatus")

                                tuStatus?.forEach { element ->
                                    if (element.isJsonObject && element.asJsonObject.optJsonArray("history") != null) {
                                        results = element.asJsonObject.optJsonArray("history")
                                    }
                                }


                                val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!
                                val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                                results?.apply {

                                    var index = size()

                                    while (index-- > 0) {

                                        try {

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())

                                            //2019-10-15,08:23:00

                                            val df = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
                                            val date = cur.get("date").asString.trim()
                                            val time = cur.get("time").asString.trim()

                                            calendar.time = df.parse("$date,$time") ?: Date()

                                            val itemDescription = ItemDescription()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId

                                            val address: JsonObject? = cur.getAsJsonObject("address")

                                            val location = address?.get("city")?.asString?.trim() ?: "UNKNOWN"

                                            itemDescription.location = location
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                itemDescription.description = Html.fromHtml(
                                                    cur.get("evtDscr").asString.trim()
                                                        .replace("  ", " "), Html.FROM_HTML_MODE_COMPACT
                                                ).toString()
                                            } else {
                                                @Suppress("DEPRECATION")
                                                itemDescription.description = Html.fromHtml(
                                                    cur.get("evtDscr").asString
                                                        .trim().replace("  ", " ")
                                                ).toString()
                                            }
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            list.add(itemDescription)

                                            if (itemDescription.description.contains("Paket je isporučen", true)) {
                                                markAsDelivered = true
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })

                    }
                    overseasMatcher.find() -> {

                        val id = overseasMatcher.group().drop(6)

                        Network.getOverseasTrackingData(id, object : NetworkCallback() {

                            override fun onJsonResponse(response: JsonObject?) {
                                Log.wtf(TAG, response.toString())

                                var results: JsonArray? = null
                                val list = ArrayList<ItemDescription>()

                                var markAsDelivered = false

                                val collies = response?.optJsonArray("Collies")

                                collies?.forEach { element ->
                                    if (element.isJsonObject && element.asJsonObject.optJsonArray("Traces") != null) {
                                        results = element.asJsonObject.optJsonArray("Traces")
                                    }
                                }

                                val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!
                                val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                                results?.apply {

                                    var index = size()

                                    while (index-- > 0) {

                                        try {

                                            val itemDescription = ItemDescription()

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())

                                            //04.12.2019.,08:23:00

                                            val df = SimpleDateFormat("dd.MM.yyyy.,HH:mm:ss", Locale.getDefault())
                                            val date = cur.optString("ScanDateString", "").trim()
                                            val time = cur.optString("ScanTimeString", "").trim()

                                            calendar.time = df.parse("$date,$time") ?: Date()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId
                                            itemDescription.location = cur.optString("CenterName", "UNKNOWN")
                                            itemDescription.description = cur.optString("StatusDescription", "Nema podataka za uneseni broj pošiljke")
                                                .trim().replace("  ", " ")
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            list.add(itemDescription)

                                            if (itemDescription.description.contains("Pošiljka je isporučena.", true)) {
                                                markAsDelivered = true
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })
                    }
                    dpdMatcher.find() -> {

                        val id = dpdMatcher.group()

                        Network.getDPDTrackingData(id, object : NetworkCallback() {

                            override fun onJsonResponse(response: JsonObject?) {
                                Log.wtf(TAG, response.toString())

                                val list = ArrayList<ItemDescription>()

                                var markAsDelivered = false

                                val results: JsonArray? = response?.optJsonObject("parcellifecycleResponse")
                                    ?.optJsonObject("parcelLifeCycleData")
                                    ?.optJsonArray("statusInfo")

                                val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!
                                val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                                results?.apply {

                                    var index = size()

                                    while (index-- > 0) {

                                        try {

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())

                                            //2019-12-10, 08:27

                                            val df = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault())
                                            val date = cur.get("date").asString.trim()

                                            calendar.time = df.parse(date) ?: Date()

                                            val itemDescription = ItemDescription()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId
                                            itemDescription.location = cur.get("location").asString
                                            itemDescription.description = cur.get("label").asString
                                                .trim().replace("  ", " ")
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            if (itemDescription.description.contains("Dostavljeno", true)) {
                                                markAsDelivered = true
                                            }

                                            list.add(itemDescription)

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })
                    }
                    dhlMatcher.find() -> {

                        val id = dhlMatcher.group()

                        Network.getDHLTrackingData(id, object : NetworkCallback() {

                            override fun onJsonResponse(response: JsonObject?) {
                                Log.wtf(TAG, response.toString())

                                var results: JsonArray? = null
                                val list = ArrayList<ItemDescription>()

                                var markAsDelivered = false

                                response?.optJsonArray("results")?.forEach { element ->
                                    if (element.isJsonObject && element.asJsonObject.optJsonArray("checkpoints") != null) {
                                        results = element.asJsonObject.optJsonArray("checkpoints")
                                    }
                                }

                                val trackingItem = AppDatabase.getInstance().getTrackingDao().getItem(trackingId)!!
                                val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                                results?.apply {

                                    var index = size()

                                    while (index-- > 0) {

                                        try {

                                            val cur = get(index).asJsonObject

                                            val calendar = Calendar.getInstance(Locale.getDefault())

                                            //Veljača 03, 2020,10:09

                                            val months = arrayOf(
                                                "Siječanj", "Veljača", "Ožujak", "Travanj", "Svibanj", "Lipanj",
                                                "Srpanj", "Kolovoz", "Rujan", "Listopad", "Studeni", "Prosinac"
                                            )

                                            val df = SimpleDateFormat("M dd, yyyy,HH:mm", Locale.getDefault())
                                            var date = cur.get("date").asString.substringAfter(",").trim()

                                            val month = date.substringBefore(" ")

                                            date = date.replace(month, (months.indexOf(month) + 1).toString())

                                            val time = cur.get("time").asString.trim()

                                            calendar.time = df.parse("$date,$time") ?: Date()

                                            val itemDescription = ItemDescription()

                                            itemDescription.id = md5(cur.toString() + trackingId)
                                            itemDescription.trackingId = trackingId
                                            itemDescription.location = cur.get("location").asString
                                            itemDescription.description = cur.get("description").asString.trim()
                                                .replace("<Service Area>", "").replace("  ", " ")
                                            itemDescription.created = calendar.timeInMillis
                                            itemDescription.seen = 0

                                            if (itemDescription.description.contains("Dostavljeno", true)) {
                                                markAsDelivered = true
                                            }

                                            list.add(itemDescription)

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                handleTrackingInfo(trackingItem, list, results, descriptionItemsCount, markAsDelivered, showNotification)

                                countDownLatch.countDown()
                            }
                        })
                    }
                    else -> {
                        val descriptionItemsCount = AppDatabase.getInstance().getTrackingInfoDao().countTrackingItems(trackingId)

                        val list = ArrayList<ItemDescription>()

                        if (descriptionItemsCount == 0) {
                            val itemDescription = getEmptyItemForId(trackingId)
                            itemDescription.description = "Uneseni broj pošiljke nije ispravan."
                            list.add(itemDescription)
                        }

                        AppDatabase.getInstance().getTrackingDao().setUpd(trackingId, currentTimeStamp)
                        AppDatabase.getInstance().getTrackingInfoDao().insert(list)
                        countDownLatch.countDown()
                    }
                }
            }

            try {
                countDownLatch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        return Result.success()
    }

    fun handleTrackingInfo(
        trackingItem: TrackingItem,
        list: ArrayList<ItemDescription>,
        results: JsonArray?,
        descriptionItemsCount: Int,
        markAsDelivered: Boolean,
        showNotification: Boolean
    ) {
        val trackingId = trackingItem.id

        if ((list.isEmpty() || results == null) && descriptionItemsCount == 0) {
            list.add(getEmptyItemForId(trackingId))
        } else if (descriptionItemsCount > 1) {
            AppDatabase.getInstance().getTrackingInfoDao().delete(md5(trackingId + trackingId))
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
    }

    private fun getEmptyItemForId(trackingId: String): ItemDescription {
        val itemDescription = ItemDescription()
        itemDescription.id = md5(trackingId + trackingId)
        itemDescription.trackingId = trackingId
        itemDescription.location = "UNKNOWN"
        itemDescription.description = "Nema podataka za uneseni broj pošiljke."
        itemDescription.created = currentTimeStamp
        itemDescription.seen = 0
        return itemDescription
    }
}