package com.cometengine.tracktrace.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.cometengine.tracktrace.misc.currentTimeStamp
import com.cometengine.tracktrace.database.AppDatabase
import com.cometengine.tracktrace.database.TrackingItemDescription

class MainViewModel : ViewModel() {

    private val trackingInfoDao = AppDatabase.getInstance().getTrackingInfoDao()

    val query = MutableLiveData<TrackingItemDescription>()

    val list: LiveData<PagedList<TrackingItemDescription>> = Transformations.switchMap(query) {
        if (it == null) {
            trackingInfoDao.getAllItems().toLiveData(
                Config(
                    pageSize = 20,
                    enablePlaceholders = false,
                    maxSize = PagedList.Config.MAX_SIZE_UNBOUNDED
                )
            )
        } else {
            trackingInfoDao.getItemsFor(it.trackingId).toLiveData(
                Config(
                    pageSize = 20,
                    enablePlaceholders = false,
                    maxSize = PagedList.Config.MAX_SIZE_UNBOUNDED
                )
            )
        }
    }

    fun updateSeen() {
        trackingInfoDao.updateSeen(currentTimeStamp, 0L)
    }

    init {
        query.value = null
    }
}