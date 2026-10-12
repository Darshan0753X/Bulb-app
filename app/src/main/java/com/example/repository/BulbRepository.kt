package com.example.repository

import com.example.network.BulbTransport
import com.example.network.TcpTransport
import com.example.network.TransportResult
import com.example.protocol.BulbModel
import com.example.protocol.BulbStatus
import com.example.protocol.CommandCodec
import com.example.protocol.FramingStrategy
import com.example.protocol.LightingEffect
import com.example.protocol.StatusParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectionMode(val title: String, val defaultHost: String, val defaultPort: Int) {
    HOTSPOT_SETUP("Bulb Hotspot Mode (Smitch_v1ab)", "192.168.4.1", 80),
    HOME_LAN("Home Wi-Fi LAN Mode (DIGISOL)", "192.168.1.50", 80)
}

data class BulbTargetConfig(
    val mode: ConnectionMode = ConnectionMode.HOTSPOT_SETUP,
    val host: String = ConnectionMode.HOTSPOT_SETUP.defaultHost,
    val port: Int = ConnectionMode.HOTSPOT_SETUP.defaultPort,
    val framing: FramingStrategy = FramingStrategy.SINGLE_CR,
    val model: BulbModel = BulbModel.RGB_CCT,
    val lanControlEnabled: Boolean = false,
    val maxBrightnessConfig: Int = 100
)

class BulbRepository(
    private val transport: BulbTransport = TcpTransport("SmitchLocalTcp")
) {

    private val _config = MutableStateFlow(BulbTargetConfig())
    val config: StateFlow<BulbTargetConfig> = _config.asStateFlow()

    private val _lastStatus = MutableStateFlow<BulbStatus?>(null)
    val lastStatus: StateFlow<BulbStatus?> = _lastStatus.asStateFlow()

    fun updateConfig(updater: (BulbTargetConfig) -> BulbTargetConfig) {
        _config.value = updater(_config.value)
    }

    suspend fun queryStatus(): TransportResult {
        val currentConfig = _config.value
        val cmd = CommandCodec.buildStatusQueryCommand()
        val result = transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = cmd,
            framing = currentConfig.framing,
            readResponse = true
        )
        if (result.success && result.response != null) {
            val parseResult = StatusParser.parse(result.response)
            if (parseResult.isSuccess) {
                _lastStatus.value = parseResult.getOrNull()
            }
        }
        return result
    }

    suspend fun setPower(on: Boolean): TransportResult {
        val currentConfig = _config.value
        val cmd = CommandCodec.buildPowerCommand(on)
        return transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = cmd,
            framing = currentConfig.framing,
            readResponse = false
        )
    }

    suspend fun setRgb(r: Int, g: Int, b: Int): TransportResult {
        val currentConfig = _config.value
        val cmd = CommandCodec.buildRgbCommand(
            r = r,
            g = g,
            b = b,
            model = currentConfig.model,
            enableWhiteSpecialCase = false
        )
        return transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = cmd,
            framing = currentConfig.framing,
            readResponse = false
        )
    }

    suspend fun setBrightness(brightness: Int): TransportResult {
        val currentConfig = _config.value
        val cmd = CommandCodec.buildBrightnessCommand(
            brightness = brightness,
            model = currentConfig.model,
            maxBrightness = currentConfig.maxBrightnessConfig
        )
        return transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = cmd,
            framing = currentConfig.framing,
            readResponse = false
        )
    }

    suspend fun setColorTemperature(kelvin: Int): TransportResult {
        val currentConfig = _config.value
        val cmd = CommandCodec.buildColorTemperatureCommand(
            temperatureKelvin = kelvin,
            model = currentConfig.model
        )
        return transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = cmd,
            framing = currentConfig.framing,
            readResponse = false
        )
    }

    suspend fun setEffect(effect: LightingEffect, enable: Boolean): TransportResult {
        val currentConfig = _config.value
        val cmd = CommandCodec.buildEffectCommand(effect, enable)
        return transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = cmd,
            framing = currentConfig.framing,
            readResponse = false
        )
    }

    suspend fun sendDiagnosticRawCommand(commandBody: String, framing: FramingStrategy, readResponse: Boolean): TransportResult {
        val currentConfig = _config.value
        return transport.sendCommand(
            host = currentConfig.host,
            port = currentConfig.port,
            commandBody = commandBody,
            framing = framing,
            readResponse = readResponse
        )
    }
}
