package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PowerActiveGlow
import com.example.ui.theme.PowerInactive

@Composable
fun FuturisticPowerButton(
    isPoweredOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val buttonGlow by animateColorAsState(
        targetValue = if (isPoweredOn) PowerActiveGlow else PowerInactive,
        label = "powerColor"
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onToggle()
        },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (isPoweredOn) buttonGlow else MaterialTheme.colorScheme.outline
        ),
        shadowElevation = if (isPoweredOn) 12.dp else 2.dp,
        modifier = modifier
            .size(72.dp)
            .testTag("power_toggle_button")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    if (isPoweredOn) {
                        Brush.radialGradient(
                            colors = listOf(buttonGlow.copy(alpha = 0.35f), Color.Transparent)
                        )
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = if (isPoweredOn) "Power Off" else "Power On",
                tint = if (isPoweredOn) buttonGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun BrightnessSliderCard(
    brightness: Int,
    isPending: Boolean,
    onBrightnessChange: (Int) -> Unit,
    onApplyBrightness: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Brightness6,
                contentDescription = null,
                tint = NeonAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "BRIGHTNESS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$brightness%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = brightness.toFloat(),
                onValueChange = { onBrightnessChange(it.toInt()) },
                valueRange = 5f..100f,
                steps = 94,
                colors = SliderDefaults.colors(
                    thumbColor = NeonAmber,
                    activeTrackColor = NeonAmber,
                    inactiveTrackColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("brightness_slider")
            )

            Spacer(modifier = Modifier.size(12.dp))

            ElevatedButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onApplyBrightness()
                },
                enabled = isPending,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonAmber,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("apply_brightness_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ColorTemperatureCard(
    tempKelvin: Int,
    isPending: Boolean,
    onTempChange: (Int) -> Unit,
    onApplyTemp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val tempLabel = when {
        tempKelvin < 3200 -> "Warm White"
        tempKelvin < 4500 -> "Neutral White"
        tempKelvin < 5500 -> "Cool White"
        else -> "Daylight / Cool"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "WHITE CCT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$tempKelvin K  ($tempLabel)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF9E43),
                            Color(0xFFFFFAEE),
                            Color(0xFFB5D8FF)
                        )
                    )
                )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = tempKelvin.toFloat(),
                onValueChange = { onTempChange(it.toInt()) },
                valueRange = 2700f..6500f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("cct_slider")
            )

            Spacer(modifier = Modifier.size(12.dp))

            ElevatedButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onApplyTemp()
                },
                enabled = isPending,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("apply_temp_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
