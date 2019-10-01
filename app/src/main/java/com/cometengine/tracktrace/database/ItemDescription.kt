package com.cometengine.tracktrace.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "description", indices = [Index(value = ["trackingId", "created"])])
class ItemDescription {

    @PrimaryKey
    var id: String = ""

    var trackingId: String = ""

    var location: String = ""

    var description: String = ""

    var created: Long = 0

    var seen: Long = 0
}