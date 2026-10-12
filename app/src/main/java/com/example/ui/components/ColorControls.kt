package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun InteractiveColorWheel(
    currentColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .size(260.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        currentColor.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), CircleShape)
            .testTag("color_wheel_picker")
    ) {
        val sizePx = constraints.maxWidth.toFloat()
        val radius = sizePx / 2f
        val center = Offset(radius, radius)
        val outerRadius = radius * 0.94f
        val innerRadius = radius * 0.52f

        var thumbPos by remember { mutableStateOf(Offset(radius, radius * 0.35f)) }

        fun updateFromOffset(offset: Offset) {
            val dx = offset.x - center.x
            val dy = offset.y - center.y
            val dist = sqrt(dx * dx + dy * dy).coerceIn(innerRadius, outerRadius)
            val angleRad = atan2(dy, dx)
            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
            if (angleDeg < 0) angleDeg += 360f

            thumbPos = Offset(
                center.x + (cos(angleRad) * dist).toFloat(),
                center.y + (sin(angleRad) * dist).toFloat()
            )

            // Dynamic saturation mapped from inner to outer ring
            val saturation = ((dist - innerRadius) / (outerRadius - innerRadius)).coerceIn(0.35f, 1f)
            val hsv = floatArrayOf(angleDeg, saturation, 1.0f)
            val argb = android.graphics.Color.HSVToColor(hsv)
            onColorChanged(Color(argb))
        }

        val surfaceColor = MaterialTheme.colorScheme.surface
        val outlineColor = MaterialTheme.colorScheme.outline

        Canvas(
            modifier = Modifier
                .size(260.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset -> updateFromOffset(offset) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        updateFromOffset(change.position)
                    }
                }
        ) {
            val sweepColors = listOf(
                Color(0xFFFF1744), // Crimson Red
                Color(0xFFFF9100), // Amber Gold
                Color(0xFFFFEA00), // Sunshine Yellow
                Color(0xFF00E676), // Spring Green
                Color(0xFF00E5FF), // Electric Cyan
                Color(0xFF2979FF), // Deep Azure
                Color(0xFFD500F9), // Neon Magenta
                Color(0xFFFF1744)  // Loop
            )

            // 1. Chromatic Donut Ring
            drawCircle(
                brush = Brush.sweepGradient(sweepColors, center),
                radius = (outerRadius + innerRadius) / 2f,
                center = center,
                style = Stroke(width = outerRadius - innerRadius)
            )

            // 2. Subtle Soft Radial Light Bleed
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            // 3. Inner Frosted Glass Core with Current Color Ambient Bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentColor.copy(alpha = 0.35f),
                        surfaceColor.copy(alpha = 0.95f),
                        surfaceColor
                    ),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            // Inner Ring Border Bezel
            drawCircle(
                color = outlineColor.copy(alpha = 0.8f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Outer Ring Border Bezel
            drawCircle(
                color = outlineColor.copy(alpha = 0.8f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Center Glowing Disc Indicator
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, currentColor),
                    center = center,
                    radius = innerRadius * 0.45f
                ),
                radius = innerRadius * 0.45f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = innerRadius * 0.45f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 5. Aesthetic Glass Halo Thumb Indicator
            // Soft Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 18.dp.toPx(),
                center = thumbPos + Offset(0f, 3f)
            )
            // Outer Glowing Ring
            drawCircle(
                color = currentColor.copy(alpha = 0.5f),
                radius = 17.dp.toPx(),
                center = thumbPos
            )
            // Crisp White Ring
            drawCircle(
                color = Color.White,
                radius = 13.dp.toPx(),
                center = thumbPos,
                style = Stroke(width = 2.5.dp.toPx())
            )
            // Vibrant Center Dot
            drawCircle(
                color = currentColor,
                radius = 10.dp.toPx(),
                center = thumbPos
            )
        }
    }
}

@Composable
fun FullColorStudioSection(
    currentColor: Color,
    isPending: Boolean,
    favoriteColors: List<Color>,
    recentColors: List<Color>,
    onColorChanged: (Color) -> Unit,
    onToggleFavorite: (Color) -> Unit,
    onApplyColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val r = (currentColor.red * 255).roundToInt()
    val g = (currentColor.green * 255).roundToInt()
    val b = (currentColor.blue * 255).roundToInt()
    val hexCode = String.format("#%02X%02X%02X", r, g, b)
    val isFavorite = favoriteColors.contains(currentColor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHROMATIC COLOR WHEEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isPending) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Text(
                        text = "UNAPPLIED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Wheel
        InteractiveColorWheel(
            currentColor = currentColor,
            onColorChanged = onColorChanged
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Live Telemetry Bar & Apply Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = hexCode,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "R: $r  G: $g  B: $b",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onToggleFavorite(currentColor) },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ElevatedButton(
                onClick = onApplyColor,
                enabled = isPending,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("apply_color_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Favorite Swatches
        if (favoriteColors.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FAVORITES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(favoriteColors) { color ->
                        ColorSwatch(color = color, isSelected = color == currentColor, onClick = { onColorChanged(color) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Recent Swatches
        if (recentColors.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RECENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentColors) { color ->
                        ColorSwatch(color = color, isSelected = color == currentColor, onClick = { onColorChanged(color) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}
