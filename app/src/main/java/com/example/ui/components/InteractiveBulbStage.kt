package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.MutedGray
import com.example.ui.theme.SurfaceDark
import kotlin.math.roundToInt

@Composable
fun InteractiveBulbStage(
    bulbColor: Color,
    isPoweredOn: Boolean,
    brightness: Int,
    modifier: Modifier = Modifier,
    onResetPosition: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        if (isPoweredOn) bulbColor.copy(alpha = 0.22f * (brightness / 100f)) else Color.Transparent,
                        SurfaceDark.copy(alpha = 0.95f),
                        Color(0xFF090C12)
                    ),
                    radius = 900f
                )
            )
            .border(1.dp, BorderDark.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
    ) {
        val stageWidthPx = constraints.maxWidth.toFloat()
        val stageHeightPx = constraints.maxHeight.toFloat()

        val bulbSizeDp = 130.dp
        val density = androidx.compose.ui.platform.LocalDensity.current
        val bulbSizePx = with(density) { bulbSizeDp.toPx() }

        // Initial center coordinates
        val defaultX = (stageWidthPx - bulbSizePx) / 2f
        val defaultY = (stageHeightPx - bulbSizePx) / 2f

        var offsetX by remember { mutableFloatStateOf(defaultX) }
        var offsetY by remember { mutableFloatStateOf(defaultY) }
        var isDragging by remember { mutableStateOf(false) }

        // Recalculate default if constraints change from 0
        LaunchedEffect(stageWidthPx, stageHeightPx) {
            if (stageWidthPx > 0 && stageHeightPx > 0) {
                if (offsetX == 0f && offsetY == 0f) {
                    offsetX = (stageWidthPx - bulbSizePx) / 2f
                    offsetY = (stageHeightPx - bulbSizePx) / 2f
                }
            }
        }

        // Ambient floor reflection
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isPoweredOn && stageHeightPx > 0) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            bulbColor.copy(alpha = 0.25f * (brightness / 100f)),
                            Color.Transparent
                        ),
                        center = Offset(offsetX + bulbSizePx / 2f, stageHeightPx - 25f),
                        radius = 200f
                    ),
                    topLeft = Offset(offsetX + bulbSizePx / 2f - 180f, stageHeightPx - 50f),
                    size = Size(360f, 60f)
                )
            }
        }

        // Draggable Glowing Bulb Component
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(bulbSizeDp)
                .testTag("interactive_bulb_item")
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val maxX = (stageWidthPx - bulbSizePx).coerceAtLeast(0f)
                            val maxY = (stageHeightPx - bulbSizePx).coerceAtLeast(0f)
                            offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                            offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                        }
                    )
                }
        ) {
            SmartBulbIllustration(
                color = bulbColor,
                isPoweredOn = isPoweredOn,
                brightness = brightness,
                isDragging = isDragging
            )
        }

        // Reset Position & Stage Status Indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SurfaceDark.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        offsetX = (stageWidthPx - bulbSizePx) / 2f
                        offsetY = (stageHeightPx - bulbSizePx) / 2f
                        onResetPosition()
                    },
                    modifier = Modifier.testTag("reset_bulb_position_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Bulb Position",
                        tint = MutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Stage Drag Hint
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(
                text = if (isDragging) "DRAGGING BULB" else "INTERACTIVE BULB STAGE",
                color = MutedGray.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
fun SmartBulbIllustration(
    color: Color,
    isPoweredOn: Boolean,
    brightness: Int,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bulbBreathing")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val scaleTarget = if (isDragging) 1.08f else 1.0f
    val scaleAnim = remember { Animatable(1.0f) }

    LaunchedEffect(isDragging) {
        scaleAnim.animateTo(scaleTarget, tween(200, easing = FastOutSlowInEasing))
    }

    val glowColor = if (isPoweredOn) {
        color.copy(alpha = (0.55f * (brightness / 100f) * breathingAlpha).coerceIn(0f, 1f))
    } else {
        Color.Transparent
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width * scaleAnim.value
        val h = size.height * scaleAnim.value
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Outer ambient glow (Bloom effect)
        if (isPoweredOn) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        glowColor.copy(alpha = glowColor.alpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy - h * 0.1f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(cx, cy - h * 0.1f)
            )
        }

        // Bulb Glass Body Path
        val domeRadius = w * 0.32f
        val domeCenter = Offset(cx, cy - h * 0.08f)

        // Glass Outer Shell
        drawCircle(
            color = if (isPoweredOn) {
                color.copy(alpha = (0.35f * (brightness / 100f)).coerceAtLeast(0.15f))
            } else {
                Color(0xFF1E2634)
            },
            radius = domeRadius,
            center = domeCenter
        )

        // Glass Rim Highlight / Border
        drawCircle(
            color = if (isPoweredOn) {
                color.copy(alpha = 0.8f)
            } else {
                Color(0xFF334155)
            },
            radius = domeRadius,
            center = domeCenter,
            style = Stroke(width = 3.dp.toPx())
        )

        // Inner Light Emitting Core (Filament / LED Capsule)
        if (isPoweredOn) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        color,
                        Color.Transparent
                    ),
                    center = domeCenter,
                    radius = domeRadius * 0.65f
                ),
                radius = domeRadius * 0.65f,
                center = domeCenter
            )
        } else {
            // Dark inactive filament
            drawCircle(
                color = Color(0xFF121822),
                radius = domeRadius * 0.45f,
                center = domeCenter
            )
        }

        // Glass Curvature Specular Reflection
        val highlightPath = Path().apply {
            moveTo(cx - domeRadius * 0.6f, domeCenter.y - domeRadius * 0.6f)
            cubicTo(
                cx - domeRadius * 0.8f, domeCenter.y - domeRadius * 0.2f,
                cx - domeRadius * 0.7f, domeCenter.y + domeRadius * 0.2f,
                cx - domeRadius * 0.4f, domeCenter.y + domeRadius * 0.5f
            )
        }
        drawPath(
            path = highlightPath,
            color = Color.White.copy(alpha = if (isPoweredOn) 0.5f else 0.15f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Metallic Neck & Screw Base
        val neckTop = domeCenter.y + domeRadius * 0.75f
        val neckWidth = w * 0.28f
        val baseHeight = h * 0.25f

        // Metal Neck
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(cx - neckWidth / 2f, neckTop),
            size = Size(neckWidth, baseHeight * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )

        // Screw Threads (ribs)
        val threadColor = Color(0xFF64748B)
        for (i in 0..2) {
            val ty = neckTop + baseHeight * 0.35f + (i * 6.dp.toPx())
            drawRoundRect(
                color = threadColor,
                topLeft = Offset(cx - (neckWidth * 0.9f) / 2f, ty),
                size = Size(neckWidth * 0.9f, 3.5.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }

        // Base Contact Tip
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(cx - neckWidth * 0.35f, neckTop + baseHeight * 0.75f),
            size = Size(neckWidth * 0.7f, baseHeight * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
    }
}
