package com.example.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Custom View for drawing the floating bulb with genuine transparency outside the bulb graphic.
 * No box, card, or square background.
 */
class TransparentBulbView(context: Context) : View(context) {

    private var bulbColor: Int = Color(0xFF00E5FF).toArgb()
    private var isPoweredOn: Boolean = true
    private var brightness: Int = 80

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val domePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val filamentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = android.graphics.Color.argb(120, 255, 255, 255)
    }
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(71, 85, 105)
    }
    private val threadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(100, 116, 139)
    }

    init {
        // Ensure transparent background
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    fun updateVisual(color: Color, poweredOn: Boolean, bright: Int) {
        this.bulbColor = color.toArgb()
        this.isPoweredOn = poweredOn
        this.brightness = bright.coerceIn(5, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f - h * 0.08f

        val domeRadius = w * 0.32f

        // 1. Subtle Glow (Bloom)
        if (isPoweredOn) {
            val alphaFactor = (brightness / 100f).coerceIn(0.2f, 1f)
            val r = (android.graphics.Color.red(bulbColor) * alphaFactor).toInt()
            val g = (android.graphics.Color.green(bulbColor) * alphaFactor).toInt()
            val b = (android.graphics.Color.blue(bulbColor) * alphaFactor).toInt()
            val glowArgb = android.graphics.Color.argb(140, r, g, b)

            glowPaint.shader = RadialGradient(
                cx, cy, domeRadius * 1.5f,
                intArrayOf(glowArgb, android.graphics.Color.TRANSPARENT),
                floatArrayOf(0.4f, 1.0f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, domeRadius * 1.5f, glowPaint)
        }

        // 2. Bulb Dome Interior
        if (isPoweredOn) {
            val intAlpha = (80 + (brightness * 1.5f)).toInt().coerceIn(80, 240)
            domePaint.color = android.graphics.Color.argb(
                intAlpha,
                android.graphics.Color.red(bulbColor),
                android.graphics.Color.green(bulbColor),
                android.graphics.Color.blue(bulbColor)
            )
        } else {
            domePaint.color = android.graphics.Color.argb(200, 30, 38, 52)
        }
        canvas.drawCircle(cx, cy, domeRadius, domePaint)

        // 3. Glass Rim
        rimPaint.color = if (isPoweredOn) {
            android.graphics.Color.argb(220, 255, 255, 255)
        } else {
            android.graphics.Color.argb(100, 100, 116, 139)
        }
        canvas.drawCircle(cx, cy, domeRadius, rimPaint)

        // 4. Glowing Filament / LED Core
        if (isPoweredOn) {
            filamentPaint.shader = RadialGradient(
                cx, cy, domeRadius * 0.6f,
                intArrayOf(android.graphics.Color.WHITE, bulbColor, android.graphics.Color.TRANSPARENT),
                floatArrayOf(0f, 0.7f, 1.0f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, domeRadius * 0.6f, filamentPaint)
        }

        // 5. Specular highlight
        val hlPath = Path().apply {
            moveTo(cx - domeRadius * 0.6f, cy - domeRadius * 0.6f)
            cubicTo(
                cx - domeRadius * 0.8f, cy - domeRadius * 0.2f,
                cx - domeRadius * 0.7f, cy + domeRadius * 0.2f,
                cx - domeRadius * 0.4f, cy + domeRadius * 0.5f
            )
        }
        canvas.drawPath(hlPath, highlightPaint)

        // 6. Screw Base
        val neckTop = cy + domeRadius * 0.75f
        val neckW = w * 0.28f
        val baseH = h * 0.25f

        val neckRect = RectF(cx - neckW / 2f, neckTop, cx + neckW / 2f, neckTop + baseH * 0.45f)
        canvas.drawRoundRect(neckRect, 4f, 4f, basePaint)

        for (i in 0..2) {
            val ty = neckTop + baseH * 0.35f + (i * 8f)
            val threadRect = RectF(cx - (neckW * 0.9f) / 2f, ty, cx + (neckW * 0.9f) / 2f, ty + 5f)
            canvas.drawRoundRect(threadRect, 3f, 3f, threadPaint)
        }

        // Tip
        val tipRect = RectF(cx - neckW * 0.35f, neckTop + baseH * 0.75f, cx + neckW * 0.35f, neckTop + baseH * 0.95f)
        canvas.drawRoundRect(tipRect, 3f, 3f, basePaint)
    }
}
