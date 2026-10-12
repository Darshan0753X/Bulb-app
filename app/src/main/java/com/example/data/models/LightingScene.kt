package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.protocol.LightingEffect

data class LightingScene(
    val id: String,
    val name: String,
    val description: String,
    val color: Color,
    val brightness: Int,
    val tempKelvin: Int = 4000,
    val hardwareEffect: LightingEffect? = null,
    val isCustom: Boolean = false
)

object DefaultScenes {
    val presetScenes = listOf(
        LightingScene(
            id = "sunset",
            name = "Sunset",
            description = "Warm golden hour amber glow",
            color = Color(0xFFFF7043),
            brightness = 85,
            tempKelvin = 2700
        ),
        LightingScene(
            id = "ocean",
            name = "Ocean",
            description = "Deep marine blue & cyan breeze",
            color = Color(0xFF00B0FF),
            brightness = 75,
            tempKelvin = 5000
        ),
        LightingScene(
            id = "forest",
            name = "Forest",
            description = "Lush emerald calm ambience",
            color = Color(0xFF00E676),
            brightness = 70,
            tempKelvin = 4000
        ),
        LightingScene(
            id = "galaxy",
            name = "Galaxy",
            description = "Mystic nebula violet and indigo",
            color = Color(0xFFB388FF),
            brightness = 90,
            tempKelvin = 6000
        ),
        LightingScene(
            id = "candle",
            name = "Candle",
            description = "Soft organic warm candlelight",
            color = Color(0xFFFFB74D),
            brightness = 40,
            tempKelvin = 2700,
            hardwareEffect = LightingEffect.CANDLE
        ),
        LightingScene(
            id = "party",
            name = "Party",
            description = "Dynamic vibrant energy rhythm",
            color = Color(0xFFFF4081),
            brightness = 100,
            tempKelvin = 6500,
            hardwareEffect = LightingEffect.STROBE
        ),
        LightingScene(
            id = "arctic",
            name = "Arctic",
            description = "Crisp glacial blue daylight",
            color = Color(0xFFE0F7FA),
            brightness = 80,
            tempKelvin = 6500
        ),
        LightingScene(
            id = "study",
            name = "Study Mode",
            description = "Focused comfortable neutral light",
            color = Color(0xFFFFF9C4),
            brightness = 90,
            tempKelvin = 4500
        ),
        LightingScene(
            id = "gaming",
            name = "Gaming Mode",
            description = "Cyberpunk neon cyan & high contrast",
            color = Color(0xFF00E5FF),
            brightness = 100,
            tempKelvin = 6000
        ),
        LightingScene(
            id = "chill",
            name = "Chill Mode",
            description = "Relaxing warm ambient sunset",
            color = Color(0xFFFFAB40),
            brightness = 45,
            tempKelvin = 3000
        ),
        LightingScene(
            id = "night",
            name = "Night Mode",
            description = "Ultra dim amber sleep glow",
            color = Color(0xFFFF6D00),
            brightness = 10,
            tempKelvin = 2700
        )
    )
}
