# Phase 2 — Manual Test Plan

Device: OPPO CPH2491 (ZX9HQCAYO785NVBI), ColorOS. Fresh install of `com.railprep.dev` or
opened post-upgrade. Signed-in account should have `onboarding_complete=true` (profile row).
Do NOT clear app data between steps unless a step explicitly says to.

Legend: **[PASS]** / **[FAIL]** tick each step. Attach a note if behaviour differs from expected.

---

## A. Launch + auth state

1. **Cold-start app.** Splash appears → routes to Home (if session present) or Auth (if not). No crash, no blank frame longer than 1s.
2. **If at Auth:** sign in with the Phase 1 flows — Google or email. Should land at Home with bottom nav visible.
3. **If at Goal first:** complete goal (Phase 1 behaviour). Should land at Home.

## B. Home dashboard (Home tab)

4. "Hey, {name}" renders the profile display name (if set) else email else "friend".
5. "Let's keep the streak going." subtitle visible.
6. **Start learning** CTA visible with book icon. Tap → navigates to Subjects list.
7. **Your bookmarks** CTA visible with bookmark icon. Tap → Bookmarks screen (empty on first run).
8. Back from Subjects returns to Home with Home tab still selected.

## C. Subjects → Chapters → Topic (learning flow)

9. **Subjects screen.** Shows 6 rows: Mathematics / Reasoning / General Awareness / General Science / English / Current Affairs. Each shows EN + HI titles. Tap-target is ≥48dp.
10. **Tap Mathematics.** Chapters screen opens with subject title in top bar. 3 chapter cards: Number System & Simplification, Percentage Profit & Loss, Geometry & Mensuration.
11. **Each chapter card** lists its topics inline (not a separate tap). First chapter has 3 PDF topics; second has 4 (1 YT + 2 PDF + 1 YT); third has 2 YT.
12. **Tap a PDF topic** (e.g. "Number Systems (NCERT Class 9 Ch 1)"). Topic detail screen opens. Shows "downloading" indicator briefly, then renders the NCERT PDF pages scrollably.
13. Attribution footer at bottom: "Source: NCERT — ncert.nic.in. RailPrep links to NCERT's original PDF and does not host a copy."
14. **Scroll PDF.** Pages render inline. Scrolling is smooth.
15. **Back** from PDF topic returns to chapters screen with the same scroll position.
16. **Tap a YouTube topic** (e.g. "Percentage — Part 1 (Gagan Pratap)"). Topic detail opens. YouTube player chrome appears after a beat; shows the video thumbnail. Tap the player's play button — video plays with YouTube's native controls.
17. Attribution footer: "Source: Gagan Pratap Maths on YouTube. Played via the official YouTube IFrame player."
18. **Bookmark toggle** (top right): tap the bookmark outline icon → fills. Leave and come back → still filled.

## D. Bookmarks

19. **Home → Your bookmarks** (or Profile → Bookmarks). The topic from step 18 appears in the list with its source subtitle.
20. **Tap the bookmarked topic.** Topic detail opens with the same content as before.
21. **Unbookmark** on the topic detail. Go back to Bookmarks — the topic is no longer listed.
22. When bookmarks list is empty, the empty state shows a bookmark icon + "No bookmarks yet / Tap the bookmark icon on any topic to save it here."

## E. Tests + Feed placeholders

23. **Tap Tests tab** in bottom nav. "Mock tests are coming in Phase 3." + subtitle visible with a quiz icon. No crash, no empty stack.
24. **Tap Feed tab.** "Current affairs feed lands in Phase 4." + subtitle visible with an article icon.

## F. Profile

25. **Tap Profile tab.** Header shows avatar (Person icon circle), display name, email.
26. **Profile rows:** Edit profile · Bookmarks · Language · About & attributions · Sign out. All have left icon + right chevron.
27. **Tap Edit profile.** Form pre-fills with current name + daily minutes slider. Change name, tap Save. Snackbar "Saved." shows; back-nav returns to Profile tab. Next time you open Edit, the new value is there.
28. **Tap Language.** Dialog shows English + हिन्दी with the current selection radio-filled. Pick Hindi → dialog closes → app UI re-renders in Hindi within ~1 second.
29. **Open Subjects again.** Subject titles are now the Hindi versions (गणित / तर्कशक्ति / सामान्य ज्ञान / …). Chapter titles also Hindi. Topic attribution footer in Hindi.
30. **Switch back to English** via Profile → Language. UI returns to English.
31. **Tap About & attributions.** Intro paragraph + Content sources section (NCERT / YouTube / PIB / data.gov.in bullets) + Licenses section. All visible, scrollable if needed.

## G. Diagnostics (long-press Profile avatar)

32. **Profile tab → long-press the avatar circle.** Diagnostics screen opens. Shows: app version, user ID, PDF cache size in KB.
33. **Tap "Clear PDF cache".** Snackbar "Cache cleared." appears. PDF cache size becomes 0 KB.
34. **Open a PDF topic again.** Downloads fresh (loading spinner), then renders.

## H. Sign-out

35. **Profile → Sign out.** Returns to Splash → Auth (since there is no session). Re-sign-in works.

## Regressions to check (Phase 1 still works)

36. Phase 1 Google sign-in still succeeds.
37. Phase 1 email signup + signin still succeed.
38. Password reset OTP flow still works end-to-end.

---

## Known limitations for this demo seed

- English chapter has only 1 topic (Class 9 Beehive PDF); Current Affairs chapters have 0 topics (empty state shows). These fill in once the content pipeline runs with a real YouTube API key.
- Reasoning "Analytical & Data" chapter has 0 topics for the same reason.
- Tests + Feed tabs are intentional placeholders — Phase 3/4 work.
- Profile edit only surfaces name + daily minutes. Exam target / qualification / category / DOB are persisted from Phase 1 onboarding; full edit is Phase 3 work.
