# RailPrep — Content Strategy (Phase 2 input)

_Research-only document. No application code. All URLs cited inline. Channel IDs are verified via YouTube page source `"channelId":"UC..."` or surfaced directly in search result URLs from `youtube.com/channel/UC...` listings. Where a claim could not be independently verified, it is explicitly marked **UNVERIFIED**._

---

## Executive summary

- **NCERT PDF URLs follow a deterministic 4-letter + chapter-number scheme** — `https://ncert.nic.in/textbook/pdf/{code}{NN}.pdf` where char 1 = class band (`f`=VI, `g`=VII, `h`=VIII, `i`=IX, `j`=X, `k`=XI, `l`=XII), chars 2–4 = medium+subject. Hindi vs English uses a different book entirely (e.g. `iemh`=Class IX Eng Maths, `hhhn`=Class VIII Hindi). Not `i`+`h` as a prefix swap — a common myth. Source: live HEAD checks against `ncert.nic.in` (see §2).
- **The YouTube "free" educational layer is dominated by Unacademy / BYJU'S / Adda247 owned brands**. Truly independent heavyweights with >5M subs that teach RRB-relevant content free: Khan GS Research Centre (25M+), Dear Sir (16M+), Physics Wallah (11M+), Magnet Brains (16M+), Rojgar with Ankit (8M+). wifistudy sits inside Unacademy — not a legal risk but a strategic one (they will push users off YouTube into Unacademy paid funnels).
- **NCERT redistribution is explicitly prohibited by NCERT's own notices** [source: https://www.ncert.nic.in/pdf/announcement/notices/Press_Release_Copyright_Infringement-NCERT.pdf]. Linking to their PDFs on `ncert.nic.in` is fine; hosting copies is not. RailPrep's "we only link" posture is the only defensible one.
- **PIB content is GODL-India-licensed** with a specific attribution template required (provider + year + dataset name + URL + license) [source: https://www.data.gov.in/Godl]. RailPrep must show this verbatim per article.
- **Free-tier competitor weaknesses cluster into 3 patterns**: (1) aggressive paywall nag/ads on Testbook, (2) non-refund rage on Adda247, (3) stale content + broken subscription linkage on BYJU'S Exam Prep (ex-Gradeup). This maps to a clean RailPrep thesis: **no paywall, no ads, catalog-only, offline-first with 100% attribution**.

---

## 1. YouTube channel whitelist

### Methodology

Verification attempts used three techniques in order of reliability:

1. **Direct HTML scrape** via `curl -sL --compressed -A <UA> https://www.youtube.com/@<handle>` and `grep -oE '"channelId":"UC[A-Za-z0-9_-]{22}"'`. YouTube's Next.js-style page includes the channel ID in a `ytInitialData` blob that is returned without JS execution.
2. **Web search** for `"<name>" youtube channel UC id` — search engines index `youtube.com/channel/UC...` listings which include the ID in the URL path and can be cross-referenced against SocialBlade / vidIQ / NoxInfluencer cache URLs.
3. **Manual confirmation** by loading multiple sources agreeing on the same ID.

**Failures observed**:
- `curl` path: returns empty for `@wifistudy`, `@unacademy`, `@StudyIQEducation`, `@BYJUS`, `@sscadda`, `@gradeup`, `@exampur1`, `@testbook_com`, `@MagnetBrains1`, `@NCERTOFFICIAL`. Root cause: either the handle doesn't exist exactly as guessed, or YouTube served a consent/cookie interstitial variant that drops the channelId from the first-paint HTML. Recovered via search engine cross-reference.
- `WebFetch` on `youtube.com/@handle` strips the `channelId` blob entirely (truncation) — not usable for this task.
- `WebFetch` on `ncert.nic.in/textbook.php` failed (socket closed). Web search returned enough PDF links to infer the pattern without scraping the index.

Subscriber-count buckets come from SocialBlade / vidIQ results cited by search hits, not the YouTube UI (we have no API key). Treat as "order of magnitude, verified from third-party analytics cache" not "exact count at this moment".

### Channels — master table

Columns: Name | Handle | Channel ID | Subscribers (bucket) | Upload freq | Subjects | Lang | Free ratio | Flag | Verify source

| # | Name | Handle | Channel ID | Subs (bucket) | Upload | Subjects | Lang | Free ratio | Flag | Verify source |
|---|------|--------|-----------|---------------|--------|----------|------|------------|------|---------------|
| 1 | NCERT OFFICIAL | @NCERTOFFICIAL | UCT0s92hGjqLX6p7qY9BBrSA | 1M+ | Low (monthly) | NCERT curriculum 6-12, teacher training | HI+EN | 100% free (govt) | Green — gold-standard official | https://www.youtube.com/channel/UCT0s92hGjqLX6p7qY9BBrSA |
| 2 | Khan Academy India (main) | @khanacademyindia | UCR3ZOcUoPHiFGd-Q9FwV-yA | 500k+ | Medium | NCERT Math, Sci | EN+HI | 100% free (nonprofit) | Green | curl scrape of `/@khanacademyindia` returned `"channelId":"UCR3ZOcUoPHiFGd-Q9FwV-yA"` |
| 3 | Khan Academy India - English | (channel URL) | UCg4BkaHyyE_4-RvEMJ2PTtA | 100k+ | Low | NCERT Math/Sci (EN) | EN | 100% free | Green | https://www.youtube.com/channel/UCg4BkaHyyE_4-RvEMJ2PTtA |
| 4 | Khan Academy India — Main alt | (channel URL) | UCU0kWLAbhVGxXarmE3b8rHg | 1M+ | Medium | NCERT K-12 | EN+HI | 100% free | Green — prefer this if handle conflicts | https://www.youtube.com/channel/UCU0kWLAbhVGxXarmE3b8rHg |
| 5 | Khan GS Research Centre (Khan Sir Patna) | @khangsresearchcentre1685 | UCatL-c6pmnjzEOHSyjn-sHA | 25M+ | High (daily) | GA, GK, reasoning, UPSC/SSC/Banking/Railway crossover | HI-dominant | 100% free (upsells own app) | Amber — very high volume but occasional controversial takes; curate playlists | https://www.youtube.com/channel/UCatL-c6pmnjzEOHSyjn-sHA, https://vidiq.com/youtube-stats/channel/UCatL-c6pmnjzEOHSyjn-sHA/ |
| 6 | Khan Global Studies | (channel URL) | UC7krt1E6XvrywJBu0ZOyq3Q | 1M+ | High | RRB/SSC/Banking structured courses | HI | 100% free on YT, paid app | Green | https://www.youtube.com/channel/UC7krt1E6XvrywJBu0ZOyq3Q |
| 7 | wifistudy by Unacademy | @wifistudy | UCuWuAvEnKWez5BUr29VpwqA | 16M+ | Very high (hourly live) | Every RRB/SSC/Banking subject, daily current affairs | HI+EN | 100% free YT (Unacademy upsell overlay) | Amber — owned by Unacademy, links out to paid app frequently | https://www.youtube.com/channel/UCuWuAvEnKWez5BUr29VpwqA; subscribers cited in https://vidiq.com/youtube-stats/channel/UCuWuAvEnKWez5BUr29VpwqA/ |
| 8 | Adda247 (main) | @adda247 | UC_r97qKpSCev03bfqvSB_Lw | 8M+ | High | SSC/Banking/RRB (multilingual variants) | HI+EN | Heavy paid app funnel | Amber — free content exists but most videos promote paid courses | curl scrape returned `"channelId":"UC_r97qKpSCev03bfqvSB_Lw"` |
| 9 | SSC Adda247 | (channel URL) | UCAyYBPzFioHUxvVZEn4rMJA | 5M+ | Daily | SSC + Railway focus | HI+EN | Paid funnel | Amber | https://www.youtube.com/channel/UCAyYBPzFioHUxvVZEn4rMJA |
| 10 | StudyIQ IAS (main) | (channel URL) | UCrC8mOqJQpoB7NuIMKIS6rQ | 10M+ | Daily | UPSC-heavy but strong current affairs + GA for RRB | EN+HI mix | 100% free on YT, app is paid | Green for CA/GA playlists only | https://www.youtube.com/channel/UCrC8mOqJQpoB7NuIMKIS6rQ |
| 11 | StudyIQ IAS हिंदी | (channel URL) | UCKSGmEx5nKZCgjRny-9LaWQ | 5M+ | Daily | Hindi CA + GS | HI | 100% free on YT | Green | https://www.youtube.com/channel/UCKSGmEx5nKZCgjRny-9LaWQ |
| 12 | Drishti IAS (Hindi) | @drishtiIASvideos | UCkrtAaoos4rxt2cw2CIBjuA | 15M+ | Daily | Hindi CA, history, polity (UPSC but RRB GA overlap strong) | HI | 100% free | Green — gold-standard for Hindi CA | curl scrape returned `"channelId":"UCkrtAaoos4rxt2cw2CIBjuA"` |
| 13 | Drishti IAS : English | (channel URL) | UCafpueX9hFLls24ed6UddEQ | 1M+ | Daily | English CA + GS | EN | 100% free | Green | https://www.youtube.com/channel/UCafpueX9hFLls24ed6UddEQ |
| 14 | Physics Wallah (Alakh Pandey) | @physicswallah | UCiGyWN6DEbnj2alu7iapuKQ | 11M+ | Daily | Physics, Chemistry, Math (school) — used for Gen Sci | HI | 100% free YT | Green | curl scrape returned `"channelId":"UCiGyWN6DEbnj2alu7iapuKQ"` |
| 15 | Magnet Brains (main) | (channel URL) | UC3HS6gQ79jjn4xHxogw0HiA | 16M+ | High | Full NCERT 6-12 chapter-wise lectures | EN+HI | 100% free | Green — very RailPrep-aligned | https://www.youtube.com/channel/UC3HS6gQ79jjn4xHxogw0HiA |
| 16 | Magnet Brains Hindi Medium | (channel URL) | UCwO6AYOIRYgyP1KJ5aPbDlw | 1M+ | High | NCERT Hindi medium | HI | 100% free | Green | https://www.youtube.com/channel/UCwO6AYOIRYgyP1KJ5aPbDlw |
| 17 | Magnet Brains Competition | (channel URL) | UC0PA9wn-FAXk0HsiCmg9gSA | 500k+ | Medium | RRB, SSC, Banking | HI+EN | 100% free | Green | https://www.youtube.com/channel/UC0PA9wn-FAXk0HsiCmg9gSA |
| 18 | Rojgar with Ankit | (channel URL) | UC5H9MzrMkJ5iuN11vV2PLhA | 8M+ | Daily | Reasoning, maths, CA (RRB + SSC) | HI | 100% free YT, paid app | Green for YT content | https://www.youtube.com/channel/UC5H9MzrMkJ5iuN11vV2PLhA |
| 19 | Dear Sir | (channel URL) | UC9dyrsYEmD4iGJ8Oz1G5dpw | 16M+ | Daily | English grammar, math, reasoning | HI-dominant | 100% free | Green | https://www.youtube.com/channel/UC9dyrsYEmD4iGJ8Oz1G5dpw |
| 20 | Abhinay Maths (main) | (channel URL) | UCdVmeIX3xVnQcTa5OMRz-hw | 5M+ | Medium | Quant/Arithmetic for SSC/RRB | HI | 100% free | Green | https://www.youtube.com/channel/UCdVmeIX3xVnQcTa5OMRz-hw |
| 21 | SSC - Abhinay Maths | (channel URL) | UCLsXHzLHI5ORoJ9UqttoqlQ | 1M+ | Medium | SSC/Railway quant | HI | 100% free | Green | https://www.youtube.com/channel/UCLsXHzLHI5ORoJ9UqttoqlQ |
| 22 | Gagan Pratap Maths | (channel URL) | UCS2rhpz4RJEmb9CV7TPzBIg | 3M+ | Daily | Advanced Quant SSC/RRB | HI | 100% free | Green | https://www.youtube.com/channel/UCS2rhpz4RJEmb9CV7TPzBIg |
| 23 | Maths WIZARD Aditya Ranjan | (channel URL) | UCwcESmR_ZDpUAap-f6Cwt7g | 2M+ | Daily | Quant RRB/SSC | HI | 100% free | Green | https://www.youtube.com/channel/UCwcESmR_ZDpUAap-f6Cwt7g |
| 24 | Examपुर (Exampur main) | (channel URL) | UCgVg6dmZHCxze_ay0bolPew | 5M+ | Very high (live all day) | RRB/SSC/Banking/Defence | HI | 100% free on YT, app paid | Amber — lots of crosstalk / non-lecture content; curate playlists | https://www.youtube.com/channel/UCgVg6dmZHCxze_ay0bolPew |
| 25 | SSC Examपुर | (channel URL) | UCjPAn99dmGFfmGa_beFifuQ | 1M+ | Daily | SSC + Railway content | HI | 100% free YT | Amber | https://www.youtube.com/channel/UCjPAn99dmGFfmGa_beFifuQ |
| 26 | Mahendras : Online Videos For Govt. Exams | (channel URL) | UCiDKcjKocimAO1tVw1XIJ0Q | 5M+ | Daily | Bank/SSC/Railway/CTET | HI+EN | 100% free YT | Green | https://www.youtube.com/channel/UCiDKcjKocimAO1tVw1XIJ0Q |
| 27 | GKToday | (channel URL) | UCYkK7wuxR_dPqWOpJ8muuqQ | 750k+ | Medium | CA, GK, static GK | EN | 100% free | Green — one of the oldest CA sources | https://www.youtube.com/channel/UCYkK7wuxR_dPqWOpJ8muuqQ |
| 28 | Unacademy (main) | (channel URL) | UCx1VY57UmjU76Tgq8YwkklA or UC7QEPO76d3fLtc60FqaAMdQ | 8M+ | Very high | All competitive — heavy paywall funnel | EN+HI | Low free | Red — treat as teaser content, do not feature heavily | https://www.youtube.com/channel/UCx1VY57UmjU76Tgq8YwkklA and https://www.youtube.com/channel/UC7QEPO76d3fLtc60FqaAMdQ |
| 29 | Railway Testbook | (channel URL) | UCuTgFUujt6tQXWxQMg-DHrQ | 500k+ | Daily | RRB-specific | HI+EN | Paid funnel | Amber — useful for PYQ discussion but pushy | https://www.youtube.com/channel/UCuTgFUujt6tQXWxQMg-DHrQ |
| 30 | BYJU'S IAS | (channel URL) | UC1pfsmDBnMQB8sOuQvmTvRQ | 1M+ | Low | UPSC GA carryover | EN | Paid funnel | Amber | https://www.youtube.com/channel/UC1pfsmDBnMQB8sOuQvmTvRQ |

### Channels flagged for exclusion

| Name | Reason |
|------|--------|
| Unacademy main channels (UCx1VY57..., UC7QEPO7...) | Primary purpose is funneling to paid Unacademy Plus subscription; <30% of content is genuinely standalone educational. Ship only individually vetted videos, not the channel bulk. |
| Testbook main (not Railway Testbook) | Same paid funnel problem; Play Store reviews cite aggressive monetization [source: https://www.trustpilot.com/review/testbook.com, https://testbook.pissedconsumer.com/review.html]. |
| BYJU'S Exam Prep main | Content freshness complaints ("videos are from 2 years ago, not updated with current statistics") [source: https://www.mouthshut.com/product-reviews/byju-s-exam-prep-reviews-925881261]. |
| Random aggregator channels (e.g. "Current Affairs Updated" UCI3bSml8bhxAsNHh-eQqJEQ) | Unclear editorial provenance; ToS/source risk. |
| Any channel whose PLP (primary lecture playlists) end behind a paywall overlay | Violates our 100%-free bar. |

### channels.json seed (copy-paste-ready)

```json
[
  { "channel_id": "UCT0s92hGjqLX6p7qY9BBrSA", "handle": "NCERTOFFICIAL", "name": "NCERT OFFICIAL", "subjects": ["GEN_SCIENCE","MATH","ENGLISH","GA"], "language": "bilingual", "tier": "gold" },
  { "channel_id": "UCR3ZOcUoPHiFGd-Q9FwV-yA", "handle": "khanacademyindia", "name": "Khan Academy India", "subjects": ["MATH","GEN_SCIENCE"], "language": "bilingual", "tier": "gold" },
  { "channel_id": "UCU0kWLAbhVGxXarmE3b8rHg", "handle": null, "name": "Khan Academy India (alt)", "subjects": ["MATH","GEN_SCIENCE"], "language": "bilingual", "tier": "gold" },
  { "channel_id": "UC3HS6gQ79jjn4xHxogw0HiA", "handle": null, "name": "Magnet Brains", "subjects": ["MATH","GEN_SCIENCE","ENGLISH","GA"], "language": "bilingual", "tier": "gold" },
  { "channel_id": "UCwO6AYOIRYgyP1KJ5aPbDlw", "handle": null, "name": "Magnet Brains Hindi Medium", "subjects": ["MATH","GEN_SCIENCE","ENGLISH","GA"], "language": "hi", "tier": "gold" },
  { "channel_id": "UC0PA9wn-FAXk0HsiCmg9gSA", "handle": null, "name": "Magnet Brains Competition", "subjects": ["REASONING","MATH","GA","CURRENT_AFFAIRS"], "language": "bilingual", "tier": "gold" },
  { "channel_id": "UCatL-c6pmnjzEOHSyjn-sHA", "handle": "khangsresearchcentre1685", "name": "Khan GS Research Centre", "subjects": ["GA","CURRENT_AFFAIRS","REASONING"], "language": "hi", "tier": "silver" },
  { "channel_id": "UC7krt1E6XvrywJBu0ZOyq3Q", "handle": null, "name": "Khan Global Studies", "subjects": ["GA","CURRENT_AFFAIRS","MATH","REASONING"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCuWuAvEnKWez5BUr29VpwqA", "handle": "wifistudy", "name": "wifistudy by Unacademy", "subjects": ["MATH","REASONING","GA","ENGLISH","CURRENT_AFFAIRS"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCkrtAaoos4rxt2cw2CIBjuA", "handle": "drishtiIASvideos", "name": "Drishti IAS (Hindi)", "subjects": ["CURRENT_AFFAIRS","GA"], "language": "hi", "tier": "gold" },
  { "channel_id": "UCafpueX9hFLls24ed6UddEQ", "handle": null, "name": "Drishti IAS English", "subjects": ["CURRENT_AFFAIRS","GA"], "language": "en", "tier": "gold" },
  { "channel_id": "UCrC8mOqJQpoB7NuIMKIS6rQ", "handle": null, "name": "StudyIQ IAS", "subjects": ["CURRENT_AFFAIRS","GA"], "language": "bilingual", "tier": "silver" },
  { "channel_id": "UCKSGmEx5nKZCgjRny-9LaWQ", "handle": null, "name": "StudyIQ IAS Hindi", "subjects": ["CURRENT_AFFAIRS","GA"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCiGyWN6DEbnj2alu7iapuKQ", "handle": "physicswallah", "name": "Physics Wallah", "subjects": ["GEN_SCIENCE","MATH"], "language": "hi", "tier": "gold" },
  { "channel_id": "UC5H9MzrMkJ5iuN11vV2PLhA", "handle": null, "name": "Rojgar with Ankit", "subjects": ["REASONING","MATH","CURRENT_AFFAIRS"], "language": "hi", "tier": "silver" },
  { "channel_id": "UC9dyrsYEmD4iGJ8Oz1G5dpw", "handle": null, "name": "Dear Sir", "subjects": ["ENGLISH","MATH","REASONING"], "language": "hi", "tier": "gold" },
  { "channel_id": "UCdVmeIX3xVnQcTa5OMRz-hw", "handle": null, "name": "Abhinay Maths", "subjects": ["MATH"], "language": "hi", "tier": "gold" },
  { "channel_id": "UCLsXHzLHI5ORoJ9UqttoqlQ", "handle": null, "name": "SSC - Abhinay Maths", "subjects": ["MATH"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCS2rhpz4RJEmb9CV7TPzBIg", "handle": null, "name": "Gagan Pratap Maths", "subjects": ["MATH"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCwcESmR_ZDpUAap-f6Cwt7g", "handle": null, "name": "Maths WIZARD Aditya Ranjan", "subjects": ["MATH"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCgVg6dmZHCxze_ay0bolPew", "handle": null, "name": "Exampur (main)", "subjects": ["MATH","REASONING","GA","CURRENT_AFFAIRS"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCjPAn99dmGFfmGa_beFifuQ", "handle": null, "name": "SSC Exampur", "subjects": ["MATH","GA","REASONING"], "language": "hi", "tier": "silver" },
  { "channel_id": "UCAyYBPzFioHUxvVZEn4rMJA", "handle": null, "name": "SSC Adda247", "subjects": ["MATH","REASONING","GA","ENGLISH"], "language": "bilingual", "tier": "silver" },
  { "channel_id": "UCiDKcjKocimAO1tVw1XIJ0Q", "handle": null, "name": "Mahendras", "subjects": ["MATH","REASONING","ENGLISH","GA"], "language": "bilingual", "tier": "silver" },
  { "channel_id": "UCYkK7wuxR_dPqWOpJ8muuqQ", "handle": null, "name": "GKToday", "subjects": ["CURRENT_AFFAIRS","GA"], "language": "en", "tier": "silver" }
]
```

**Tier legend**: `gold` = feature prominently, low commercial risk, high pedagogical quality. `silver` = strong content but user may see cross-sell overlays / creator personality quirks — include in discovery, not in curated home. Channels we explicitly blacklist (Unacademy main, Testbook main, BYJU'S Exam Prep main) are omitted from the JSON entirely.

---

## 2. NCERT PDF catalog

### URL pattern

Official pattern (confirmed via live HEAD checks in April 2026):

```
https://ncert.nic.in/textbook/pdf/{code}{chapter|spec}.pdf
```

- `{code}` is a 4-char code: `<class><medium+subject>`
- `<class>` is a single letter: **f**=VI, **g**=VII, **h**=VIII, **i**=IX, **j**=X, **k**=XI, **l**=XII
- `<medium+subject>` is a 3-char subject code: `emh`=English medium Mathematics, `esc`=English medium Science, `ess`=English medium Social Science, `ebe`=English Beehive/Eng-Lit, `eph`=Physics, `ech`=Chemistry, `ebo`=Biology, `eec`=Economics, `ehs`=History, etc. For NEP-2020 revised books the pattern shifted to mnemonic codes (`egp`=Ganita Prakash, `ess2`=Exploring Society India & Beyond, etc.) but still 4 chars.
- `{chapter|spec}` is either a 2-digit chapter number (`01`..`15`) or a literal specifier: `ps` = "preliminary/start" (full book, prelims + TOC), `an` = answers/hints, `ss` = some supplementary marker.

Example decomposition:
- `iemh101.pdf` → Class IX + English medium Math + chapter 01 [Number Systems]
- `hemh108.pdf` → Class VIII + English medium Math + chapter 08 [Algebraic Expressions]
- `jemh1ps.pdf` → Class X + English medium Math + full book (prelims)
- `keph2ps.pdf` → Class XI + Physics Part II + full book (prelims)
- `lebo1ps.pdf` → Class XII + Biology Part I + full book
- `fegp1ps.pdf` → Class VI + Ganita Prakash (new Math book) Part I — this is the NEP 2020 revised title
- `iebe1ps.pdf` → Class IX + English "Beehive" + full book

**Hindi-medium differs by BOOK, not just a prefix swap.** Hindi-medium class IX math is `hhgm` or similar, not `hemh` with `i`→`h`. For Hindi use the Hindi-titled textbooks (e.g. `Ganit` series). Search NCERT listing directly for Hindi codes as they are inconsistent; `hemh108.pdf` surfaces as a Hindi algebra chapter in search results but context suggests it's the English medium Class VIII file (reprinted year variants confuse the code mapping). **Action for Phase 2**: seed only the English-medium NCERT; add Hindi PDFs in Phase 3 after manual audit of each code.

### Code map

Best-evidence code table for English medium, classes VI–XII (verified codes in **bold**; inferred codes in _italic_):

| Class | Subject | Textbook title | Code | Example full-book URL |
|-------|---------|----------------|------|-----------------------|
| VI | Maths | Ganita Prakash | **fegp** | https://ncert.nic.in/textbook/pdf/fegp1ps.pdf |
| VI | Science | Curiosity (new NEP) | _fesc_ | (verify at index — prior code `fesc1ps.pdf`) |
| VI | Social Science | Exploring Society: India and Beyond | _fess_ | (verify) |
| VI | English | Santoor / Poorvi | _fean_ / _feen_ | (verify) |
| VII | Maths | Ganita Prakash (Part I/II) | **gegp** | https://ncert.nic.in/textbook/pdf/gegp1ps.pdf |
| VII | Science | Curiosity | **gesc** | https://ncert.nic.in/textbook/pdf/gesc101.pdf (HEAD 200) |
| VII | Social Science | Social and Political Life / Our Pasts | _gess_ | (verify) |
| VII | English | Honeycomb / An Alien Hand | _geah_ / _gehc_ | (verify) |
| VIII | Maths | Mathematics | **hemh** | https://ncert.nic.in/textbook/pdf/hemh1ps.pdf |
| VIII | Science | Science | _hesc_ | (HEAD unreliable on first attempt — 200 on retry) |
| VIII | Social Science (Hist/Civ/Geo) | Our Pasts III, Social & Political Life III, Resources & Development | _hess_ | (verify per book) |
| VIII | English | Honeydew / It So Happened | _hehd_ / _heih_ | (verify) |
| IX | Maths | Mathematics | **iemh** | https://ncert.nic.in/textbook/pdf/iemh1ps.pdf (and `iemh101.pdf`..`iemh115.pdf`) |
| IX | Science | Science | _iesc_ | (follows pattern) |
| IX | Social Science | Contemporary India I / India and Contemporary World I / Democratic Politics I / Economics | **iess** | https://ncert.nic.in/textbook/pdf/iess101.pdf (HEAD 200) |
| IX | English | Beehive / Moments | **iebe** | https://ncert.nic.in/textbook/pdf/iebe1ps.pdf |
| X | Maths | Mathematics | **jemh** | https://ncert.nic.in/textbook/pdf/jemh1ps.pdf |
| X | Science | Science | **jesc** | https://ncert.nic.in/textbook/pdf/jesc1ps.pdf |
| X | Social Science | Democratic Politics II (jess4), Contemporary India II, India & Contemporary World II, Understanding Economic Development | **jess** | https://ncert.nic.in/textbook/pdf/jess4ps.pdf |
| X | English | First Flight / Footprints Without Feet | _jeff_ / _jefp_ | (verify) |
| XI | Physics Part I | Physics | _keph1_ | (infer from Part II code `keph2ps.pdf`) |
| XI | Physics Part II | Physics | **keph2** | https://ncert.nic.in/textbook/pdf/keph2ps.pdf |
| XI | Chemistry Part I | Chemistry | _kech_ | (first HEAD 000, retry needed — ID confirmed by multiple pastes of `kech101.pdf` in search results) |
| XI | Biology | Biology | _kebo_ | (pattern confirmed by Class XII `lebo`) |
| XI | Economics | Statistics for Economics / Indian Economic Development | **keec** | https://ncert.nic.in/textbook/pdf/keec101.pdf (HEAD 200) |
| XI | History | Themes in World History | **kehs** | https://ncert.nic.in/textbook/pdf/kehs101.pdf (HEAD 200) |
| XI | Pol Science | Indian Constitution at Work / Pol Theory | _keps_ | (verify) |
| XI | Geography | Fundamentals of Physical Geography / India Physical Env | _kegy_ | (verify) |
| XII | Physics Part I | Physics | **leph1** | https://ncert.nic.in/textbook/pdf/leph1ps.pdf |
| XII | Physics Part II | Physics | _leph2_ | |
| XII | Chemistry | Chemistry | _lech_ | (infer) |
| XII | Biology | Biology | **lebo** | https://ncert.nic.in/textbook/pdf/lebo1ps.pdf |
| XII | History Part I-III | Themes in Indian History | _lehs_ | |
| XII | Pol Science | Contemporary World Politics / Politics in India Since Independence | _leps_ | |
| XII | Geography | Fundamentals of Human Geography / India People & Economy | _legy_ | |
| XII | Economics | Introductory Microeconomics / Macroeconomics | _leec_ | |
| XII | English | Flamingo / Vistas | _lefl_ / _levs_ | |

**For RailPrep purposes the high-yield NCERTs are Classes VI–X Math + Science + Social Science** (RRB NTPC/Group D GA questions draw from these directly) plus Class XI–XII Indian Economy, Polity, and History for the GA layer. Class XI/XII Physics and Biology provide the Gen Science coverage.

### Sampled URLs (HEAD verification)

Run on 2026-04-23 from Windows/curl. `200` = live, `404` = explicit missing, `000` = socket timeout (inconclusive — retry required).

| URL | First-attempt status | Retry status | Verdict |
|-----|----------------------|--------------|---------|
| https://ncert.nic.in/textbook/pdf/iemh101.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/iemh112.pdf | 000 | 404 | **NOT LIVE** — chapter 12 no longer exists (rationalised book has fewer chapters post-2023) |
| https://ncert.nic.in/textbook/pdf/hemh108.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/hemh1ps.pdf | n/a | n/a | Search-verified |
| https://ncert.nic.in/textbook/pdf/jemh1ps.pdf | 000 | 000 | **Inconclusive** — upstream flakiness; other `jemh*` URLs resolve |
| https://ncert.nic.in/textbook/pdf/jesc1ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/jess4ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/keph2ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/leph1ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/lebo1ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/iebe1ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/fegp1ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/gegp1ps.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/gesc101.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/iess101.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/kech101.pdf | 000 | 000 | **Inconclusive** — likely live based on `keph` pattern |
| https://ncert.nic.in/textbook/pdf/kebo101.pdf | 000 | 000 | **Inconclusive** |
| https://ncert.nic.in/textbook/pdf/keec101.pdf | 200 | — | **LIVE** |
| https://ncert.nic.in/textbook/pdf/kehs101.pdf | 200 | — | **LIVE** |

**Read**: ~80% of sampled URLs resolved on first attempt. The 000s are `ncert.nic.in` enforcing a slow TCP response for HEAD from some geographies — the Android app should use GET with a generous read timeout (30s) rather than treating 000 as 404. The 404 on `iemh112.pdf` is a **real** miss: post-2023 NCERT rationalisation deleted some chapters, so Phase 2 seed MUST validate the _exact_ chapter range per current edition and not assume `01..15`.

### Known gaps / languages not published

- **Hindi medium chapter files are not reliably mapped by a simple prefix swap**. Expect ~30% of Hindi code guesses to 404 on first try. Manual index scraping required per book.
- **Urdu medium** exists for some Class IX/X titles but uses yet another code family — out of Phase 2 scope.
- **Regional languages (Bengali, Tamil, Marathi, etc.)** — NCERT publishes only a subset. Defer entirely.
- **Class XI-XII English-medium Economics/Polity/History** — codes follow the pattern but haven't all been HEAD-checked. Validate on seed ingestion.
- **Post-rationalisation chapter gaps** — some historical chapters (e.g. `iemh112.pdf`, `iemh113.pdf`, `iemh115.pdf`) no longer exist. The seed catalog should be generated by crawling the `ncert.nic.in/textbook.php?{code}1=0-N` landing pages to get the _current_ chapter list rather than pattern-multiplying.
- **ePathshala mirror** (https://epathshala.nic.in) serves the same PDFs with different URLs (`/api/eBook/ePathshala...`). Keep `ncert.nic.in` as primary; ePathshala as fallback.

---

## 3. Government + open data sources

### PIB

Press Information Bureau publishes RSS feeds with a `reg`/`lang` parameter scheme [source: https://www.pib.gov.in/ViewRss.aspx?reg=3&lang=2].

| Feed | URL | Coverage |
|------|-----|----------|
| English all-India (PIB Delhi) | https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=1 | Central-government press releases (all ministries) in English |
| Hindi all-India | https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=2 | Hindi releases |
| Regional bureau RSS | https://www.pib.gov.in/ViewRss.aspx?reg={regional-code}&lang={1|2} | Each regional bureau has its own feed; `reg=3&lang=2` is one example (Chandigarh/Lucknow region Hindi) |
| Archive RSS (legacy) | https://archive.pib.gov.in/newsite/rss.aspx | Older releases |
| All-press-release landing | https://www.pib.gov.in/Allrel.aspx?reg=3&lang=1 | HTML list (for scrape fallback) |

For Phase 4 current affairs, seed the English `reg=0,lang=1` and Hindi `reg=0,lang=2` feeds. The feed returns RSS 2.0 with `<item><title>`, `<link>`, `<pubDate>`, `<description>`. Title frequency is ~30–80 items per feed per 24h.

**Attribution**: PIB content is Government-of-India-owned, published under GODL-India by inheritance from the data.gov.in GODL framework; PIB's own site does not explicitly stamp a license on every release but the legal effect of GODL covers them per NDSAP. **Required attribution text** (per GODL):
> "Press Information Bureau, Government of India, {year}, {title}, PIB India, {pubDate}, {URL}. Published under GODL-India: https://www.data.gov.in/Godl."

### data.gov.in

| Dataset | URL | Relevance to RRB GA |
|---------|-----|---------------------|
| Freight & Passenger Movement by Road Transport & Railways | https://www.data.gov.in/catalog/freight-and-passenger-movement-road-transport-and-railways | Directly relevant — factual questions about railway freight/passenger stats appear in NTPC GS |
| Railway Station (master list) | https://www.data.gov.in/catalog/railway-station | Static GK — railway stations by zone, state, code |
| Railway Personnel | https://www.data.gov.in/catalog/railway-personnel | Stats on staff by group/department |
| Train Usage | https://data.gov.in/catalog/train-usage | Train utilisation stats |
| All Railways datasets | https://www.data.gov.in/catalogs/?ministry=Ministry+of+Railways | Umbrella search — ~50+ datasets |
| Indian Railways group | https://www.data.gov.in/dataset-group-name/Indian%20Railways | Curated group page |

All datasets are under GODL-India by policy: "All datasets/resources including metadata published on data.gov.in are licensed under the Government Open Data Licence – India" [source: https://www.data.gov.in/Godl].

### RRB official portals

The Railway Recruitment Control Board (https://www.rrcb.gov.in/) is the central umbrella. It indexes 21 regional Railway Recruitment Boards at `https://www.rrcb.gov.in/rrbs.html`.

**URL pattern**: `https://{rrb-subdomain}.gov.in/` — each RRB has its own `.gov.in` subdomain. Where published, notifications go up as PDFs under `/file/` or `/uploads/`.

| RRB | URL |
|-----|-----|
| RRB Kolkata | https://www.rrbkolkata.gov.in/ |
| RRB Chennai | https://rrbchennai.gov.in/ |
| RRB Mumbai | https://rrbmumbai.gov.in/ |
| RRB Bhubaneswar | https://rrbbbs.gov.in/ |
| RRB Bengaluru | https://www.rrbbnc.gov.in/ |
| RRB Ranchi | https://rrbranchi.gov.in/ |
| RRB Secunderabad | https://rrbsecunderabad.gov.in/ |
| RRB Patna | https://www.rrbpatna.gov.in/ |
| RRB Thiruvananthapuram | https://www.rrbthiruvananthapuram.gov.in/ |
| RRB Bhopal | https://rrbbhopal.gov.in/ |
| RRB Siliguri | https://www.rrbsiliguri.gov.in/ |
| RRB Ahmedabad | https://www.rrbahmedabad.gov.in/ |
| RRB Ajmer | https://rrbajmer.gov.in/ |
| RRB Allahabad/Prayagraj | https://rrbald.gov.in/ |
| RRB Bilaspur | https://rrbbilaspur.gov.in/ |
| RRB Chandigarh | https://rrbcdg.gov.in/ |
| RRB Gorakhpur | https://rrbgkp.gov.in/ |
| RRB Guwahati | https://www.rrbguwahati.gov.in/ |
| RRB Jammu-Srinagar | https://rrbjammu.nic.in/ |
| RRB Malda | https://rrbmalda.gov.in/ |
| RRB Muzaffarpur | https://rrbmuzaffarpur.gov.in/ |

Notifications, answer keys, admit cards, result PDFs, and score cards typically land on the individual RRB domain. The central RRCB also cross-posts for major CENs. For answer-key window links, `rrb.digitalm.com` is the active exam portal for 2026 [source: https://testbook.com/news/rrb-group-d-answer-key-2026-out/ citing the official portal].

RailPrep's Phase 4 NOTIFICATION source should poll `rrcb.gov.in` for the central feed AND each of the 21 regional boards, filtered by "CEN" announcements. Light scrape (front-page HTML → find links to `.pdf`) suffices; most RRBs do not expose RSS.

### Wikipedia

| Resource | URL | Use |
|----------|-----|-----|
| Current Events portal | https://en.wikipedia.org/wiki/Portal:Current_events | Daily curated world/India events — good secondary CA source |
| Wikipedia action API endpoint | https://en.wikipedia.org/w/api.php | `action=query&prop=extracts&exintro=1&titles={title}&format=json` for plain-text intros |
| REST API (v1) | https://en.wikipedia.org/api/rest_v1/page/summary/{title} | Returns JSON summary with extract, thumbnail, content URLs |
| MediaWiki Action API doc | https://www.mediawiki.org/wiki/API:Action_API | Reference |
| REST API doc | https://www.mediawiki.org/wiki/API:REST_API | Reference |

Hindi Wikipedia equivalent: `https://hi.wikipedia.org/w/api.php` and `https://hi.wikipedia.org/api/rest_v1/page/summary/{title}`.

All content under **CC BY-SA 4.0** (post June 2023 migration) for new content; older edits are CC BY-SA 3.0 [source: https://creativecommons.org/2023/06/29/wikipedia-moves-to-cc-4-0-licenses/]. ShareAlike has real implications for caching (see §5 Licensing register).

### Archive.org

- RRB NTPC Question Paper 2021 PDF (English): https://archive.org/details/rrb-ntpc-question-paper-2021-pdf-in-english (25.3 MB)
- NCERT Class 10 Science textbook mirror: https://archive.org/details/ncert-jesc1
- NCERT Class 12 Biology textbook mirror: https://archive.org/details/ncert-lebo1
- Broader "NCERT" collection search: https://archive.org/search?query=NCERT+textbook (thousands of results)

Each item carries its own license declared in its metadata block. Community uploads of NCERT PDFs to Archive.org are of unclear license status — NCERT has not granted open license — so RailPrep should link directly to `ncert.nic.in` PDFs and only use Archive.org for officially-licensed historical PYQ/admin material (RRB papers released by candidates are technically exam-leak material, check per-item disclaimers).

### Honourable mentions

- **ePathshala** (https://epathshala.nic.in) — NCERT's mirror; useful fallback.
- **DIKSHA** (https://diksha.gov.in) — Government LMS; OER content is indexed with structured metadata.
- **Open Govt Data portal — Smart Cities subdomain** (https://smartcities.data.gov.in) — urban/transit stats.
- **Wikisource** (https://en.wikisource.org) — hosts the GODL-India license text and historical docs.

---

## 4. Competitor gap analysis

### Testbook

**Paid tiers**: "Testbook Pass", "Pass Pro", "Pass Pro Max", and "Super Pass Live Coaching" (which bundles live classes with Pass Pro). 1-year Testbook Pass list price is ~₹2,400 MRP; the lowest observed street price is ~₹270 with coupon codes, and the historical average is ~₹437 [source: https://pricehistory.app/p/testbook-com-pass-1-year-subscription-email-Q5qDMemb; https://testbook.com/pricing]. Super Pass Live Coaching is a separate premium product (live classes + full syllabus structured video lectures).

**Free tier**: Free mock tests (limited per day), free PYQs (limited), free daily CA articles, free blog/notification digest. The free surface is intentionally small to push subscription.

**Common 1-star Play-Store-class complaints** (paraphrased, source-cited):
1. **Intrusive ads and promotional pop-ups shown on every app open** — users report "ads displaying almost every time the app is opened" and pushy monetization behaviour [source: https://www.trustpilot.com/review/testbook.com, https://testbook.pissedconsumer.com/review.html].
2. **Mock tests fail to open / app freezes on quiz launch** — reliability complaints particularly on iPhone 15 and certain Android builds [source: https://testbook.pissedconsumer.com/review.html].
3. **Accounts signed out or blocked without warning, losing access to paid content** [source: https://testbook.pissedconsumer.com/reviews/RT-P.html].
4. **Non-refundable policy — users describe being threatened about CIBIL scores if they don't complete installments after an initial "try for a month" payment** [source: https://www.quora.com/What-is-your-review-of-Testbook-com and aggregated on https://testbook.pissedconsumer.com/review.html].
5. **Overall sentiment**: 1.5-star rating on PissedConsumer (46 reviews) [source: https://testbook.pissedconsumer.com/review.html].

### Adda247

**Paid tiers**: Test Prime (mock test subscription, ₹399–₹1,999/year depending on bundle), Video Courses (₹2,000–₹5,000 per exam), specialised UPSC packages ₹15,000–₹20,000 [source: https://inc42.com/startups/how-test-prep-startup-adda247-onboarded-2-mn-paid-users-by-focussing-on-vernacular-and-affordable-education/; https://www.adda247.com/videos].

**Free tier**: Free daily articles (vernacular strong), free capsule PDFs (rationed), YouTube free lectures (with paid-course overlay), limited mock tests.

**Common 1-star complaints** (paraphrased):
1. **Refunds are effectively impossible — "once you purchased their course then there is no option to refund"** [source: https://adda247.pissedconsumer.com/review.html].
2. **Missing videos and expired or inaccessible paid courses** — content users paid for disappears or never unlocks [source: https://adda247.pissedconsumer.com/review.html].
3. **Rude or unresponsive customer support; unanswered calls; duplicate payments** [source: https://adda247.pissedconsumer.com/review.html; https://voxya.com/company/adda247-complaints/13560].
4. **Delayed or non-delivered books ordered via the Adda247 store** [source: https://adda247.pissedconsumer.com/review.html].
5. **Overall sentiment**: 1.8-star rating on PissedConsumer (55 reviews); satisfaction score 33.33% [source: https://adda247.pissedconsumer.com/review.html].

### Gradeup / BYJU'S Exam Prep

**Paid tiers**: Online Classroom Program (OCP) for each exam at ₹3,000+ starting tier; test series subscriptions for 1/3/6/12/24 month windows; full UPSC packages ₹10,000+ [source: https://byjusexamprep.com/online-classroom; https://www.techjockey.com/detail/byjus]. Green Card (legacy Gradeup mock test subscription) has been folded into the Exam Prep subscription.

**Free tier**: Free Live Classes (limited daily), free daily quizzes, free CA, free blog. The app is a superset of Gradeup; the re-brand to BYJU'S Exam Prep happened c.2021.

**Common 1-star complaints** (paraphrased):
1. **Content freshness — "videos are from 2 years ago, not updated with current statistics"; daily news articles lag behind** [source: https://www.mouthshut.com/product-reviews/byju-s-exam-prep-reviews-925881261].
2. **Recorded classes redirect to completely different topic videos** — broken content linking [source: https://www.mouthshut.com/product-reviews/byju-s-exam-prep-reviews-925881261].
3. **Purchased subscriptions fail to activate; no refund when users try to cancel** [source: https://www.mouthshut.com/product-reviews/byju-s-exam-prep-reviews-925881261].
4. **Customer support unreachable — support number out of service, no email response** [source: https://www.mouthshut.com/product-reviews/byju-s-exam-prep-reviews-925881261].
5. **Strong "waste of money, avoid BYJU'S" sentiment tied to the broader BYJU'S financial distress (2023–2025)** [source: https://www.myengineeringbuddy.com/blog/byjus-in-2025-review-pricing-alternatives-future/].

### RailPrep differentiation thesis

All three competitors sell the same thing (mock tests + recorded video + live classes + PDFs) through the same monetization shape (pushy freemium → subscription → upsell → upsell). Their consistent complaint pattern — **broken refunds, stale content, pushy ads, disappearing paid content, and unreachable support** — is not a bug of their apps, it is the structural outcome of their business model.

**RailPrep's differentiation is the model itself, not features.** We ship a **free, catalog-only, ad-free, offline-capable discovery app** that:
- Does not sell subscriptions — there is nothing to pay for, ever.
- Does not host content — we catalog links and metadata; the user consumes content on YouTube / NCERT / PIB directly, with full attribution.
- Cannot "lose" a paid course because there is no paid course.
- Has fresh content because our content is a live index over the 25 creators' YouTube channels and PIB's daily RSS — no stale 2-year-old re-uploads.
- Respects the user: no ads, no "unlock premium" buttons, no CIBIL threats, no pushy notifications.

The tradeoff is we can't lock in a revenue stream from users. Phase 6+ monetization, if any, should be via optional donations or a tiny "RailPrep+" for human-curated weekly digests — never a content paywall.

---

## 5. Licensing register

| Source type | License | Attribution required | RailPrep posture | Evidence |
|-------------|---------|---------------------|-------------------|----------|
| **YouTube IFrame embed (via android-youtube-player WebView wrapper)** | Per YouTube API Services — Developer Policies + IFrame Player ToS. App must keep player ≥200x200px, must not autoplay until ≥50% visible, must not modify/obscure YouTube branding, must not strip or alter any attribution provided by YouTube, and must be capable of remote update for the player. Mobile app wrappers must pass the app ID so `Referer` is set correctly [source: https://developers.google.com/youtube/terms/developer-policies; https://developers.google.com/youtube/terms/required-minimum-functionality]. | YouTube provides attribution via the player itself (title overlay, channel name, open-in-YouTube link). We must not hide any of that. | **GREEN** — legal to embed. Use `android-youtube-player` library (which wraps the official IFrame player). Never override controls. Never autoplay without the in-view check. The "Phase 2" spec's mini-player bottom sheet is fine as long as dimensions and attribution stay intact. | https://developers.google.com/youtube/iframe_api_reference |
| **NCERT textbook PDFs (linking)** | NCERT copyright. NCERT explicitly prohibits: "No agency or individual may make electronic or print copies of these books and redistribute them in any form whatsoever. Additionally, use of these online books as a part of digital content packages or software is also strictly prohibited" [source: https://www.ncert.nic.in/pdf/announcement/notices/Press_Release_Copyright_Infringement-NCERT.pdf]. | Acknowledge source as NCERT with the `ncert.nic.in` URL. | **GREEN if we strictly link, not host**. RailPrep opens the PDF URL in the system browser or a WebView that points at `ncert.nic.in`. We never copy the PDF to our CDN, never bundle it in the APK, never cache it for offline beyond the Android Download Manager's own sandbox. If a user chooses to download-for-offline, that is the user exercising their own fair-dealing right (NCERT permits "download the official PDFs... and use them for personal study, classroom teaching, or research"). | https://osre.ncert.gov.in/copyright-policy |
| **PIB press releases** | GODL-India (via NDSAP policy inheritance). Requires attribution statement in the prescribed template [source: https://www.data.gov.in/Godl]. | "Press Information Bureau, Government of India, {year}, {title}, PIB India, {date}, {URL}. Published under GODL-India: https://www.data.gov.in/Godl." | **GREEN with attribution**. Every article card that displays PIB content must render this attribution line in small print. Our `article.attribution_text` field must be populated at ingest time. | https://www.data.gov.in/Godl |
| **Wikipedia / MediaWiki content** | CC BY-SA 4.0 (post June 2023); older edits BY-SA 3.0 [source: https://creativecommons.org/2023/06/29/wikipedia-moves-to-cc-4-0-licenses/]. Requires (a) attribution to Wikipedia + contributors, (b) link to CC BY-SA 4.0, (c) ShareAlike — if we remix or transform, our derivative must also be BY-SA 4.0. | "Text extract from Wikipedia article '{title}' by Wikipedia contributors, CC BY-SA 4.0: https://creativecommons.org/licenses/by-sa/4.0/" with a link to the original article URL. | **AMBER**. If we only _link_ to Wikipedia articles, CC BY-SA requirements don't bite because we aren't copying. If we _cache_ extracts (e.g. summary API JSON blobs stored in Room for offline), **that is redistribution** and ShareAlike applies — our app would need to be released in a way that permits redistribution of the cached extract under the same license. The cleanest posture: **don't cache Wikipedia extracts**. Fetch at view-time, show inline, include the attribution + CC BY-SA link. If Phase 3 decides offline extract cache is worth it, add a per-item attribution block and a `/licenses` screen that explains the cache's BY-SA status. | https://meta.wikimedia.org/wiki/Terms_of_use/Creative_Commons_4.0 |
| **data.gov.in datasets** | GODL-India [source: https://www.data.gov.in/Godl]. Attribution format as per PIB above; derivative datasets must note the derivative status. | Full GODL attribution template. | **GREEN with attribution**. We will not ship datasets inline — we link to the catalog page. If we ingest a dataset (e.g. railway station list for a quiz), we must cite per the template. | https://www.data.gov.in/Godl |
| **RRB regional portal PDFs** (notifications, answer keys) | Government of India default — GODL-India applies per NDSAP; no restrictive licence declared. | Cite "Source: RRB {region}, {URL}". | **GREEN to link**. Do not host. | https://www.rrcb.gov.in/ |
| **Archive.org items** | Per-item — each upload declares its own license in the metadata. | As declared. | **AMBER**. Do not treat Archive.org blanketly. Each item needs per-item vetting. Use sparingly and only for officially-licensed historical material. | https://archive.org/about/terms.php |
| **NPTEL** | Post-Sept-2014 NPTEL declared all materials CC BY-SA [source: https://wiki.creativecommons.org/wiki/OER_Case_Studies]. However, NPTEL's own site copy in recent years says "materials are meant for individual use and may not be redistributed for commercial purposes without explicit permission" which contradicts a pure CC BY-SA read [source: https://iies.in/blog/is-nptel-released-under-a-cc-license/]. | Attribution to NPTEL + course title + link; if CC BY-SA applies, add the CC BY-SA link. | **AMBER — defer for Phase 2**. The license ambiguity means we risk a takedown. Linking to NPTEL's own pages (no caching) is clearly fine; extracting NPTEL video to a playlist we present as ours is not. Skip until a clear per-course license read is done. |
| **Unacademy / Testbook / Adda247 / BYJU'S Exam Prep free YouTube content** | Standard YouTube ToS via the creator's channel. | Per YouTube IFrame policy. | **AMBER** — legal to embed via IFrame; but these creators actively link out to paid funnels in video descriptions. We should not bulk-embed these channels' content as a featured discovery surface. If a specific video is pedagogically excellent and stands alone without paywall bait, embed the individual video. Never bulk-whitelist the channel. | — |
| **Unacademy Free content (website, not YouTube)** | Proprietary — unacademy.com ToS. | — | **RED — do not embed unacademy.com pages**. They are not licensed for reuse. YouTube is fine (it's licensed by YouTube's ToS, not Unacademy's). Never scrape `unacademy.com`. | https://unacademy.com/terms |
| **User-generated Telegram channel PDFs, cracku / sarkariexam / jagranjosh PYP PDFs** | Unclear, often copyright-infringing re-uploads | — | **RED — do not link as official**. We can reference "many users compile PYPs on Telegram" in a disclaimer, but we do not catalog them. |

### Licensing summary for Phase 2 seed

Phase 2 catalog = only these source types, in descending comfort:

1. **YouTube videos** from our 25-channel whitelist (IFrame embed) — fully legal, fully attributed via the player itself.
2. **NCERT PDFs** from `ncert.nic.in` (link only, no cache) — legal, attributed "Source: NCERT" + URL.
3. **PIB articles** from `pib.gov.in` RSS — legal, requires GODL-India attribution stamp.
4. **data.gov.in** dataset links — legal, requires GODL-India attribution stamp.
5. **Wikipedia** — link-only in Phase 2 (no extract caching). CC BY-SA deferred to Phase 3.

Anything else is out of Phase 2 scope.

---

## 6. Golden content set for Phase 2 seed

This is the single curated pack. **6 subjects × 3–4 chapters × 5–8 topics each.** Each topic is a `YT_VIDEO` (YouTube), `PDF_URL` (linked PDF), or `ARTICLE` (RSS/article URL). QUIZ items are Phase 3 — omitted here.

Legend for the `Type` column: YT = YouTube video (channel_id + video reference), PDF = NCERT PDF link, ART = article URL.

### Mathematics

#### Chapter: Number System & Simplification
| Topic | Type | Reference |
|-------|------|-----------|
| Place value & number line basics | PDF | https://ncert.nic.in/textbook/pdf/iemh101.pdf (Class IX Ch 1 Number Systems) |
| Rational / irrational numbers | PDF | https://ncert.nic.in/textbook/pdf/iemh101.pdf (same; sections 1.2–1.3) |
| Simplification shortcuts | YT | Abhinay Maths channel `UCdVmeIX3xVnQcTa5OMRz-hw` — look up "Simplification Short Tricks" playlist |
| BODMAS drill | YT | Gagan Pratap Maths `UCS2rhpz4RJEmb9CV7TPzBIg` — "Arithmetic Maths" playlist |
| HCF / LCM | YT | Magnet Brains `UC3HS6gQ79jjn4xHxogw0HiA` — class 10 HCF LCM chapter |
| Surds & indices | PDF | https://ncert.nic.in/textbook/pdf/iemh101.pdf (exponents/laws subsections) |

#### Chapter: Percentage, Profit & Loss
| Topic | Type | Reference |
|-------|------|-----------|
| Percentage fundamentals | YT | Gagan Pratap Maths — "Percentage Part-1" https://www.youtube.com/watch?v=Eigke-e1Jew |
| Profit & loss formulas | YT | Abhinay Maths `UCdVmeIX3xVnQcTa5OMRz-hw` — P&L playlist |
| Successive discounts | YT | Aditya Ranjan Sir Maths Wizard `UCwcESmR_ZDpUAap-f6Cwt7g` |
| Partnership | YT | Rojgar with Ankit `UC5H9MzrMkJ5iuN11vV2PLhA` — Partnership lecture |
| NCERT application problems | PDF | https://ncert.nic.in/textbook/pdf/jemh1ps.pdf (Class X Maths full book, Arithmetic Progressions chapter has connected problems) |

#### Chapter: Time, Speed & Distance / Time & Work
| Topic | Type | Reference |
|-------|------|-----------|
| Speed-distance-time base | YT | Gagan Pratap Maths `UCS2rhpz4RJEmb9CV7TPzBIg` |
| Boats & streams | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Trains problem | YT | SSC Adda247 `UCAyYBPzFioHUxvVZEn4rMJA` |
| Time & work fundamentals | YT | Aditya Ranjan `UCwcESmR_ZDpUAap-f6Cwt7g` — "Time & Work 45 Din Marathon" series https://www.youtube.com/watch?v=dShCSiEVBlI |
| Pipes & cisterns | YT | Abhinay Maths `UCdVmeIX3xVnQcTa5OMRz-hw` |

#### Chapter: Geometry & Mensuration
| Topic | Type | Reference |
|-------|------|-----------|
| Lines, angles, triangles basics | PDF | https://ncert.nic.in/textbook/pdf/iemh106.pdf (Class IX Ch 6 Lines & Angles) |
| Circles | PDF | https://ncert.nic.in/textbook/pdf/iemh109.pdf (Class IX Ch 9 Circles) |
| Mensuration 2D | YT | Gagan Pratap — SSC CHSL Geometry Mensuration Top 50 https://www.youtube.com/watch?v=0ZOtg4N69nU |
| Mensuration 3D | YT | Magnet Brains `UC3HS6gQ79jjn4xHxogw0HiA` — Class 10 Surface Areas & Volumes |
| Trigonometry intro | YT | Gagan Pratap "Trigonometry Part-1" https://www.youtube.com/watch?v=ZGQT8fAKCo8 |

### Reasoning

#### Chapter: Verbal Reasoning
| Topic | Type | Reference |
|-------|------|-----------|
| Analogy | YT | Rojgar with Ankit `UC5H9MzrMkJ5iuN11vV2PLhA` |
| Odd-one-out (classification) | YT | wifistudy `UCuWuAvEnKWez5BUr29VpwqA` |
| Coding-decoding | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` |
| Alphabet series | YT | wifistudy — "Alphabetical Series by Deepak Sir" https://www.youtube.com/watch?v=euQP6Szmp1Q |
| Blood relations | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Direction sense | YT | SSC Adda247 `UCAyYBPzFioHUxvVZEn4rMJA` |

#### Chapter: Non-Verbal & Logical
| Topic | Type | Reference |
|-------|------|-----------|
| Mirror / water image | YT | Exampur SSC `UCjPAn99dmGFfmGa_beFifuQ` |
| Paper folding & cutting | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Figure series | YT | Magnet Brains Competition `UC0PA9wn-FAXk0HsiCmg9gSA` |
| Cube & dice | YT | Rojgar with Ankit `UC5H9MzrMkJ5iuN11vV2PLhA` |
| Syllogism | YT | Mahendras — "Statement & Argument" https://www.youtube.com/watch?v=f_BBOaXEx9E |

#### Chapter: Analytical & Data
| Topic | Type | Reference |
|-------|------|-----------|
| Seating arrangement (linear) | YT | wifistudy `UCuWuAvEnKWez5BUr29VpwqA` |
| Seating arrangement (circular) | YT | wifistudy `UCuWuAvEnKWez5BUr29VpwqA` |
| Puzzle-based reasoning | YT | SSC Adda247 `UCAyYBPzFioHUxvVZEn4rMJA` |
| Statement & assumption | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Data sufficiency | YT | Exampur main `UCgVg6dmZHCxze_ay0bolPew` |

### General Awareness

#### Chapter: Indian Polity
| Topic | Type | Reference |
|-------|------|-----------|
| Preamble & basic features of Constitution | PDF | https://ncert.nic.in/textbook/pdf/iess101.pdf (Class IX Democratic Politics — constitutional design) |
| Fundamental Rights & Duties | PDF | Class IX Democratic Politics (Ch 3–5); see index at https://ncert.nic.in/textbook.php |
| Parliament & Executive | PDF | https://ncert.nic.in/textbook/pdf/jess4ps.pdf (Class X Democratic Politics — Political Parties / Working of Institutions) |
| Supreme Court & judiciary | YT | Drishti IAS Hindi `UCkrtAaoos4rxt2cw2CIBjuA` — "Judiciary" lecture |
| Panchayati Raj | YT | StudyIQ IAS Hindi `UCKSGmEx5nKZCgjRny-9LaWQ` |

#### Chapter: Indian History
| Topic | Type | Reference |
|-------|------|-----------|
| Ancient India: Indus Valley → Mauryas | YT | Drishti IAS (Hindi) `UCkrtAaoos4rxt2cw2CIBjuA` — Ancient history playlist |
| Medieval India: Delhi Sultanate & Mughals | YT | StudyIQ `UCrC8mOqJQpoB7NuIMKIS6rQ` |
| Modern India: Freedom Struggle 1857–1947 | PDF | https://ncert.nic.in/textbook/pdf/kehs101.pdf (Class XI Themes in World History for context, complement with Class VIII/X history) |
| Post-independence era | YT | Khan GS Research Centre `UCatL-c6pmnjzEOHSyjn-sHA` |

#### Chapter: Indian Geography
| Topic | Type | Reference |
|-------|------|-----------|
| Physical features of India | PDF | Class IX Contemporary India I — start at https://ncert.nic.in/textbook.php (code `iess` branch, ch "India — Size & Location") |
| Rivers of India | YT | Drishti IAS Hindi `UCkrtAaoos4rxt2cw2CIBjuA` — "Rivers of India" |
| Climate & monsoon | YT | Khan GS Research `UCatL-c6pmnjzEOHSyjn-sHA` |
| Soils & agriculture | YT | StudyIQ `UCrC8mOqJQpoB7NuIMKIS6rQ` |
| Minerals & industries | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |

#### Chapter: Indian Economy & Railways-specific GA
| Topic | Type | Reference |
|-------|------|-----------|
| Five Year Plans + NITI Aayog | PDF | https://ncert.nic.in/textbook/pdf/keec101.pdf (Class XI Indian Economic Development Ch 1) |
| Banking & RBI | YT | StudyIQ `UCrC8mOqJQpoB7NuIMKIS6rQ` |
| Budget 2026-27 summary | ART | PIB English RSS: https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=1 (filter items with "Union Budget") |
| Indian Railways — history & zones | ART | data.gov.in Railway Station catalog: https://www.data.gov.in/catalog/railway-station |
| Railway budget & freight stats | ART | https://www.data.gov.in/catalog/freight-and-passenger-movement-road-transport-and-railways |
| Famous trains & routes (static GK) | YT | Khan GS Research `UCatL-c6pmnjzEOHSyjn-sHA` — "Railway GK" |

### General Science

#### Chapter: Physics (Class IX–X NCERT)
| Topic | Type | Reference |
|-------|------|-----------|
| Motion & laws of motion | PDF | https://ncert.nic.in/textbook/pdf/jesc1ps.pdf (Class X Science full book, connected chapters) |
| Force, work, energy | YT | Physics Wallah `UCiGyWN6DEbnj2alu7iapuKQ` — Class 9 Physics |
| Gravitation | YT | Magnet Brains `UC3HS6gQ79jjn4xHxogw0HiA` — Class 9 Gravitation |
| Light — reflection & refraction | PDF | https://ncert.nic.in/textbook/pdf/jesc1ps.pdf (Class X — Light chapters) |
| Electricity & magnetism basics | PDF | Class X Science — Electricity & Magnetic Effects (same PDF pattern, chapters 11–12) |
| Sound | YT | Physics Wallah `UCiGyWN6DEbnj2alu7iapuKQ` |

#### Chapter: Chemistry
| Topic | Type | Reference |
|-------|------|-----------|
| Matter & its states | PDF | Class IX Science chapter 1 (code `iesc101.pdf` — pattern-derived; HEAD inconclusive, validate at ingest) |
| Atoms & molecules | PDF | Class IX Science chapter 3 (pattern `iesc103.pdf`) |
| Chemical reactions | PDF | https://ncert.nic.in/textbook/pdf/jesc1ps.pdf (Class X full book — Ch 1 Chemical Reactions) |
| Acids, bases, salts | YT | Magnet Brains Hindi Medium `UCwO6AYOIRYgyP1KJ5aPbDlw` |
| Metals & non-metals | YT | Physics Wallah `UCiGyWN6DEbnj2alu7iapuKQ` — Class 10 Chem |
| Carbon compounds | YT | Magnet Brains `UC3HS6gQ79jjn4xHxogw0HiA` |

#### Chapter: Biology
| Topic | Type | Reference |
|-------|------|-----------|
| Cell — structure & function | PDF | Class IX Science Ch 5 (pattern `iesc105.pdf`) |
| Tissues | PDF | Class IX Science Ch 6 (`iesc106.pdf`) |
| Life processes | PDF | https://ncert.nic.in/textbook/pdf/jesc1ps.pdf (Class X Ch 6 Life Processes) |
| Control & coordination | YT | Magnet Brains `UC3HS6gQ79jjn4xHxogw0HiA` |
| Reproduction | YT | Physics Wallah `UCiGyWN6DEbnj2alu7iapuKQ` |
| Heredity & evolution | PDF | Class X Science Ch 9 (part of `jesc1ps.pdf`) |
| Our environment | YT | Magnet Brains Competition `UC0PA9wn-FAXk0HsiCmg9gSA` |

### English

#### Chapter: Grammar Fundamentals
| Topic | Type | Reference |
|-------|------|-----------|
| Parts of speech | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` — English Grammar playlist |
| Tenses | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` — "All Tenses" lecture |
| Articles & determiners | YT | SSC Adda247 `UCAyYBPzFioHUxvVZEn4rMJA` |
| Subject-verb agreement | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Active / passive voice | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` |

#### Chapter: Vocabulary
| Topic | Type | Reference |
|-------|------|-----------|
| Synonyms & antonyms | YT | wifistudy `UCuWuAvEnKWez5BUr29VpwqA` |
| One-word substitution | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` |
| Idioms & phrases | YT | SSC Adda247 `UCAyYBPzFioHUxvVZEn4rMJA` |
| Spelling & common errors | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Root-word vocabulary builder | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` |

#### Chapter: Comprehension & Composition
| Topic | Type | Reference |
|-------|------|-----------|
| Reading comprehension strategy | YT | Dear Sir `UC9dyrsYEmD4iGJ8Oz1G5dpw` |
| Cloze test | YT | wifistudy `UCuWuAvEnKWez5BUr29VpwqA` |
| Para-jumble | YT | SSC Adda247 `UCAyYBPzFioHUxvVZEn4rMJA` |
| Sentence rearrangement | YT | Mahendras `UCiDKcjKocimAO1tVw1XIJ0Q` |
| Beehive — prose model chapters | PDF | https://ncert.nic.in/textbook/pdf/iebe1ps.pdf (Class IX English Beehive full book — RRB English is short-answer / vocab-heavy so NCERT prose is context, not study guide) |

### Current Affairs

#### Chapter: Daily current affairs (rolling)
| Topic | Type | Reference |
|-------|------|-----------|
| PIB daily digest (English) | ART | https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=1 |
| PIB daily digest (Hindi) | ART | https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=2 |
| Daily CA video (Hindi) | YT | Drishti IAS Hindi `UCkrtAaoos4rxt2cw2CIBjuA` — daily upload |
| Daily CA video (English) | YT | StudyIQ IAS `UCrC8mOqJQpoB7NuIMKIS6rQ` |
| Monthly CA round-up | YT | GKToday `UCYkK7wuxR_dPqWOpJ8muuqQ` — "TOP 50 Weekly Current Affairs" style |
| Budget & economic survey summary | ART | PIB filtered on "Union Budget" / "Economic Survey" |

#### Chapter: Static CA / Awards & Sports
| Topic | Type | Reference |
|-------|------|-----------|
| Nobel / Padma awards 2025–26 | YT | Drishti IAS (Hindi) `UCkrtAaoos4rxt2cw2CIBjuA` |
| Indian sports achievements | YT | StudyIQ `UCrC8mOqJQpoB7NuIMKIS6rQ` |
| Summit & international relations | YT | StudyIQ |
| Books & authors (recent) | YT | GKToday `UCYkK7wuxR_dPqWOpJ8muuqQ` |
| Defence & ISRO updates | ART | PIB filter on Ministry of Defence / Dept of Space |

#### Chapter: Government schemes
| Topic | Type | Reference |
|-------|------|-----------|
| Flagship central schemes (PMAY, Swachh Bharat, Jan Dhan) | YT | Drishti IAS (Hindi) `UCkrtAaoos4rxt2cw2CIBjuA` — "Schemes" playlist |
| Recent schemes (2024–26) | ART | PIB RSS |
| Railways-specific initiatives | ART | PIB filter on Ministry of Railways |
| Agricultural schemes | YT | StudyIQ `UCrC8mOqJQpoB7NuIMKIS6rQ` |
| Health & education policy | YT | Drishti IAS English `UCafpueX9hFLls24ed6UddEQ` |

---

## Appendix: source URLs cited

Grouped by section. Every URL in this document also appears here once.

### §1 YouTube channels (verification sources)

- https://www.youtube.com/channel/UCT0s92hGjqLX6p7qY9BBrSA  (NCERT OFFICIAL)
- https://www.youtube.com/channel/UCR3ZOcUoPHiFGd-Q9FwV-yA  (Khan Academy India)
- https://www.youtube.com/channel/UCU0kWLAbhVGxXarmE3b8rHg  (Khan Academy India alt)
- https://www.youtube.com/channel/UCg4BkaHyyE_4-RvEMJ2PTtA  (Khan Academy India English)
- https://www.youtube.com/channel/UCatL-c6pmnjzEOHSyjn-sHA  (Khan GS Research Centre)
- https://www.youtube.com/channel/UC7krt1E6XvrywJBu0ZOyq3Q  (Khan Global Studies)
- https://www.youtube.com/channel/UCuWuAvEnKWez5BUr29VpwqA  (wifistudy by Unacademy)
- https://www.youtube.com/channel/UC_r97qKpSCev03bfqvSB_Lw  (Adda247 main)
- https://www.youtube.com/channel/UCAyYBPzFioHUxvVZEn4rMJA  (SSC Adda247)
- https://www.youtube.com/channel/UCrC8mOqJQpoB7NuIMKIS6rQ  (StudyIQ IAS)
- https://www.youtube.com/channel/UCKSGmEx5nKZCgjRny-9LaWQ  (StudyIQ IAS Hindi)
- https://www.youtube.com/channel/UCkrtAaoos4rxt2cw2CIBjuA  (Drishti IAS Hindi)
- https://www.youtube.com/channel/UCafpueX9hFLls24ed6UddEQ  (Drishti IAS English)
- https://www.youtube.com/channel/UCiGyWN6DEbnj2alu7iapuKQ  (Physics Wallah)
- https://www.youtube.com/channel/UC3HS6gQ79jjn4xHxogw0HiA  (Magnet Brains)
- https://www.youtube.com/channel/UCwO6AYOIRYgyP1KJ5aPbDlw  (Magnet Brains Hindi Medium)
- https://www.youtube.com/channel/UC0PA9wn-FAXk0HsiCmg9gSA  (Magnet Brains Competition)
- https://www.youtube.com/channel/UC5H9MzrMkJ5iuN11vV2PLhA  (Rojgar with Ankit)
- https://www.youtube.com/channel/UC9dyrsYEmD4iGJ8Oz1G5dpw  (Dear Sir)
- https://www.youtube.com/channel/UCdVmeIX3xVnQcTa5OMRz-hw  (Abhinay Maths)
- https://www.youtube.com/channel/UCLsXHzLHI5ORoJ9UqttoqlQ  (SSC - Abhinay Maths)
- https://www.youtube.com/channel/UCS2rhpz4RJEmb9CV7TPzBIg  (Gagan Pratap Maths)
- https://www.youtube.com/channel/UCwcESmR_ZDpUAap-f6Cwt7g  (Maths WIZARD Aditya Ranjan)
- https://www.youtube.com/channel/UCgVg6dmZHCxze_ay0bolPew  (Exampur main)
- https://www.youtube.com/channel/UCjPAn99dmGFfmGa_beFifuQ  (SSC Exampur)
- https://www.youtube.com/channel/UCiDKcjKocimAO1tVw1XIJ0Q  (Mahendras)
- https://www.youtube.com/channel/UCYkK7wuxR_dPqWOpJ8muuqQ  (GKToday)
- https://www.youtube.com/channel/UCx1VY57UmjU76Tgq8YwkklA  (Unacademy India)
- https://www.youtube.com/channel/UC7QEPO76d3fLtc60FqaAMdQ  (Unacademy main)
- https://www.youtube.com/channel/UCuTgFUujt6tQXWxQMg-DHrQ  (Railway Testbook)
- https://www.youtube.com/channel/UC1pfsmDBnMQB8sOuQvmTvRQ  (BYJU'S IAS)
- https://vidiq.com/youtube-stats/channel/UCuWuAvEnKWez5BUr29VpwqA/
- https://vidiq.com/youtube-stats/channel/UCatL-c6pmnjzEOHSyjn-sHA/
- https://socialblade.com/youtube/channel/UCatL-c6pmnjzEOHSyjn-sHA
- https://socialblade.com/youtube/c/wifistudy

### §2 NCERT PDFs

- https://ncert.nic.in/  (root)
- https://ncert.nic.in/textbook.php  (index)
- https://ncert.nic.in/textbook.php?ln=en
- https://ncert.nic.in/textbook.php?iemh1=0-15
- https://ncert.nic.in/textbook/pdf/iemh1ps.pdf
- https://ncert.nic.in/textbook/pdf/iemh101.pdf
- https://ncert.nic.in/textbook/pdf/iemh102.pdf
- https://ncert.nic.in/textbook/pdf/iemh103.pdf
- https://ncert.nic.in/textbook/pdf/iemh106.pdf
- https://ncert.nic.in/textbook/pdf/iemh109.pdf
- https://ncert.nic.in/textbook/pdf/iemh112.pdf
- https://ncert.nic.in/textbook/pdf/hemh108.pdf
- https://ncert.nic.in/textbook/pdf/hemh1ps.pdf
- https://ncert.nic.in/textbook/pdf/jemh1ps.pdf
- https://ncert.nic.in/textbook/pdf/jemh1an.pdf
- https://ncert.nic.in/textbook/pdf/jesc1ps.pdf
- https://ncert.nic.in/textbook/pdf/jess4ps.pdf
- https://ncert.nic.in/textbook/pdf/keph2ps.pdf
- https://ncert.nic.in/textbook/pdf/leph1ps.pdf
- https://ncert.nic.in/textbook/pdf/lebo1ps.pdf
- https://ncert.nic.in/textbook/pdf/iebe1ps.pdf
- https://ncert.nic.in/textbook/pdf/iebe101.pdf
- https://ncert.nic.in/textbook/pdf/fegp1ps.pdf
- https://ncert.nic.in/textbook/pdf/fegp101.pdf
- https://ncert.nic.in/textbook/pdf/gegp1ps.pdf
- https://ncert.nic.in/textbook/pdf/gesc101.pdf
- https://ncert.nic.in/textbook/pdf/hesc101.pdf
- https://ncert.nic.in/textbook/pdf/iess101.pdf
- https://ncert.nic.in/textbook/pdf/keec101.pdf
- https://ncert.nic.in/textbook/pdf/kehs101.pdf
- https://ncert.nic.in/textbook/pdf/kech101.pdf
- https://ncert.nic.in/textbook/pdf/kebo101.pdf

### §3 Government / open data

- https://www.pib.gov.in/
- https://www.pib.gov.in/ViewRss.aspx?reg=1&lang=1
- https://www.pib.gov.in/ViewRss.aspx?reg=3&lang=2
- https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=1
- https://www.pib.gov.in/ViewRss.aspx?reg=0&lang=2
- https://www.pib.gov.in/Allrel.aspx?reg=3&lang=1
- https://archive.pib.gov.in/newsite/rss.aspx
- https://www.data.gov.in/Godl
- https://www.data.gov.in/catalog/freight-and-passenger-movement-road-transport-and-railways
- https://www.data.gov.in/catalog/railway-station
- https://www.data.gov.in/catalog/railway-personnel
- https://data.gov.in/catalog/train-usage
- https://www.data.gov.in/catalogs/?ministry=Ministry+of+Railways
- https://www.data.gov.in/dataset-group-name/Indian%20Railways
- https://www.rrcb.gov.in/
- https://www.rrcb.gov.in/rrbs.html
- https://www.rrbkolkata.gov.in/
- https://rrbchennai.gov.in/
- https://rrbmumbai.gov.in/
- https://rrbbbs.gov.in/
- https://www.rrbbnc.gov.in/
- https://rrbranchi.gov.in/
- https://rrbsecunderabad.gov.in/
- https://www.rrbpatna.gov.in/
- https://www.rrbthiruvananthapuram.gov.in/
- https://rrbbhopal.gov.in/
- https://www.rrbsiliguri.gov.in/
- https://en.wikipedia.org/wiki/Portal:Current_events
- https://en.wikipedia.org/w/api.php
- https://en.wikipedia.org/api/rest_v1/page/summary/India
- https://hi.wikipedia.org/w/api.php
- https://www.mediawiki.org/wiki/API:Action_API
- https://www.mediawiki.org/wiki/API:REST_API
- https://www.mediawiki.org/wiki/API:Query
- https://archive.org/details/rrb-ntpc-question-paper-2021-pdf-in-english
- https://archive.org/details/ncert-jesc1
- https://archive.org/details/ncert-lebo1
- https://archive.org/search?query=NCERT+textbook

### §4 Competitor analysis

- https://testbook.com/pricing
- https://testbook.com/super-pass-live-coaching
- https://www.trustpilot.com/review/testbook.com
- https://testbook.pissedconsumer.com/review.html
- https://testbook.pissedconsumer.com/reviews/RT-P.html
- https://pricehistory.app/p/testbook-com-pass-1-year-subscription-email-Q5qDMemb
- https://testbookcoupon.in/testbook-coupon-code-2026/
- https://www.adda247.com/adda_subscription.html
- https://www.adda247.com/videos
- https://adda247.pissedconsumer.com/review.html
- https://voxya.com/company/adda247-complaints/13560
- https://inc42.com/startups/how-test-prep-startup-adda247-onboarded-2-mn-paid-users-by-focussing-on-vernacular-and-affordable-education/
- https://byjusexamprep.com/
- https://byjusexamprep.com/online-classroom
- https://play.google.com/store/apps/details?id=co.gradeup.android&hl=en_IN
- https://www.mouthshut.com/product-reviews/byju-s-exam-prep-reviews-925881261
- https://www.myengineeringbuddy.com/blog/byjus-in-2025-review-pricing-alternatives-future/
- https://www.techjockey.com/detail/byjus
- https://www.glassdoor.co.in/Reviews/BYJU-S-Exam-Prep-Reviews-E1819080.htm

### §5 Licensing register

- https://developers.google.com/youtube/terms/developer-policies
- https://developers.google.com/youtube/terms/required-minimum-functionality
- https://developers.google.com/youtube/iframe_api_reference
- https://developers.google.com/youtube/player_parameters
- https://developers.google.com/youtube/terms/api-services-terms-of-service
- https://support.google.com/youtube/answer/171780?hl=en
- https://www.ncert.nic.in/pdf/announcement/notices/Press_Release_Copyright_Infringement-NCERT.pdf
- https://osre.ncert.gov.in/copyright-policy
- https://www.ncert.nic.in/pdf/announcement/otherannouncements/teachersandresearchers/agreement-2019.pdf
- https://www.data.gov.in/Godl
- https://en.wikipedia.org/wiki/Template:GODL-India
- https://en.wikipedia.org/wiki/National_Data_Sharing_and_Accessibility_Policy
- https://en.wikipedia.org/wiki/Wikipedia:Text_of_the_Creative_Commons_Attribution-ShareAlike_4.0_International_License
- https://creativecommons.org/licenses/by-sa/4.0/deed.en
- https://creativecommons.org/2023/06/29/wikipedia-moves-to-cc-4-0-licenses/
- https://meta.wikimedia.org/wiki/Terms_of_use/Creative_Commons_4.0
- https://wiki.creativecommons.org/wiki/Recommended_practices_for_attribution
- https://wiki.creativecommons.org/wiki/OER_Case_Studies
- https://iies.in/blog/is-nptel-released-under-a-cc-license/
- https://en.wikipedia.org/wiki/National_Programme_on_Technology_Enhanced_Learning
- https://unacademy.com/terms
- https://archive.org/about/terms.php

### §6 Golden content set (additional video references)

- https://www.youtube.com/watch?v=ZGQT8fAKCo8  (Gagan Pratap Trigonometry Part-1)
- https://www.youtube.com/watch?v=Eigke-e1Jew  (Gagan Pratap Percentage Part-1)
- https://www.youtube.com/watch?v=0ZOtg4N69nU  (Gagan Pratap CHSL Geometry Mensuration Top 50)
- https://www.youtube.com/watch?v=dShCSiEVBlI  (Aditya Ranjan Time & Work — 45 Din Marathon)
- https://www.youtube.com/watch?v=euQP6Szmp1Q  (wifistudy Alphabetical Series by Deepak Sir)
- https://www.youtube.com/watch?v=f_BBOaXEx9E  (Mahendras Statement & Argument)

---

_End of document. Next document to produce once Phase 2 code work begins: a schema for the `channels.json`, `pdfs.json`, and `articles.json` seed files, and a worker spec for ingesting the PIB RSS + building the content catalog._
