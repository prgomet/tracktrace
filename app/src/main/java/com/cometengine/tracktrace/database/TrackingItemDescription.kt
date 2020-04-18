package com.cometengine.tracktrace.database


import androidx.room.PrimaryKey

class TrackingItemDescription {

    companion object {
        const val SELECT: String =
            "description.id, items.title, description.trackingId, description.location, description.description, description.created, items.status, description.seen, items.upd"
        const val SELECT_ALT: String =
            "description.id, items.title, description.trackingId, description.location, description.description, max(description.created) as created, items.status, description.seen, items.upd"
    }

    @PrimaryKey
    var id: String = ""

    var title: String? = ""

    var trackingId: String = ""

    var location: String = ""

    var description: String? = ""

    var created: Long = 0

    @TrackingItem.Companion.ItemStatus
    var status: Int = TrackingItem.TRANSIT

    var seen: Long = 0

    var upd: Long = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackingItemDescription

        if (id != other.id) return false
        if (title != other.title) return false
        if (trackingId != other.trackingId) return false
        if (location != other.location) return false
        if (description != other.description) return false
        if (created != other.created) return false
        if (status != other.status) return false
        if (seen != other.seen) return false
        if (upd != other.upd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + trackingId.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + status
        result = 31 * result + seen.hashCode()
        result = 31 * result + upd.hashCode()
        return result
    }

    override fun toString(): String {
        return "TrackingItemDescription(id='$id', title='$title', trackingId='$trackingId', location='$location', description='$description', created=$created, status=$status, seen=$seen, upd=$upd)"
    }
}