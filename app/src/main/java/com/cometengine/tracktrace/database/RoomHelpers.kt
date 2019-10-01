package com.cometengine.tracktrace.database

import androidx.work.Data
import com.cometengine.tracktrace.misc.enqueueWorker
import com.cometengine.tracktrace.workers.TrackingWorker


class InsertTrackingItem : RoomAsyncTask<TrackingItem, Int>() {
    override fun doInBackground(param: TrackingItem): Int {
        AppDatabase.getInstance().getTrackingDao().insert(param)

        Data.Builder().apply {
            putString("trackingId", param.id)
            putBoolean("notifications", false)
        }.also { data ->
            enqueueWorker<TrackingWorker>(data.build())
        }

        return 1
    }
}

class UpdateTrackingItemTitle(val trackingId: String) : RoomAsyncTask<String, Int>() {
    override fun doInBackground(param: String): Int {
        AppDatabase.getInstance().getTrackingDao().updateItemTitle(trackingId, param)
        return 1
    }
}

class DeleteTrackingItem : RoomAsyncTask<String, Int>() {
    override fun doInBackground(param: String): Int {
        AppDatabase.getInstance().getTrackingDao().delete(param)
        AppDatabase.getInstance().getTrackingInfoDao().delete(param)
        return 1
    }
}

class ChangeStatus(val trackingId: String) : RoomAsyncTask<Int, Int>() {
    override fun doInBackground(param: Int): Int {
        AppDatabase.getInstance().getTrackingDao().updateItemStatus(trackingId, param)
        return 1
    }
}