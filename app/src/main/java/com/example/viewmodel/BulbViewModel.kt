package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diagnostics.DiagnosticLogEntry
import com.example.diagnostics.DiagnosticLogger
import com.example.network.DiscoveredDevice
import com.example.network.LocalDiscoveryManager
import com.example.network.TransportResult
import com.example.protocol.BulbModel
import com.example.protocol.BulbStatus
import com.example.protocol.FramingStrategy
import com.example.protocol.LightingEffect
import com.example.repository.BulbRepository
import com.example.repository.BulbTargetConfig
import com.example.repository.ConnectionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ConnectionStatusIndicator {
    IDLE,
    SENDING,
    WRITE_SUCCESS,
    RESPONSE_RECEIVED,
    STATUS_CONFIRMED,
    ERROR
}

data class BulbUiState(
    // Visual preview state (immediate UI response)
    val previewPower: Boolean = true,
    val previewColor: Color = Color(0xFF00E5FF),
    val previewBrightness: Int = 80,
    val previewTempKelvin: Int = 4000,
    val activeEffect: LightingEffect? = null,

    // Hardware confirmed / pending flags
    val isColorPending: Boolean = false,
    val isBrightnessPending: Boolean = false,
    val isTempPending: Boolean = false,
    val confirmedPower: Boolean? = null,
    val confirmedStatus: BulbStatus? = null,

    // Communication status
    val connectionStatus: ConnectionStatusIndicator = ConnectionStatusIndicator.IDLE,
    val statusMessage: String = "Ready for local control",
    val isBusy: Boolean = false,

    // Target configuration
    val config: BulbTargetConfig = BulbTargetConfig()
)

class BulbViewModel(application: Application) : AndroidViewModel(application) {

    val repository = BulbRepository()
    val discoveryManager = LocalDiscoveryManager(application.applicationContext)

    private val _uiState = MutableStateFlow(BulbUiState())
    val uiState: StateFlow<BulbUiState> = _uiState.asStateFlow()

    val diagnosticLogs: StateFlow<List<DiagnosticLogEntry>> = DiagnosticLogger.logs
    val lastDiagnosticEntry: StateFlow<DiagnosticLogEntry?> = DiagnosticLogger.lastEntry

    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = discoveryManager.discoveredDevices
    val isDiscovering: StateFlow<Boolean> = discoveryManager.isDiscovering
    val discoveryError: StateFlow<String?> = discoveryManager.discoveryError

    init {
        viewModelScope.launch {
            repository.config.collect { cfg ->
                _uiState.value = _uiState.value.copy(config = cfg)
            }
        }
        viewModelScope.launch {
            repository.lastStatus.collect { st ->
                st?.let { applyConfirmedStatus(it) }
            }
        }
    }

    private fun applyConfirmedStatus(status: BulbStatus) {
        val newPower = status.powerState ?: _uiState.value.previewPower
        val newColor = if (status.r != null && status.g != null && status.b != null) {
            Color(status.r, status.g, status.b)
        } else {
            _uiState.value.previewColor
        }
        val newBright = status.brightness ?: _uiState.value.previewBrightness
        val newTemp = status.tempKelvin ?: _uiState.value.previewTempKelvin

        val activeEff = when {
            status.candleActive == true -> LightingEffect.CANDLE
            status.rainbowActive == true -> LightingEffect.RANDOM
            status.flashActive == true -> LightingEffect.STROBE
            else -> null
        }

        _uiState.value = _uiState.value.copy(
            confirmedStatus = status,
            confirmedPower = status.powerState,
            previewPower = newPower,
            previewColor = newColor,
            previewBrightness = newBright,
            previewTempKelvin = newTemp,
            activeEffect = activeEff,
            isColorPending = false,
            isBrightnessPending = false,
            isTempPending = false,
            connectionStatus = ConnectionStatusIndicator.STATUS_CONFIRMED,
            statusMessage = "Confirmed status received from ${status.model ?: "Bulb"}"
        )
    }

    // --- Visual Preview updates (Immediate, no network traffic) ---

    fun onPreviewColorChanged(color: Color) {
        _uiState.value = _uiState.value.copy(
            previewColor = color,
            isColorPending = true
        )
    }

    fun onPreviewBrightnessChanged(brightness: Int) {
        _uiState.value = _uiState.value.copy(
            previewBrightness = brightness,
            isBrightnessPending = true
        )
    }

    fun onPreviewTempChanged(kelvin: Int) {
        _uiState.value = _uiState.value.copy(
            previewTempKelvin = kelvin,
            isTempPending = true
        )
    }

    // --- Explicit Hardware Actions ---

    fun togglePower() {
        val targetPower = !_uiState.value.previewPower
        _uiState.value = _uiState.value.copy(
            previewPower = targetPower,
            isBusy = true,
            connectionStatus = ConnectionStatusIndicator.SENDING,
            statusMessage = "Sending power ${if (targetPower) "ON" else "OFF"}..."
        )

        viewModelScope.launch {
            val result = repository.setPower(targetPower)
            handleCommandResult(result, "Power ${if (targetPower) "ON" else "OFF"}")
        }
    }

    fun applyColor() {
        val color = _uiState.value.previewColor
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)

        _uiState.value = _uiState.value.copy(
            isBusy = true,
            connectionStatus = ConnectionStatusIndicator.SENDING,
            statusMessage = "Applying RGB ($r, $g, $b)..."
        )

        viewModelScope.launch {
            val result = repository.setRgb(r, g, b)
            handleCommandResult(result, "RGB ($r, $g, $b)")
            if (result.success) {
                _uiState.value = _uiState.value.copy(isColorPending = false)
            }
        }
    }

    fun applyBrightness() {
        val brightness = _uiState.value.previewBrightness
        _uiState.value = _uiState.value.copy(
            isBusy = true,
            connectionStatus = ConnectionStatusIndicator.SENDING,
            statusMessage = "Applying Brightness $brightness..."
        )

        viewModelScope.launch {
            val result = repository.setBrightness(brightness)
            handleCommandResult(result, "Brightness $brightness")
            if (result.success) {
                _uiState.value = _uiState.value.copy(isBrightnessPending = false)
            }
        }
    }

    fun applyColorTemperature() {
        val kelvin = _uiState.value.previewTempKelvin
        _uiState.value = _uiState.value.copy(
            isBusy = true,
            connectionStatus = ConnectionStatusIndicator.SENDING,
            statusMessage = "Applying CCT ${kelvin}K..."
        )

        viewModelScope.launch {
            val result = repository.setColorTemperature(kelvin)
            handleCommandResult(result, "CCT ${kelvin}K")
            if (result.success) {
                _uiState.value = _uiState.value.copy(isTempPending = false)
            }
        }
    }

    fun toggleEffect(effect: LightingEffect) {
        val currentlyActive = _uiState.value.activeEffect == effect
        val enable = !currentlyActive

        _uiState.value = _uiState.value.copy(
            isBusy = true,
            connectionStatus = ConnectionStatusIndicator.SENDING,
            statusMessage = "Sending ${effect.displayName} ${if (enable) "Enable" else "Disable"}..."
        )

        viewModelScope.launch {
            val result = repository.setEffect(effect, enable)
            handleCommandResult(result, "${effect.displayName} effect")
            if (result.success) {
                _uiState.value = _uiState.value.copy(
                    activeEffect = if (enable) effect else null
                )
            }
        }
    }

    fun requestStatus() {
        _uiState.value = _uiState.value.copy(
            isBusy = true,
            connectionStatus = ConnectionStatusIndicator.SENDING,
            statusMessage = "Querying device status (0001xx03)..."
        )

        viewModelScope.launch {
            val result = repository.queryStatus()
            if (result.success) {
                if (result.response != null) {
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        connectionStatus = ConnectionStatusIndicator.RESPONSE_RECEIVED,
                        statusMessage = "Response received: ${result.response.take(40)}..."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        connectionStatus = ConnectionStatusIndicator.WRITE_SUCCESS,
                        statusMessage = "Status command written (no response received)"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    connectionStatus = ConnectionStatusIndicator.ERROR,
                    statusMessage = "Status query failed: ${result.error ?: "Unknown error"}"
                )
            }
        }
    }

    private fun handleCommandResult(result: TransportResult, actionName: String) {
        _uiState.value = if (result.success) {
            _uiState.value.copy(
                isBusy = false,
                connectionStatus = ConnectionStatusIndicator.WRITE_SUCCESS,
                statusMessage = "$actionName sent successfully"
            )
        } else {
            _uiState.value.copy(
                isBusy = false,
                connectionStatus = ConnectionStatusIndicator.ERROR,
                statusMessage = "Failed to send $actionName: ${result.error ?: "Socket error"}"
            )
        }
    }

    // --- Configuration management ---

    fun setConnectionMode(mode: ConnectionMode) {
        repository.updateConfig { it.copy(mode = mode, host = mode.defaultHost, port = mode.defaultPort) }
    }

    fun setCustomTarget(host: String, port: Int) {
        repository.updateConfig { it.copy(host = host, port = port) }
    }

    fun setFramingStrategy(framing: FramingStrategy) {
        repository.updateConfig { it.copy(framing = framing) }
    }

    fun setBulbModel(model: BulbModel) {
        repository.updateConfig { it.copy(model = model) }
    }

    fun setLanControlEnabled(enabled: Boolean) {
        repository.updateConfig { it.copy(lanControlEnabled = enabled) }
    }

    fun selectDiscoveredDevice(device: DiscoveredDevice) {
        repository.updateConfig {
            it.copy(
                mode = ConnectionMode.HOME_LAN,
                host = device.hostAddress,
                port = device.port
            )
        }
        _uiState.value = _uiState.value.copy(
            statusMessage = "Selected discovered device: ${device.serviceName} (${device.hostAddress}:${device.port})"
        )
    }

    fun sendDiagnosticCommand(commandBody: String, framing: FramingStrategy, readResponse: Boolean) {
        viewModelScope.launch {
            repository.sendDiagnosticRawCommand(commandBody, framing, readResponse)
        }
    }

    fun startDiscovery() {
        discoveryManager.startDiscovery()
    }

    fun stopDiscovery() {
        discoveryManager.stopDiscovery()
    }
}
