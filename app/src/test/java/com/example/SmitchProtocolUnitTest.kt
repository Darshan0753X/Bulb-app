package com.example

import com.example.protocol.BulbModel
import com.example.protocol.CommandCodec
import com.example.protocol.LightingEffect
import com.example.protocol.StatusParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmitchProtocolUnitTest {

    @Test
    fun testRgbCommandEncoding() {
        // RGB 0, 0, 255 -> "0001xx01000000255"
        val blueCmd = CommandCodec.buildRgbCommand(0, 0, 255)
        assertEquals("0001xx01000000255", blueCmd)

        // RGB 0, 255, 0 -> "0001xx01000255000"
        val greenCmd = CommandCodec.buildRgbCommand(0, 255, 0)
        assertEquals("0001xx01000255000", greenCmd)

        // RGB 255, 70, 0 -> "0001xx01255070000"
        val orangeCmd = CommandCodec.buildRgbCommand(255, 70, 0)
        assertEquals("0001xx01255070000", orangeCmd)
    }

    @Test
    fun testRgbClampingAndWhiteSpecialCase() {
        // Values outside 0..255 should clamp safely
        val clampedCmd = CommandCodec.buildRgbCommand(-10, 300, 12)
        assertEquals("0001xx01000255012", clampedCmd)

        // Special case for RGB CCT Bulb when enabled
        val whiteSpecial = CommandCodec.buildRgbCommand(
            r = 255, g = 255, b = 255,
            model = BulbModel.RGB_CCT,
            enableWhiteSpecialCase = true
        )
        assertEquals("0001xx106500", whiteSpecial)
    }

    @Test
    fun testBrightnessCommandEncoding() {
        // Default models use prefix 0001xx07
        assertEquals("0001xx07080", CommandCodec.buildBrightnessCommand(80, BulbModel.RGB_CCT))

        // Clamping below 5
        assertEquals("0001xx07005", CommandCodec.buildBrightnessCommand(2, BulbModel.RGB_CCT))

        // White Bulb models use prefix 0001xx02
        assertEquals("0001xx02050", CommandCodec.buildBrightnessCommand(50, BulbModel.WHITE_BULB))
        assertEquals("0001xx02090", CommandCodec.buildBrightnessCommand(90, BulbModel.WHITE_BULB_SC))
    }

    @Test
    fun testColorTemperatureCommandEncoding() {
        // Default model uses prefix 0001xx10
        assertEquals("0001xx102700", CommandCodec.buildColorTemperatureCommand(2700, BulbModel.RGB_CCT))
        assertEquals("0001xx104000", CommandCodec.buildColorTemperatureCommand(4000, BulbModel.RGB_CCT))
        assertEquals("0001xx106500", CommandCodec.buildColorTemperatureCommand(6500, BulbModel.RGB_CCT))

        // Clamping bounds 2700..6500
        assertEquals("0001xx102700", CommandCodec.buildColorTemperatureCommand(1000, BulbModel.RGB_CCT))
        assertEquals("0001xx106500", CommandCodec.buildColorTemperatureCommand(8000, BulbModel.RGB_CCT))

        // White Bulb uses prefix 0001xx01
        assertEquals("0001xx013000", CommandCodec.buildColorTemperatureCommand(3000, BulbModel.WHITE_BULB))
    }

    @Test
    fun testPowerCommands() {
        assertEquals("0001xx09xxx1", CommandCodec.buildPowerCommand(true))
        assertEquals("0001xx09xxx0", CommandCodec.buildPowerCommand(false))
    }

    @Test
    fun testEffectCommands() {
        assertEquals("0001xx041", CommandCodec.buildEffectCommand(LightingEffect.STROBE, true))
        assertEquals("0001xx040", CommandCodec.buildEffectCommand(LightingEffect.STROBE, false))

        assertEquals("0001xx051", CommandCodec.buildEffectCommand(LightingEffect.RANDOM, true))
        assertEquals("0001xx050", CommandCodec.buildEffectCommand(LightingEffect.RANDOM, false))

        assertEquals("0001xx061", CommandCodec.buildEffectCommand(LightingEffect.CANDLE, true))
        assertEquals("0001xx060", CommandCodec.buildEffectCommand(LightingEffect.CANDLE, false))
    }

    @Test
    fun testStatusParserValidJson() {
        val json = """
            {
                "mac_address": "84:F3:EB:12:34:56",
                "type": "bulb",
                "hw_model": "SB161001",
                "model": "RGB CCT Bulb",
                "status": "online",
                "values": {
                    "powerState": 1,
                    "r": 255,
                    "g": 100,
                    "b": 50,
                    "brightness": 85,
                    "temp": 4500,
                    "candle": false,
                    "flash": true,
                    "rainbow": false,
                    "custom_future_flag": 123
                }
            }
        """.trimIndent()

        val result = StatusParser.parse(json)
        assertTrue(result.isSuccess)

        val status = result.getOrNull()
        assertNotNull(status)
        assertEquals("84:F3:EB:12:34:56", status?.macAddress)
        assertEquals("SB161001", status?.hwModel)
        assertEquals("RGB CCT Bulb", status?.model)
        assertEquals(true, status?.powerState)
        assertEquals(255, status?.r)
        assertEquals(100, status?.g)
        assertEquals(50, status?.b)
        assertEquals(85, status?.brightness)
        assertEquals(4500, status?.tempKelvin)
        assertEquals(false, status?.candleActive)
        assertEquals(true, status?.flashActive)
        assertEquals(false, status?.rainbowActive)
        assertEquals(123, status?.unknownFields?.get("custom_future_flag"))
    }

    @Test
    fun testStatusParserTolerantHandling() {
        // Empty response
        val emptyResult = StatusParser.parse("")
        assertTrue(emptyResult.isFailure)

        // Malformed response
        val malformedResult = StatusParser.parse("Not JSON at all")
        assertTrue(malformedResult.isFailure)

        // Missing fields JSON
        val sparseJson = """{"status": "ok"}"""
        val sparseResult = StatusParser.parse(sparseJson)
        assertTrue(sparseResult.isSuccess)
        assertNull(sparseResult.getOrNull()?.powerState)
        assertNull(sparseResult.getOrNull()?.brightness)
    }
}
