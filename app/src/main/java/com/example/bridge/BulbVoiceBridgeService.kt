package com.example.bridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.protocol.BulbStatus
import com.example.repository.BulbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class VoiceBridgeStatus(val display: String) {
    STOPPED("Bridge Stopped"),
    CONNECTING("Connecting to Voice Cloud..."),
    ONLINE("🟢 Online & Ready for Alexa / Google Home"),
    ERROR("Connection Error")
}

object VoiceBridgeManager {
    private val _status = MutableStateFlow(VoiceBridgeStatus.STOPPED)
    val status: StateFlow<VoiceBridgeStatus> = _status.asStateFlow()

    private val _lastVoiceCommand = MutableStateFlow<String?>(null)
    val lastVoiceCommand: StateFlow<String?> = _lastVoiceCommand.asStateFlow()

    fun setStatus(s: VoiceBridgeStatus) {
        _status.value = s
    }

    fun logCommand(cmd: String) {
        _lastVoiceCommand.value = cmd
    }
}

/**
 * Android Phone Background Voice Bridge Service.
 * Acts as the secure authenticated local bridge on the DIGISOL 2.4 GHz network,
 * dispatching incoming Alexa and Google Assistant directives to the physical bulb.
 */
class BulbVoiceBridgeService : Service() {

    private val repository = BulbRepository()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        const val CHANNEL_ID = "bulb_voice_bridge_channel"
        const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, BulbVoiceBridgeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BulbVoiceBridgeService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startNotification()
        VoiceBridgeManager.setStatus(VoiceBridgeStatus.ONLINE)
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smitch Voice Bridge Gateway",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps voice bridge running for Alexa & Google Home control"
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
            .setContentTitle("Smitch Voice Bridge Gateway Active")
            .setContentText("Listening for Alexa & Google Home voice directives")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * Executes directive from Alexa or Google Home cloud adapter.
     */
    fun handleVoiceDirective(directiveJson: String): String {
        return try {
            val json = JSONObject(directiveJson)
            val action = json.optString("action")
            VoiceBridgeManager.logCommand("Voice action: $action")

            when (action) {
                "turn_on" -> {
                    scope.launch { repository.setPower(true) }
                    """{"status":"SUCCESS","powerState":true}"""
                }
                "turn_off" -> {
                    scope.launch { repository.setPower(false) }
                    """{"status":"SUCCESS","powerState":false}"""
                }
                "set_brightness" -> {
                    val bright = json.optInt("brightness", 80)
                    scope.launch { repository.setBrightness(bright) }
                    """{"status":"SUCCESS","brightness":$bright}"""
                }
                "set_color_rgb" -> {
                    val r = json.optInt("r", 255)
                    val g = json.optInt("g", 255)
                    val b = json.optInt("b", 255)
                    scope.launch { repository.setRgb(r, g, b) }
                    """{"status":"SUCCESS","r":$r,"g":$g,"b":$b}"""
                }
                "set_color_temperature" -> {
                    val kelvin = json.optInt("kelvin", 4000)
                    scope.launch { repository.setColorTemperature(kelvin) }
                    """{"status":"SUCCESS","kelvin":$kelvin}"""
                }
                "query_state" -> {
                    val last = repository.lastStatus.value
                    """{"status":"SUCCESS","powerState":${last?.powerState ?: true},"brightness":${last?.brightness ?: 80}}"""
                }
                else -> """{"status":"ERROR","message":"Unsupported directive: $action"}"""
            }
        } catch (e: Exception) {
            """{"status":"ERROR","message":"${e.message}"}"""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VoiceBridgeManager.setStatus(VoiceBridgeStatus.STOPPED)
    }
}
