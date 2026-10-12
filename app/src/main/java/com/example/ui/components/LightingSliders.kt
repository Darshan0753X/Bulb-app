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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PowerActiveGlow
import com.example.ui.theme.PowerInactive
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark

@Composable
fun FuturisticPowerButton(
    isPoweredOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonGlow by animateColorAsState(
        targetValue = if (isPoweredOn) PowerActiveGlow else PowerInactive,
        label = "powerColor"
    )

    Surface(
        onClick = onToggle,
        shape = CircleShape,
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (isPoweredOn) buttonGlow else BorderDark
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
                tint = if (isPoweredOn) buttonGlow else MutedGray,
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceElevatedDark.copy(alpha = 0.7f))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
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
                color = MutedGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$brightness%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = brightness.toFloat(),
                onValueChange = { onBrightnessChange(it.toInt()) },
                valueRange = 5f..100f,
                steps = 94,
                colors = SliderDefaults.colors(
                    thumbColor = NeonAmber,
                    activeTrackColor = NeonAmber,
                    inactiveTrackColor = SurfaceDark
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("brightness_slider")
            )

            Spacer(modifier = Modifier.size(12.dp))

            ElevatedButton(
                onClick = onApplyBrightness,
                enabled = isPending,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonAmber,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceDark,
                    disabledContentColor = MutedGray
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
    val tempLabel = when {
        tempKelvin < 3200 -> "Warm White"
        tempKelvin < 4500 -> "Neutral White"
        tempKelvin < 5500 -> "Cool White"
        else -> "Daylight / Cool"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceElevatedDark.copy(alpha = 0.7f))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
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
                color = MutedGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$tempKelvin K  ($tempLabel)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Amber to Cool Blue Gradient Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF9E43), // Warm 2700K
                            Color(0xFFFFFAEE), // Neutral 4000K
                            Color(0xFFB5D8FF)  // Cool 6500K
                        )
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                onClick = onApplyTemp,
                enabled = isPending,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceDark,
                    disabledContentColor = MutedGray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("apply_temp_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
