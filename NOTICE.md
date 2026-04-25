# Third-party notices

This project bundles the following third-party assets. Copies of each license are in `licenses/`.

## Fonts (bundled under `core/core-design/src/main/res/font/`)

| Font                    | File                                | License                                                                       | Upstream                                                |
|-------------------------|-------------------------------------|-------------------------------------------------------------------------------|---------------------------------------------------------|
| Inter (variable)        | `inter.ttf`                         | [SIL Open Font License 1.1](licenses/OFL-Inter.txt)                           | https://github.com/google/fonts/tree/main/ofl/inter     |
| Plus Jakarta Sans (var) | `plus_jakarta_sans.ttf`             | [SIL Open Font License 1.1](licenses/OFL-PlusJakartaSans.txt)                 | https://github.com/google/fonts/tree/main/ofl/plusjakartasans |

Both fonts are redistributed under SIL OFL 1.1. They are bundled as fallbacks so Paparazzi JVM snapshot tests can render without hitting the Google Fonts provider. Production runs prefer the Google Fonts downloadable provider and fall through to bundled if the provider is unavailable. **Noto Sans Devanagari** is Downloadable-only in Phase 1 (see README's deferred list).

## Fonts (Downloadable at runtime)

The app requests these via the Google Fonts provider (`com.google.android.gms.fonts`). They are **not** bundled and have no NOTICE entry — the provider ships its own licensing context.

- Inter (also Downloadable as a primary source)
- Plus Jakarta Sans (also Downloadable as a primary source)
- Noto Sans Devanagari

## Google Fonts provider certs

The cert array in `core/core-design/src/main/res/values/font_certs.xml` is taken verbatim from the official [Android Compose Samples](https://github.com/android/compose-samples) repository (Apache 2.0).

## Content attribution (Phase 2 onward)

RailPrep is a **catalog-only** discovery app. It hosts no media. Every video, PDF, and article is served from its original source. Every on-device topic shows a source + license line at the bottom of the detail screen. See `docs/content-strategy.md §5` for the full licensing register.

### Sources

| Source | How we use it | License |
|---|---|---|
| **NCERT** (`ncert.nic.in`) | Topic PDFs linked directly; never rehosted or cached server-side. Android app downloads the PDF to the app's private cache at view time for rendering with `PdfRenderer`. | NCERT copyright; redistribution prohibited. Linking + end-user personal-study download is permitted. |
| **YouTube videos** | Embedded via the official YouTube IFrame Player API (`youtube.com/iframe_api`) inside a WebView. Controls + YouTube branding always visible. No autoplay (WebView user-gesture policy). | YouTube Standard License per creator; IFrame Developer Policies: [link](https://developers.google.com/youtube/terms/developer-policies). |
| **PIB** (Press Information Bureau, Phase 4) | RSS ingestion. Each article displays the full GODL-India attribution line verbatim. | [GODL-India](https://www.data.gov.in/Godl) via NDSAP inheritance. |
| **data.gov.in** (Phase 4+) | Link-only for stats cited in GA; no dataset redistribution. | [GODL-India](https://www.data.gov.in/Godl). |

### Channel whitelist

The full list of YouTube channels from which we discover content (and the tier-based rules on what gets auto-featured on Home) is maintained in `tools/content-pipeline/channels.json`. Channels in `docs/content-strategy.md §1` with "Flag = Red" (primary purpose = paid-funnel funnel, e.g. Unacademy main, Testbook main, BYJU'S Exam Prep main) are **excluded**.

### Android app libraries (Phase 2 additions)

| Library | Version | License | Upstream |
|---|---|---|---|
| `okhttp` (for PDF downloads) | 4.12.0 | Apache 2.0 | https://github.com/square/okhttp |
| `coil-compose` | 2.7.0 | Apache 2.0 | https://github.com/coil-kt/coil |

YouTube playback uses the device's system WebView pointing at the YouTube IFrame Player API — no third-party wrapper library is bundled.
