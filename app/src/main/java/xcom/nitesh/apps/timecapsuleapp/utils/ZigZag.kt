package xcom.nitesh.apps.timecapsuleapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ZigZag @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.GRAY // Color of the zigzag line
        strokeWidth = 5f // Line thickness
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Zigzag pattern dimensions
        val amplitude = 40 // Height of the zigzag
        val wavelength = 40 // Width of the zigzag

        // Starting point of the zigzag
        var startX = 2f
        var startY = 2f

        // Draw the zigzag line vertically
        while (startY < height) {
            val endX = if (startX == 0f) wavelength.toFloat() else 0f
            val endY = startY + amplitude
            canvas.drawLine(startX, startY, endX, endY, paint)
            startX = endX
            startY = endY
        }
    }
}
