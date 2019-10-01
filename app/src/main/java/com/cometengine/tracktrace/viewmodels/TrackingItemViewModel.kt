package com.cometengine.tracktrace.viewmodels

import android.text.SpannableString
import androidx.lifecycle.ViewModel
import com.cometengine.tracktrace.R
import com.cometengine.tracktrace.misc.StrikeThruSpan
import com.cometengine.tracktrace.misc.convertToTimeStamp
import com.cometengine.tracktrace.misc.currentTimeStamp
import com.cometengine.tracktrace.database.TrackingItem.Companion.DELIVERED
import com.cometengine.tracktrace.database.TrackingItemDescription

class TrackingItemViewModel(private val model: TrackingItemDescription) : ViewModel() {

    val title: SpannableString
        get() {
            val spannableString = SpannableString(if (model.title.isNotEmpty()) model.title else model.trackingId)
            if (model.status == DELIVERED) {
                spannableString.setSpan(StrikeThruSpan(), 0, spannableString.length, 0)
            }
            return spannableString
        }

    val tracking = model.trackingId

    val description = model.description

    val time = convertToTimeStamp(model.created)

    val newLabelVisible = (currentTimeStamp - model.seen) < 60000

    val location = model.location

    val statusIcon = if (model.status == DELIVERED) {
        R.drawable.ic_checkmark
    } else {
        R.drawable.status_badge
    }
}