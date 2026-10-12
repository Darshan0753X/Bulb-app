package com.example.protocol

import org.json.JSONObject

/**
 * Tolerant parsed representation of the bulb's reported status.
 * Distinguishes between confirmed device values and unknown/missing data.
 */
data class BulbStatus(
    val rawJson: String,
    val macAddress: String? = null,
    val type: String? = null,
    val hwModel: String? = null,
    val hardwareVersion: String? = null,
    val model: String? = null,
    val status: String? = null,
    val powerState: Boolean? = null,
    val r: Int? = null,
    val g: Int? = null,
    val b: Int? = null,
    val brightness: Int? = null,
    val tempKelvin: Int? = null,
    val candleActive: Boolean? = null,
    val flashActive: Boolean? = null,
    val rainbowActive: Boolean? = null,
    val unknownFields: Map<String, Any?> = emptyMap()
)

object StatusParser {

    /**
     * Tolerantly parses JSON returned by status query "0001xx03".
     * Returns null if response cannot be parsed as JSON, preserving error details safely.
     */
    fun parse(rawResponse: String): Result<BulbStatus> {
        val trimmed = rawResponse.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Status response is empty"))
        }

        return try {
            val json = JSONObject(trimmed)
            val mac = json.optString("mac_address").takeIf { it.isNotEmpty() }
            val type = json.optString("type").takeIf { it.isNotEmpty() }
            val hwModel = json.optString("hw_model").takeIf { it.isNotEmpty() }
            val hwVer = json.optString("hardware_version").takeIf { it.isNotEmpty() }
            val model = json.optString("model").takeIf { it.isNotEmpty() }
            val status = json.optString("status").takeIf { it.isNotEmpty() }

            val valuesObj = json.optJSONObject("values")

            var power: Boolean? = null
            var red: Int? = null
            var green: Int? = null
            var blue: Int? = null
            var bright: Int? = null
            var temp: Int? = null
            var candle: Boolean? = null
            var flash: Boolean? = null
            var rainbow: Boolean? = null

            val unknowns = mutableMapOf<String, Any?>()

            if (valuesObj != null) {
                if (valuesObj.has("powerState")) {
                    power = when (val p = valuesObj.get("powerState")) {
                        is Boolean -> p
                        is Int -> p == 1
                        is String -> p.equals("1", ignoreCase = true) || p.equals("true", ignoreCase = true) || p.equals("on", ignoreCase = true)
                        else -> null
                    }
                }
                if (valuesObj.has("r")) red = valuesObj.optInt("r", -1).takeIf { it in 0..255 }
                if (valuesObj.has("g")) green = valuesObj.optInt("g", -1).takeIf { it in 0..255 }
                if (valuesObj.has("b")) blue = valuesObj.optInt("b", -1).takeIf { it in 0..255 }
                if (valuesObj.has("brightness")) bright = valuesObj.optInt("brightness", -1).takeIf { it >= 0 }
                if (valuesObj.has("temp")) temp = valuesObj.optInt("temp", -1).takeIf { it >= 0 }

                if (valuesObj.has("candle")) candle = valuesObj.optBoolean("candle")
                if (valuesObj.has("flash")) flash = valuesObj.optBoolean("flash")
                if (valuesObj.has("rainbow")) rainbow = valuesObj.optBoolean("rainbow")

                val keys = valuesObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (k !in listOf("powerState", "r", "g", "b", "brightness", "temp", "candle", "flash", "rainbow")) {
                        unknowns[k] = valuesObj.opt(k)
                    }
                }
            }

            // Top-level unknown keys
            val topKeys = json.keys()
            while (topKeys.hasNext()) {
                val k = topKeys.next()
                if (k !in listOf("mac_address", "type", "hw_model", "hardware_version", "model", "status", "values")) {
                    unknowns[k] = json.opt(k)
                }
            }

            Result.success(
                BulbStatus(
                    rawJson = trimmed,
                    macAddress = mac,
                    type = type,
                    hwModel = hwModel,
                    hardwareVersion = hwVer,
                    model = model,
                    status = status,
                    powerState = power,
                    r = red,
                    g = green,
                    b = blue,
                    brightness = bright,
                    tempKelvin = temp,
                    candleActive = candle,
                    flashActive = flash,
                    rainbowActive = rainbow,
                    unknownFields = unknowns
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
