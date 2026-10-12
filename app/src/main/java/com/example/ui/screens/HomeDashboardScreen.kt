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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
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
import com.example.data.models.DefaultScenes
import com.example.ui.components.BrightnessSliderCard
import com.example.ui.components.ColorTemperatureCard
import com.example.ui.components.FuturisticPowerButton
import com.example.ui.components.InteractiveBulbStage
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.viewmodel.BulbViewModel
import com.example.viewmodel.ConnectionStatusIndicator
import java.util.Calendar

@Composable
fun HomeDashboardScreen(
    viewModel: BulbViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Contextual greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Night ambience"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Top Greeting & Connection Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Smitch Living Bulb",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Live Connection Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (uiState.connectionStatus) {
                    ConnectionStatusIndicator.STATUS_CONFIRMED -> NeonGreen.copy(alpha = 0.15f)
                    ConnectionStatusIndicator.WRITE_SUCCESS -> NeonCyan.copy(alpha = 0.15f)
                    ConnectionStatusIndicator.SENDING -> NeonAmber.copy(alpha = 0.15f)
                    ConnectionStatusIndicator.ERROR -> NeonRed.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (uiState.connectionStatus) {
                        ConnectionStatusIndicator.STATUS_CONFIRMED -> NeonGreen
                        ConnectionStatusIndicator.WRITE_SUCCESS -> NeonCyan
                        ConnectionStatusIndicator.SENDING -> NeonAmber
                        ConnectionStatusIndicator.ERROR -> NeonRed
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState.connectionStatus) {
                                    ConnectionStatusIndicator.STATUS_CONFIRMED -> NeonGreen
                                    ConnectionStatusIndicator.WRITE_SUCCESS -> NeonCyan
                                    ConnectionStatusIndicator.SENDING -> NeonAmber
                                    ConnectionStatusIndicator.ERROR -> NeonRed
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (uiState.connectionStatus) {
                            ConnectionStatusIndicator.STATUS_CONFIRMED -> "Connected"
                            ConnectionStatusIndicator.WRITE_SUCCESS -> "Sent"
                            ConnectionStatusIndicator.SENDING -> "Connecting..."
                            ConnectionStatusIndicator.ERROR -> "Error"
                            else -> "Ready"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Refresh Status
            IconButton(
                onClick = { viewModel.requestStatus() },
                enabled = !uiState.isBusy,
                modifier = Modifier.testTag("refresh_status_button")
            ) {
                if (uiState.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
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

        Spacer(modifier = Modifier.height(14.dp))

        // Central Feature: Living Bulb Floating Stage
        InteractiveBulbStage(
            bulbColor = uiState.previewColor,
            isPoweredOn = uiState.previewPower,
            brightness = uiState.previewBrightness,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Center Power Control & Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FuturisticPowerButton(
                isPoweredOn = uiState.previewPower,
                onToggle = { viewModel.togglePower() }
            )

            // Quick Scenes Scroll
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = "QUICK SCENES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DefaultScenes.presetScenes.take(4)) { scene ->
                        Surface(
                            onClick = { viewModel.applyScene(scene) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(scene.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = scene.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Brightness Slider Card
        BrightnessSliderCard(
            brightness = uiState.previewBrightness,
            isPending = uiState.isBrightnessPending,
            onBrightnessChange = { viewModel.onPreviewBrightnessChanged(it) },
            onApplyBrightness = { viewModel.applyBrightness() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Color Temperature Card
        ColorTemperatureCard(
            tempKelvin = uiState.previewTempKelvin,
            isPending = uiState.isTempPending,
            onTempChange = { viewModel.onPreviewTempChanged(it) },
            onApplyTemp = { viewModel.applyColorTemperature() }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
