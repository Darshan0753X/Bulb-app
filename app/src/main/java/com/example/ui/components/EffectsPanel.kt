package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocol.LightingEffect
import com.example.ui.theme.BorderDark
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark

@Composable
fun EffectsPanel(
    activeEffect: LightingEffect?,
    onToggleEffect: (LightingEffect) -> Unit,
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
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "LIGHTING EFFECTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MutedGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "BYTECODE RESEARCH",
                fontSize = 10.sp,
                color = MutedGray.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EffectItem(
                effect = LightingEffect.STROBE,
                icon = Icons.Default.FlashOn,
                accentColor = NeonCyan,
                isActive = activeEffect == LightingEffect.STROBE,
                onClick = { onToggleEffect(LightingEffect.STROBE) },
                modifier = Modifier.weight(1f)
            )
            EffectItem(
                effect = LightingEffect.RANDOM,
                icon = Icons.Default.Shuffle,
                accentColor = NeonGreen,
                isActive = activeEffect == LightingEffect.RANDOM,
                onClick = { onToggleEffect(LightingEffect.RANDOM) },
                modifier = Modifier.weight(1f)
            )
            EffectItem(
                effect = LightingEffect.CANDLE,
                icon = Icons.Default.LocalFireDepartment,
                accentColor = NeonAmber,
                isActive = activeEffect == LightingEffect.CANDLE,
                onClick = { onToggleEffect(LightingEffect.CANDLE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EffectItem(
    effect: LightingEffect,
    icon: ImageVector,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) accentColor.copy(alpha = 0.18f) else SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) accentColor else BorderDark
        ),
        modifier = modifier
            .testTag("effect_item_${effect.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = effect.displayName,
                tint = if (isActive) accentColor else MutedGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = effect.displayName,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) Color.White else MutedGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isActive) "ACTIVE" else "OFF",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) accentColor else MutedGray.copy(alpha = 0.5f)
            )
        }
    }
}
