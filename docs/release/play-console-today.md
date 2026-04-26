# RailPrep Same-Day Play Console Checklist

## Build

- Package name: `com.railprep`
- Version code: `1`
- Version name: `1.0.0`
- Target SDK: `35`
- AAB path: `app/build/outputs/bundle/prodRelease/app-prod-release.aab`
- Local upload key path: `C:\Users\VASU\.railprep\release\railprep-upload.jks`
- Upload key alias: `railprep_upload`
- Upload key SHA-1: `0E:79:E8:1B:E1:B6:6A:C5:30:76:9E:97:C3:0B:2A:22:EC:D6:65:99`
- Upload key SHA-256: `21:2A:DC:8A:B2:28:8D:CF:43:B2:25:5B:CD:7F:5A:FC:A4:77:20:60:9F:2A:06:8A:16:95:93:A9:5B:EB:CF:FB`

## Required Console Fields

- App name: RailPrep
- Default language: English (India)
- App or game: App
- Free or paid: Free
- Category: Education
- Contact email: railprep.support@gmail.com
- Privacy policy: https://abhishek9871.github.io/railprep/privacy.html
- Terms: https://abhishek9871.github.io/railprep/terms.html

## Declarations

- Ads: No.
- App access: All features require account sign-in, but use the provided test credentials or Google account for review.
- Target audience: 13+ / students and adults preparing for exams.
- News app: No.
- COVID/contact tracing: No.
- Data Safety: use `docs/play-store/data-safety.md`.

## Products

If enabling subscriptions today:

- `rrb_ntpc_pro_monthly`
- `rrb_ntpc_pro_quarterly`

If subscriptions are not fully reviewed in Play Console today, keep the first internal release free and test billing in a later build.

## Manual Gates Before Upload

1. Enable GitHub Pages from this repo: Settings > Pages > Deploy from branch > `main` / `docs`.
2. Confirm these URLs load:
   - https://abhishek9871.github.io/railprep/privacy.html
   - https://abhishek9871.github.io/railprep/terms.html
3. In Google Play Console, create app with package `com.railprep`.
4. Upload `app-prod-release.aab` to Internal testing.
5. After upload, open Play Console > Setup > App integrity and copy the Play app signing certificate SHA-1.
6. In Google Cloud Console, add/register Android OAuth client fingerprints for package `com.railprep`:
   - the upload-key SHA-1 above for local release installs
   - the Play app-signing SHA-1 for Play-distributed installs
7. Add tester email addresses.
8. Install from the internal testing link and smoke test sign-in, Home, Learn PDF, Tests, PYQ PDF, Saved Questions, Privacy & account.
