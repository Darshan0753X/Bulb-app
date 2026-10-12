package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.theme.BorderDark
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ColorWheelPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .background(SurfaceDark)
            .border(1.dp, BorderDark, CircleShape)
            .testTag("color_wheel_picker")
    ) {
        val sizePx = constraints.maxWidth.toFloat()
        val radius = sizePx / 2f
        val center = Offset(radius, radius)

        // Helper to compute color from polar angle/distance
        fun colorFromOffset(offset: Offset): Color {
            val dx = offset.x - center.x
            val dy = offset.y - center.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtMost(radius)
            val angleRad = atan2(dy, dx)
            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
            if (angleDeg < 0) angleDeg += 360f

            val saturation = (dist / radius).coerceIn(0f, 1f)
            val hsv = floatArrayOf(angleDeg, saturation, 1.0f)
            val argb = android.graphics.Color.HSVToColor(hsv)
            return Color(argb)
        }

        Canvas(
            modifier = Modifier
                .size(240.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onColorSelected(colorFromOffset(offset))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onColorSelected(colorFromOffset(change.position))
                    }
                }
        ) {
            val sweepColors = listOf(
                Color.Red, Color.Yellow, Color.Green,
                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
            // Sweep Hue Gradient
            drawCircle(
                brush = Brush.sweepGradient(sweepColors, center),
                radius = radius,
                center = center
            )
            // Radial Saturation Fade (Center white)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            // Outer bezel ring
            drawCircle(
                color = BorderDark,
                radius = radius - 1f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun ColorControlSection(
    currentColor: Color,
    isPending: Boolean,
    onColorChanged: (Color) -> Unit,
    onApplyColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceElevatedDark.copy(alpha = 0.7f))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RGB COLOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MutedGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isPending) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Text(
                        text = "UNAPPLIED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        ColorWheelPicker(
            selectedColor = currentColor,
            onColorSelected = onColorChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Color Preview & RGB readout
        val r = (currentColor.red * 255).roundToInt()
        val g = (currentColor.green * 255).roundToInt()
        val b = (currentColor.blue * 255).roundToInt()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "R: $r   G: $g   B: $b",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = String.format("#%02X%02X%02X", r, g, b),
                    fontSize = 11.sp,
                    color = MutedGray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            ElevatedButton(
                onClick = onApplyColor,
                enabled = isPending,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceDark,
                    disabledContentColor = MutedGray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("apply_color_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
