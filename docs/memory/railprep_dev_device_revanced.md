---
name: Dev device (OPPO CPH2491) has Revanced GmsCore alongside real Play Services
description: Non-obvious cause of YouTube IFrame error 152 / UNKNOWN on the primary test device; production users on clean devices are unaffected
type: project
originSessionId: 14001d42-4e8c-4462-9af2-e53ccd128965
---
The OPPO CPH2491 ColorOS dev device (serial ZX9HQCAYO785NVBI) has both `app.revanced.android.gms` (Revanced's patched GmsCore) AND the real `com.google.android.gms` installed. Confirm with `adb shell pm list packages | grep revanced`.

**Why this matters:** Revanced's GmsCore returns patched attestation for the Android WebView Media Integrity API. YouTube's IFrame Player rejects embeds with **error 152** ("video player configuration") when it sees this attestation mismatch — even for known-always-embeddable video IDs. The MIT `android-youtube-player` library (12.1.2) hits the same issue and surfaces it as `onError=UNKNOWN` (its mapping for any raw IFrame error code it doesn't know).

**How to apply:** When YT embeds fail on this device but the code looks correct, don't chase it as an app bug. The working combination landed in Phase 2.5 (`feature/feature-learn/.../youtube/YouTubePlayer.kt`):

- Plain WebView loading YT IFrame Player API JS — library dependency removed.
- `mediaPlaybackRequiresUserGesture = false` — required for YT to paint the thumbnail/play-button overlay. WebView gesture policy doesn't matter because `playerVars.autoplay = 0` keeps real playback gesture-gated.
- `baseUrl = "https://railprep.app/"` — a neutral origin. `baseUrl = youtube.com` triggers YT's anti-impersonation check on this device and returns 152.
- `playerVars.origin` must match the baseUrl (`https://railprep.app`), else the postMessage handshake logs a cross-origin warning and state transitions stop.
- Defence in depth: `androidx.webkit` 1.15.0 + `WebSettingsCompat.setWebViewMediaIntegrityApiStatus(WEBVIEW_MEDIA_INTEGRITY_API_DISABLED)`.
- ToS: default IFrame controls + YouTube branding stay visible; `autoplay=0` keeps no-autoplay end-to-end.

Verified signal: logcat `-s RailPrepVideo` shows `yt:state=-1 → yt:state=3 → yt:state=1 (PLAYING)` on tap. Screenshot size jumps from ~75 KB (black) to ~950 KB (real frames) is the cheap visual check.

Production users on clean devices were never affected. Don't use this device's behaviour as the canonical production test — it over-reports failures for anything WebView-Media-Integrity-gated.
