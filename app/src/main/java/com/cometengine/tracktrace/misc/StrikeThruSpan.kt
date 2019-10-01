package com.cometengine.tracktrace.misc

import android.text.TextPaint
import android.text.style.StyleSpan

class StrikeThruSpan : StyleSpan(0) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint)
    }

    private fun applyCustomTypeFace(paint: TextPaint) {
        paint.isStrikeThruText = true
    }
}