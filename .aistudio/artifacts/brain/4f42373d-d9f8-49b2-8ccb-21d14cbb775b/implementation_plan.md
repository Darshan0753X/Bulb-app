# Implementation Plan: Floating Bulb Overlay & Voice Bridge (Alexa + Google Home)

## Overview
This plan integrates two advanced smart-home capabilities into the existing **Smitch Local Controller**:
1. **Persistent System Floating Bulb Overlay**: A transparent, draggable floating bulb that stays visible over other apps using Android 15 `TYPE_APPLICATION_OVERLAY`, reflecting live bulb color and power state, with a compact tap-to-expand control panel.
2. **Unified Voice Cloud Bridge (Android Phone Background Service)**: Connects your physical Smitch bulb to **Amazon Alexa** and **Google Home / Google Assistant** without requiring external PC hardware.

---

## 1. 🌌 Floating Bulb System Overlay (Android 15)

### System Overlay Window Architecture
- **Service**: `FloatingBulbOverlayService` extending `LifecycleService` / `ForegroundService`.
- **Foreground Type**: `android:foregroundServiceType="specialUse"` / `"connectedDevice"` with clear persistent status notification.
- **Permission Flow**: 
  - Checks `Settings.canDrawOverlays(context)`.
  - Prompts and redirects directly to `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` if not granted.
- **Window Specs**:
  - `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`
  - `FLAG_NOT_FOCUSABLE` (allows touches on underlying apps except directly on the bulb icon)
  - Pixel format: `PixelFormat.TRANSLUCENT`
  - **Truly Transparent Canvas**: Transparent view hierarchy with no square/card bounds. Only the bulb dome, filament, and subtle glow are drawn.

### Gesture & Drag Handling
- Touch listener tracking `ACTION_DOWN`, `ACTION_MOVE`, and `ACTION_UP`.
- Distinguishes click (< 10px delta, < 200ms) from drag.
- Clamps coordinates to display dimensions minus screen insets.
- Persists last `(x, y)` coordinates via `SharedPreferences`.

### Compact Floating Control Panel
- Tapping the floating bulb toggles a sleek, transparent glassmorphism popup:
  - Fast Power toggle button.
  - Quick-preset color chips (Warm White, Cool White, Amber, Cyan, Magenta, Green).
  - Slim Brightness slider.
  - "Open Full App" and "Hide Floating Bulb" actions.

---

## 2. 🎙️ Unified Voice Cloud Bridge (Alexa & Google Home)

### Bridge Architecture
```
[Amazon Alexa / Fire TV]      [Google Assistant / Google Home]
          \                             /
      (Smart Home Skill)          (Cloud-to-Cloud Action)
           \                           /
        [Secure Cloud Webhook Router (AWS Lambda / Cloud Run)]
                         |
           (Encrypted WebSocket / MQTT Relay)
                         |
       [Android Phone Background Bridge Service]
                         |
           (Existing Local TCP Controller)
                         |
           [Physical Smitch RGB CCT Bulb]
```

### Android Phone Bridge Service
- **`BulbVoiceBridgeService`**: Runs within the app on the phone connected to the DIGISOL 2.4 GHz Wi-Fi.
- Subscribes to the cloud bridge stream via secure lightweight WebSocket.
- When an Alexa or Google Home directive arrives:
  1. Directs command through `BulbRepository` (`setPower`, `setRgb`, `setBrightness`, `setColorTemperature`).
  2. Queries verified status.
  3. Returns acknowledgment report to the cloud gateway.

### Smart Home Adapters
1. **Amazon Alexa Adapter (`AlexaSmartHomeAdapter.kt` & Node/Python cloud schema)**:
   - `Alexa.Discovery`: Advertises `Smitch RGB CCT Bulb` (SB161001).
   - `Alexa.PowerController`: TurnOn / TurnOff.
   - `Alexa.BrightnessController`: SetBrightness / AdjustBrightness.
   - `Alexa.ColorController`: SetColor (Hue/Saturation mapped to RGB).
   - `Alexa.ColorTemperatureController`: SetColorTemperature (2700K–6500K).
2. **Google Home Cloud-to-Cloud Adapter (`GoogleHomeAdapter.kt`)**:
   - `action.devices.SYNC`: Exposes `action.devices.types.LIGHT` with traits `action.devices.traits.OnOff`, `action.devices.traits.Brightness`, `action.devices.traits.ColorSetting`.
   - `action.devices.EXECUTE`: Maps commands `action.devices.commands.OnOff`, `action.devices.commands.BrightnessAbsolute`, `action.devices.commands.ColorAbsolute`.
   - `action.devices.QUERY`: Reports verified hardware state.

---

## 3. UI Integration & Settings Screen
- Add a new dedicated **Integrations** card in `SettingsDiagnosticsScreen.kt`:
  - **Floating Bulb Switch**: Toggle with permission check and position reset.
  - **Alexa & Google Voice Bridge Switch**: Shows bridge status (`🟢 Connected to Cloud Gateway`, `🔴 Bridge Offline`), device ID, and setup guide.

---

## 4. Verification & Testing
1. Validate `compile_applet` succeeds.
2. Run unit tests (`:app:testDebugUnitTest`).
3. Verify Android 15 permissions and service manifests.
