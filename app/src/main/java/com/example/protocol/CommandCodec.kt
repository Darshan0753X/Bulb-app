package com.example.protocol

/**
 * Model variants supported by Smitch firmware and bytecode analysis.
 */
enum class BulbModel(val displayName: String) {
    RGB_CCT("RGB CCT Bulb"),
    WHITE_BULB("White Bulb"),
    WHITE_BULB_SC("White Bulb SC"),
    GENERIC("Generic Smitch Bulb")
}

/**
 * Lighting effect identifiers.
 */
enum class LightingEffect(val displayName: String, val enableCode: String, val disableCode: String) {
    STROBE("Strobe", "0001xx041", "0001xx040"),
    RANDOM("Random", "0001xx051", "0001xx050"),
    CANDLE("Candle", "0001xx061", "0001xx060")
}

/**
 * Framing strategy for TCP transport.
 */
enum class FramingStrategy(val displayName: String, val suffix: String) {
    SINGLE_CR("Single Carriage Return (\\r)", "\r"),
    DOUBLE_CR("Double Carriage Return (\\r\\r)", "\r\r"),
    CR_LF("CRLF (\\r\\n)", "\r\n"),
    NO_TERMINATOR("None (Raw)", "")
}

/**
 * Encodes light control commands according to bytecode analysis of the Smitch application.
 * Separation of concerns: Builds command bodies only. Transport applies framing.
 */
object CommandCodec {

    const val STATUS_QUERY = "0001xx03"
    const val POWER_ON = "0001xx09xxx1"
    const val POWER_OFF = "0001xx09xxx0"

    /**
     * Builds RGB command body.
     * Format: "0001xx01" + RRR + GGG + BBB (each channel zero-padded 3-digit 000-255)
     * Special case: For "RGB CCT Bulb", if RGB is exactly 255,255,255 and model matches,
     * it encodes to "0001xx106500" (6500K white).
     */
    fun buildRgbCommand(
        r: Int,
        g: Int,
        b: Int,
        model: BulbModel = BulbModel.RGB_CCT,
        enableWhiteSpecialCase: Boolean = false
    ): String {
        val clampedR = r.coerceIn(0, 255)
        val clampedG = g.coerceIn(0, 255)
        val clampedB = b.coerceIn(0, 255)

        if (enableWhiteSpecialCase && model == BulbModel.RGB_CCT && clampedR == 255 && clampedG == 255 && clampedB == 255) {
            return "0001xx106500"
        }

        return String.format("0001xx01%03d%03d%03d", clampedR, clampedG, clampedB)
    }

    /**
     * Builds Brightness command body.
     * For "White Bulb" and "White Bulb SC": prefix "0001xx02" + 3-digit brightness.
     * For other models: prefix "0001xx07" + 3-digit brightness.
     * Values below 5 are clamped to 5 (005). Max clamped to 100 or 255 (default 100).
     */
    fun buildBrightnessCommand(
        brightness: Int,
        model: BulbModel = BulbModel.RGB_CCT,
        maxBrightness: Int = 100
    ): String {
        val clamped = brightness.coerceIn(5, maxBrightness.coerceAtLeast(5))
        val prefix = when (model) {
            BulbModel.WHITE_BULB, BulbModel.WHITE_BULB_SC -> "0001xx02"
            else -> "0001xx07"
        }
        return String.format("%s%03d", prefix, clamped)
    }

    /**
     * Builds Color Temperature command body.
     * For "White Bulb" and "White Bulb SC": prefix "0001xx01" + temperature in Kelvin.
     * For other models: prefix "0001xx10" + temperature in Kelvin.
     * Range: 2700K to 6500K. Value is decimal and NOT zero-padded.
     */
    fun buildColorTemperatureCommand(
        temperatureKelvin: Int,
        model: BulbModel = BulbModel.RGB_CCT
    ): String {
        val clamped = temperatureKelvin.coerceIn(2700, 6500)
        val prefix = when (model) {
            BulbModel.WHITE_BULB, BulbModel.WHITE_BULB_SC -> "0001xx01"
            else -> "0001xx10"
        }
        return "$prefix$clamped"
    }

    /**
     * Builds Power command body.
     * Power On: "0001xx09xxx1"
     * Power Off: "0001xx09xxx0"
     */
    fun buildPowerCommand(on: Boolean): String {
        return if (on) POWER_ON else POWER_OFF
    }

    /**
     * Builds Effect command body.
     */
    fun buildEffectCommand(effect: LightingEffect, enable: Boolean): String {
        return if (enable) effect.enableCode else effect.disableCode
    }

    /**
     * Builds Status query body: "0001xx03"
     */
    fun buildStatusQueryCommand(): String = STATUS_QUERY
}
