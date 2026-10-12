package com.example.overlay

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OverlayBulbState(
    val isEnabled: Boolean = false,
    val isPoweredOn: Boolean = true,
    val color: Color = Color(0xFF00E5FF),
    val brightness: Int = 80,
    val tempKelvin: Int = 4000,
    val isControlPanelOpen: Boolean = false,
    val posX: Int = 100,
    val posY: Int = 300
)

object OverlayStateManager {
    private val _state = MutableStateFlow(OverlayBulbState())
    val state: StateFlow<OverlayBulbState> = _state.asStateFlow()

    fun updateState(updater: (OverlayBulbState) -> OverlayBulbState) {
        _state.value = updater(_state.value)
    }

    fun setPosition(x: Int, y: Int) {
        _state.value = _state.value.copy(posX = x, posY = y)
    }

    fun setControlPanelOpen(open: Boolean) {
        _state.value = _state.value.copy(isControlPanelOpen = open)
    }
}
