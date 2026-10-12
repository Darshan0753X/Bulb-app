package com.example.bridge

import org.json.JSONArray
import org.json.JSONObject

/**
 * Amazon Alexa Smart Home Skill Adapter.
 * Translates Alexa directives (PowerController, ColorController, BrightnessController, Discovery)
 * into standardized local bridge commands.
 */
object AlexaSmartHomeAdapter {

    fun generateDiscoveryPayload(): JSONObject {
        val root = JSONObject()
        val event = JSONObject()
        val header = JSONObject().apply {
            put("namespace", "Alexa.Discovery")
            put("name", "Discover.Response")
            put("payloadVersion", "3")
            put("messageId", java.util.UUID.randomUUID().toString())
        }
        val payload = JSONObject()
        val endpoints = JSONArray()

        val bulbEndpoint = JSONObject().apply {
            put("endpointId", "smitch_bulb_01")
            put("manufacturerName", "Smitch")
            put("friendlyName", "Smitch Bulb")
            put("description", "Smitch RGB CCT Smart Bulb v2.0")
            put("displayCategories", JSONArray(listOf("LIGHT")))

            val capabilities = JSONArray()
            // 1. Alexa Interface
            capabilities.put(JSONObject().apply {
                put("type", "AlexaInterface")
                put("interface", "Alexa")
                put("version", "3")
            })
            // 2. PowerController
            capabilities.put(JSONObject().apply {
                put("type", "AlexaInterface")
                put("interface", "Alexa.PowerController")
                put("version", "3")
                put("properties", JSONObject().apply {
                    put("supported", JSONArray().put(JSONObject().put("name", "powerState")))
                    put("proactivelyReported", false)
                    put("retrievable", true)
                })
            })
            // 3. BrightnessController
            capabilities.put(JSONObject().apply {
                put("type", "AlexaInterface")
                put("interface", "Alexa.BrightnessController")
                put("version", "3")
                put("properties", JSONObject().apply {
                    put("supported", JSONArray().put(JSONObject().put("name", "brightness")))
                    put("proactivelyReported", false)
                    put("retrievable", true)
                })
            })
            // 4. ColorController
            capabilities.put(JSONObject().apply {
                put("type", "AlexaInterface")
                put("interface", "Alexa.ColorController")
                put("version", "3")
                put("properties", JSONObject().apply {
                    put("supported", JSONArray().put(JSONObject().put("name", "color")))
                    put("proactivelyReported", false)
                    put("retrievable", true)
                })
            })
            // 5. ColorTemperatureController
            capabilities.put(JSONObject().apply {
                put("type", "AlexaInterface")
                put("interface", "Alexa.ColorTemperatureController")
                put("version", "3")
                put("properties", JSONObject().apply {
                    put("supported", JSONArray().put(JSONObject().put("name", "colorTemperatureInKelvin")))
                    put("proactivelyReported", false)
                    put("retrievable", true)
                })
            })

            put("capabilities", capabilities)
        }

        endpoints.put(bulbEndpoint)
        payload.put("endpoints", endpoints)
        event.put("header", header)
        event.put("payload", payload)
        root.put("event", event)
        return root
    }

    fun parseDirectiveToAction(directiveJson: JSONObject): String {
        val directive = directiveJson.optJSONObject("directive") ?: return ""
        val header = directive.optJSONObject("header") ?: return ""
        val namespace = header.optString("namespace")
        val name = header.optString("name")
        val payload = directive.optJSONObject("payload") ?: JSONObject()

        return when (namespace) {
            "Alexa.PowerController" -> {
                if (name == "TurnOn") """{"action":"turn_on"}""" else """{"action":"turn_off"}"""
            }
            "Alexa.BrightnessController" -> {
                val bright = payload.optInt("brightness", 80)
                """{"action":"set_brightness","brightness":$bright}"""
            }
            "Alexa.ColorController" -> {
                val colorObj = payload.optJSONObject("color") ?: JSONObject()
                val hue = colorObj.optDouble("hue", 180.0).toFloat()
                val sat = colorObj.optDouble("saturation", 1.0).toFloat()
                val bright = colorObj.optDouble("brightness", 1.0).toFloat()
                val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, bright))
                val r = android.graphics.Color.red(argb)
                val g = android.graphics.Color.green(argb)
                val b = android.graphics.Color.blue(argb)
                """{"action":"set_color_rgb","r":$r,"g":$g,"b":$b}"""
            }
            "Alexa.ColorTemperatureController" -> {
                val kelvin = payload.optInt("colorTemperatureInKelvin", 4000)
                """{"action":"set_color_temperature","kelvin":$kelvin}"""
            }
            else -> ""
        }
    }
}

/**
 * Google Home Cloud-to-Cloud Smart Home Adapter.
 * Handles SYNC, EXECUTE, and QUERY intents for action.devices.types.LIGHT.
 */
object GoogleHomeAdapter {

    fun generateSyncPayload(): JSONObject {
        val payload = JSONObject()
        payload.put("agentUserId", "user_smitch_default")

        val devices = JSONArray()
        val bulb = JSONObject().apply {
            put("id", "smitch_bulb_01")
            put("type", "action.devices.types.LIGHT")
            put("traits", JSONArray(listOf(
                "action.devices.traits.OnOff",
                "action.devices.traits.Brightness",
                "action.devices.traits.ColorSetting"
            )))
            put("name", JSONObject().apply {
                put("name", "Smitch Bulb")
                put("nicknames", JSONArray(listOf("Bulb", "Smart Bulb", "Smitch Light")))
            })
            put("willReportState", false)
            put("attributes", JSONObject().apply {
                put("colorModel", "rgb")
                put("colorTemperatureRange", JSONObject().apply {
                    put("temperatureMinK", 2700)
                    put("temperatureMaxK", 6500)
                })
            })
        }
        devices.put(bulb)
        payload.put("devices", devices)
        return payload
    }

    fun parseExecuteCommand(commandName: String, params: JSONObject): String {
        return when (commandName) {
            "action.devices.commands.OnOff" -> {
                val on = params.optBoolean("on", true)
                if (on) """{"action":"turn_on"}""" else """{"action":"turn_off"}"""
            }
            "action.devices.commands.BrightnessAbsolute" -> {
                val bright = params.optInt("brightness", 80)
                """{"action":"set_brightness","brightness":$bright}"""
            }
            "action.devices.commands.ColorAbsolute" -> {
                val colorObj = params.optJSONObject("color") ?: JSONObject()
                if (colorObj.has("spectrumRGB")) {
                    val rgbInt = colorObj.optInt("spectrumRGB")
                    val r = (rgbInt shr 16) and 0xFF
                    val g = (rgbInt shr 8) and 0xFF
                    val b = rgbInt and 0xFF
                    """{"action":"set_color_rgb","r":$r,"g":$g,"b":$b}"""
                } else if (colorObj.has("temperature")) {
                    val kelvin = colorObj.optInt("temperature", 4000)
                    """{"action":"set_color_temperature","kelvin":$kelvin}"""
                } else {
                    ""
                }
            }
            else -> ""
        }
    }
}
