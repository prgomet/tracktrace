package com.cometengine.tracktrace.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.cometengine.tracktrace.AppInit
import com.cometengine.tracktrace.R


class DashedLineView : View {

    private var paint: Paint = Paint()
    private var path: Path = Path()
    private var effects: PathEffect = DashPathEffect(floatArrayOf(6f, 4f, 6f, 4f), 0F)

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = AppInit.density * 4
        paint.color = ContextCompat.getColor(context, R.color.icon_color)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.pathEffect = effects
        val measuredHeight = measuredHeight.toFloat()
        val measuredWidth = measuredWidth.toFloat()
        if (measuredHeight <= measuredWidth) {
            // horizontal
            path.moveTo(0F, 0F)
            path.lineTo(measuredWidth, 0F)
            canvas.drawPath(path, paint)
        } else {
            // vertical
            path.moveTo(0F, 0F)
            path.lineTo(0F, measuredHeight)
            canvas.drawPath(path, paint)
        }
    }
}