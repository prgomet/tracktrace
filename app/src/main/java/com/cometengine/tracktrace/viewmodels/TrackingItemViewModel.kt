package com.cometengine.tracktrace.viewmodels

import android.text.SpannableString
import androidx.lifecycle.ViewModel
import com.cometengine.tracktrace.R
import com.cometengine.tracktrace.database.TrackingItem.Companion.DELIVERED
import com.cometengine.tracktrace.database.TrackingItemDescription
import com.cometengine.tracktrace.misc.*

class TrackingItemViewModel(private val model: TrackingItemDescription) : ViewModel() {

    val title: SpannableString
        get() {
            val spannableString = SpannableString(if ((model.title ?: "").isNotEmpty()) model.title else model.trackingId)
            if (model.status == DELIVERED) {
                spannableString.setSpan(StrikeThruSpan(), 0, spannableString.length, 0)
            }
            return spannableString
        }

    val tracking = model.trackingId

    val description = model.description ?: ""

    val time = convertToTimeStamp(model.created)

    val newLabelVisible = (currentTimeStamp - model.seen) < 60000

    val location = model.location

    val statusIcon = when {
        model.status == DELIVERED -> R.drawable.ic_checkmark
        patternHP.matcher(model.trackingId).matches() -> R.drawable.logo_posta
        patternInTime.matcher(model.trackingId).matches() -> R.drawable.logo_in_time
        patternYanwen.matcher(model.trackingId).matches() -> R.drawable.logo_yanwen
        patternGLS.matcher(model.trackingId).matches() -> R.drawable.logo_gls
        patternOverseas.matcher(model.trackingId).matches() -> R.drawable.logo_overseas
        patternDPD.matcher(model.trackingId).matches() -> R.drawable.logo_dpd
        patternDHL.matcher(model.trackingId).matches() -> R.drawable.logo_dhl
        else -> R.drawable.status_badge
    }
}