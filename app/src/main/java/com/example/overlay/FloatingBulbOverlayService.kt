package com.example.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.repository.BulbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

class FloatingBulbOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var bulbView: TransparentBulbView
    private lateinit var bulbLayoutParams: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences

    private var popupControlView: View? = null
    private var popupLayoutParams: WindowManager.LayoutParams? = null

    private val repository = BulbRepository()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        const val CHANNEL_ID = "floating_bulb_overlay_channel"
        const val NOTIFICATION_ID = 1001
        const val PREFS_NAME = "floating_bulb_prefs"
        const val KEY_POS_X = "overlay_pos_x"
        const val KEY_POS_Y = "overlay_pos_y"
        const val KEY_ENABLED = "overlay_enabled"

        fun start(context: Context) {
            val intent = Intent(context, FloatingBulbOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBulbOverlayService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        startForegroundNotification()
        createFloatingBulb()
        observeSharedState()
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smitch Floating Bulb Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the floating bulb overlay active"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating Bulb Active")
            .setContentText("Tap to open controller or drag bulb on screen")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createFloatingBulb() {
        val bulbSize = (72 * resources.displayMetrics.density).toInt()

        val savedX = prefs.getInt(KEY_POS_X, 100)
        val savedY = prefs.getInt(KEY_POS_Y, 300)

        bulbLayoutParams = WindowManager.LayoutParams(
            bulbSize,
            bulbSize,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        bulbView = TransparentBulbView(this)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchStartTime = 0L

        bulbView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bulbLayoutParams.x
                    initialY = bulbLayoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    bulbLayoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    bulbLayoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(bulbView, bulbLayoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = abs(event.rawY - initialTouchY)
                    val duration = System.currentTimeMillis() - touchStartTime

                    if (deltaX < 15 && deltaY < 15 && duration < 300) {
                        // It was a tap -> Toggle compact control popup
                        togglePopupControls()
                    } else {
                        // It was a drag -> Save position
                        prefs.edit()
                            .putInt(KEY_POS_X, bulbLayoutParams.x)
                            .putInt(KEY_POS_Y, bulbLayoutParams.y)
                            .apply()
                        OverlayStateManager.setPosition(bulbLayoutParams.x, bulbLayoutParams.y)
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(bulbView, bulbLayoutParams)
            prefs.edit().putBoolean(KEY_ENABLED, true).apply()
            OverlayStateManager.updateState { it.copy(isEnabled = true) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeSharedState() {
        scope.launch {
            OverlayStateManager.state.collect { st ->
                bulbView.updateVisual(st.color, st.isPoweredOn, st.brightness)
            }
        }
    }

    private fun togglePopupControls() {
        if (popupControlView != null) {
            dismissPopupControls()
        } else {
            showPopupControls()
        }
    }

    private fun showPopupControls() {
        if (popupControlView != null) return

        val widthPx = (240 * resources.displayMetrics.density).toInt()

        popupLayoutParams = WindowManager.LayoutParams(
            widthPx,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (bulbLayoutParams.x - widthPx / 4).coerceAtLeast(20)
            y = (bulbLayoutParams.y + bulbLayoutParams.height + 10)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.argb(235, 19, 24, 36))
                cornerRadius = 32f
                setStroke(2, android.graphics.Color.argb(120, 40, 54, 79))
            }
        }

        // Title and Close Row
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "Floating Controls"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(android.graphics.Color.argb(180, 255, 255, 255))
            textSize = 16f
            setPadding(16, 8, 16, 8)
            setOnClickListener { dismissPopupControls() }
        }
        header.addView(title)
        header.addView(closeBtn)
        container.addView(header)

        // Power Toggle Button
        val powerBtn = TextView(this).apply {
            text = if (OverlayStateManager.state.value.isPoweredOn) "Power: ON" else "Power: OFF"
            setTextColor(if (OverlayStateManager.state.value.isPoweredOn) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(20, 16, 20, 16)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(if (OverlayStateManager.state.value.isPoweredOn) android.graphics.Color.rgb(0, 229, 255) else android.graphics.Color.rgb(40, 54, 79))
                cornerRadius = 18f
            }
            setOnClickListener {
                val newP = !OverlayStateManager.state.value.isPoweredOn
                OverlayStateManager.updateState { it.copy(isPoweredOn = newP) }
                text = if (newP) "Power: ON" else "Power: OFF"
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (newP) android.graphics.Color.rgb(0, 229, 255) else android.graphics.Color.rgb(40, 54, 79))
                    cornerRadius = 18f
                }
                scope.launch { repository.setPower(newP) }
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 16
            }
        }
        container.addView(powerBtn)

        // Brightness Seekbar
        val brightLabel = TextView(this).apply {
            text = "Brightness: ${OverlayStateManager.state.value.brightness}%"
            setTextColor(android.graphics.Color.argb(180, 255, 255, 255))
            textSize = 11f
            setPadding(0, 16, 0, 4)
        }
        val brightBar = SeekBar(this).apply {
            max = 100
            progress = OverlayStateManager.state.value.brightness
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    val p = progress.coerceAtLeast(5)
                    brightLabel.text = "Brightness: $p%"
                    OverlayStateManager.updateState { it.copy(brightness = p) }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {
                    scope.launch { repository.setBrightness(OverlayStateManager.state.value.brightness) }
                }
            })
        }
        container.addView(brightLabel)
        container.addView(brightBar)

        // Quick Preset Colors
        val colorsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 8)
        }
        val presets = listOf(
            android.graphics.Color.rgb(0, 229, 255),   // Cyan
            android.graphics.Color.rgb(255, 179, 0),  // Amber
            android.graphics.Color.rgb(255, 51, 102),  // Red
            android.graphics.Color.rgb(0, 230, 118),  // Green
            android.graphics.Color.rgb(255, 255, 255) // White
        )
        for (c in presets) {
            val dot = View(this).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(c)
                }
                layoutParams = LinearLayout.LayoutParams(54, 54).apply { setMargins(8, 0, 8, 0) }
                setOnClickListener {
                    val composeCol = androidx.compose.ui.graphics.Color(c)
                    OverlayStateManager.updateState { it.copy(color = composeCol) }
                    scope.launch {
                        val r = android.graphics.Color.red(c)
                        val g = android.graphics.Color.green(c)
                        val b = android.graphics.Color.blue(c)
                        repository.setRgb(r, g, b)
                    }
                }
            }
            colorsRow.addView(dot)
        }
        container.addView(colorsRow)

        popupControlView = container
        windowManager.addView(popupControlView, popupLayoutParams)
    }

    private fun dismissPopupControls() {
        popupControlView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            popupControlView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissPopupControls()
        try {
            windowManager.removeView(bulbView)
        } catch (_: Exception) {}
        prefs.edit().putBoolean(KEY_ENABLED, false).apply()
        OverlayStateManager.updateState { it.copy(isEnabled = false) }
    }
}
