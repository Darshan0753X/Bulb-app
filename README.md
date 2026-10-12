# Smitch Local Controller

A native Android 15-compatible application designed to control the **Smitch RGB CCT Smart Bulb v2.0 (SB161001)** locally without requiring the original proprietary application or cloud accounts.

---

## 1. Architecture & Protocol Implementation

### Command Codec
All protocol bodies are cleanly separated from TCP transport framing:

| Feature | Documented Command Body | Framing Strategy | Evidence Status |
| :--- | :--- | :--- | :--- |
| **Status Query** | `0001xx03` | `\r` (configurable) | Confirmed in bytecode |
| **Power ON** | `0001xx09xxx1` | `\r` (configurable) | Confirmed in bytecode |
| **Power OFF** | `0001xx09xxx0` | `\r` (configurable) | Confirmed in bytecode |
| **RGB Color** | `0001xx01` + `RRR` + `GGG` + `BBB` | `\r` (configurable) | Confirmed in bytecode |
| **RGB 6500K Special** | `0001xx106500` (RGB CCT 255,255,255) | `\r` (configurable) | Bytecode observation |
| **Brightness (RGB CCT)** | `0001xx07` + `005-100` | `\r` (configurable) | Bytecode observation |
| **Brightness (White Bulb)**| `0001xx02` + `005-100` | `\r` (configurable) | Bytecode observation |
| **Color Temperature** | `0001xx10` + `2700-6500` (decimal) | `\r` (configurable) | Bytecode observation |
| **Strobe Effect** | `0001xx041` (On) / `0001xx040` (Off) | `\r` (configurable) | Bytecode observation |
| **Random Effect** | `0001xx051` (On) / `0001xx050` (Off) | `\r` (configurable) | Bytecode observation |
| **Candle Effect** | `0001xx061` (On) / `0001xx060` (Off) | `\r` (configurable) | Bytecode observation |

### Status Parser
Tolerantly parses JSON responses returned by `0001xx03`. Handles missing keys, unmarshals values safely (`r`, `g`, `b`, `brightness`, `temp`, `powerState`, `flash`, `rainbow`, `candle`), and isolates unknown fields for diagnostics.

---

## 2. Interactive Bulb Stage UI & UX

- **Interactive Draggable Bulb**: The bulb can be dragged anywhere across the 3D-styled stage with soft bloom, glass specular reflections, and ambient floor lighting.
- **Visual Isolation**: Dragging the bulb updates local visual positions only without triggering network commands.
- **Preview vs Confirmed State**: Selected RGB colors, brightness, and temperature provide instantaneous visual feedback while displaying an **UNAPPLIED** badge until explicitly confirmed via the **Apply** button.
- **Power Switch**: Illuminated ambient power control with animated dark/lit bulb transitions.

---

## 3. Connection Modes

1. **Mode A: Bulb Hotspot Mode**
   - Connect phone Wi-Fi to bulb AP: `Smitch_v1ab`
   - Default IP/Port: `192.168.4.1:80`
2. **Mode B: Home Wi-Fi LAN Mode**
   - Discovers bulb on 2.4 GHz network using Android `NsdManager` (`_http._tcp.`)
   - Allows manual IP/port overrides for custom router assignments.
   - Includes an experimental LAN control switch.

---

## 4. Diagnostics & Safety

- **No Destructive Commands**: Factory reset (`0000xx07`) and firmware update (`0000xx08`) are strictly prohibited and not implemented.
- **Framing Sandbox**: Diagnostic tab enables testing single carriage return (`\r`), double carriage return (`\r\r`), CRLF, or raw bytes without crashing.
- **Live Socket Trace**: Inspect write results, response payload bytes, and socket error messages in real-time.

---

## 5. Building & Running

### Debug APK Build
```bash
gradle :app:assembleDebug
```
The output APK will be placed at `app/build/outputs/apk/debug/app-debug.apk`.

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest
```
