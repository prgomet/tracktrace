package com.cometengine.tracktrace.database

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "items", indices = [Index(value = ["created"])])
class TrackingItem {

    companion object {
        @IntDef(TRANSIT, DELIVERED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ItemStatus

        const val TRANSIT = 0
        const val DELIVERED = 1
    }

    @PrimaryKey
    var id: String = ""

    var title: String = ""

    var created: Long = 0

    var upd: Long = 0

    @ItemStatus
    var status: Int = TRANSIT
}