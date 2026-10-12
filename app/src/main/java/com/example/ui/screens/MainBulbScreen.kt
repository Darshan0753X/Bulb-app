package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.ConnectionMode
import com.example.ui.components.BrightnessSliderCard
import com.example.ui.components.ColorControlSection
import com.example.ui.components.ColorTemperatureCard
import com.example.ui.components.EffectsPanel
import com.example.ui.components.FuturisticPowerButton
import com.example.ui.components.InteractiveBulbStage
import com.example.ui.theme.BorderDark
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.viewmodel.BulbViewModel
import com.example.viewmodel.ConnectionStatusIndicator

@Composable
fun MainBulbScreen(
    viewModel: BulbViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var activeSubTab by remember { mutableStateOf("COLOR") } // "COLOR", "WHITE", "EFFECTS"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Top Header: App Title, Target info & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState.connectionStatus) {
                                    ConnectionStatusIndicator.STATUS_CONFIRMED -> NeonGreen
                                    ConnectionStatusIndicator.WRITE_SUCCESS -> NeonCyan
                                    ConnectionStatusIndicator.SENDING -> NeonAmber
                                    ConnectionStatusIndicator.ERROR -> NeonRed
                                    else -> MutedGray
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GLOW",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    text = "${uiState.config.model.displayName} • ${uiState.config.host}:${uiState.config.port}",
                    fontSize = 11.sp,
                    color = MutedGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Refresh Status button
            IconButton(
                onClick = { viewModel.requestStatus() },
                enabled = !uiState.isBusy,
                modifier = Modifier.testTag("refresh_status_button")
            ) {
                if (uiState.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = NeonCyan
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Query Status",
                        tint = NeonCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Connection Status Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceDark.copy(alpha = 0.8f),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (uiState.connectionStatus) {
                        ConnectionStatusIndicator.ERROR -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (uiState.connectionStatus) {
                        ConnectionStatusIndicator.STATUS_CONFIRMED -> NeonGreen
                        ConnectionStatusIndicator.WRITE_SUCCESS -> NeonCyan
                        ConnectionStatusIndicator.ERROR -> NeonRed
                        else -> MutedGray
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.statusMessage,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Central Feature: Interactive Bulb Stage
        InteractiveBulbStage(
            bulbColor = uiState.previewColor,
            isPoweredOn = uiState.previewPower,
            brightness = uiState.previewBrightness,
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Central Power Button & Mode selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Power button
            FuturisticPowerButton(
                isPoweredOn = uiState.previewPower,
                onToggle = { viewModel.togglePower() }
            )

            // Sub-control navigation pills
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                listOf("COLOR", "WHITE", "EFFECTS").forEach { tab ->
                    val isSelected = activeSubTab == tab
                    Surface(
                        onClick = { activeSubTab = tab },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                        modifier = Modifier.testTag("tab_button_${tab.lowercase()}")
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonCyan else MutedGray,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Brightness slider is always accessible
        BrightnessSliderCard(
            brightness = uiState.previewBrightness,
            isPending = uiState.isBrightnessPending,
            onBrightnessChange = { viewModel.onPreviewBrightnessChanged(it) },
            onApplyBrightness = { viewModel.applyBrightness() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Sub Tab Content
        when (activeSubTab) {
            "COLOR" -> {
                ColorControlSection(
                    currentColor = uiState.previewColor,
                    isPending = uiState.isColorPending,
                    onColorChanged = { viewModel.onPreviewColorChanged(it) },
                    onApplyColor = { viewModel.applyColor() }
                )
            }
            "WHITE" -> {
                ColorTemperatureCard(
                    tempKelvin = uiState.previewTempKelvin,
                    isPending = uiState.isTempPending,
                    onTempChange = { viewModel.onPreviewTempChanged(it) },
                    onApplyTemp = { viewModel.applyColorTemperature() }
                )
            }
            "EFFECTS" -> {
                EffectsPanel(
                    activeEffect = uiState.activeEffect,
                    onToggleEffect = { viewModel.toggleEffect(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
