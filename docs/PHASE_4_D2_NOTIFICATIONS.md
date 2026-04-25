# Phase 4 D2 — notifications decision

**Picked: on-device WorkManager `PeriodicWorkRequest` (24h), no FCM, no server scheduler.**

Why (re-affirming the D2 plan agreed on before coding):
- Supabase project has no `pg_cron`, `pg_net`, or `http` extensions installed → no way to push FCM from Postgres.
- Adding FCM means a Firebase project, `google-services.json`, `firebase-messaging` dep, server-side token registration, and a server that can actually send pushes. Three of those four we don't have today.
- The digest reminder is a single, client-owned, best-effort nudge. WorkManager's periodic scheduling fits: survives reboot (WM's JobScheduler persistence), respects Doze, no network required (local notification).
- Known OEM fragility: Oppo/Xiaomi occasionally drop WM periodic jobs under aggressive battery settings. OPPO CPH2491 is our dev device — confirmed in `railprep_dev_device_revanced.md`. Mitigation: the Home card's streak chip is the primary visible signal; the reminder is just icing.

If Phase 5+ ever needs server-driven push (cross-device sync of reminder state, engagement experiments), that's when FCM gets scoped. Not now.

## Implementation locations

- `feature/feature-notifications/.../DigestReminderScheduler.kt` — `.enable()` enqueues `PeriodicWorkRequest<DigestReminderWorker>` with a computed initial delay to next 20:00 IST (`computeInitialDelayMs` handles the same-day-already-past-8pm case by pushing to +1d). `.disable()` cancels. Unique name `digest-reminder`, `ExistingPeriodicWorkPolicy.KEEP`.
- `feature/feature-notifications/.../DigestReminderWorker.kt` — silent no-op when any of: notifications_enabled=false, digest_attempts row exists for today IST, `POST_NOTIFICATIONS` not granted. Hilt via `EntryPointAccessors.fromApplication(...Deps::class.java)` — same pattern as `AutoSubmitWorker` from Phase 3 Part B.
- `feature/feature-notifications/.../NotificationsOptInPrompt.kt` — the AlertDialog that owns the system permission request via `rememberLauncherForActivityResult(RequestPermission())`.
- Migration `0010_notifications_pref` — one column: `profiles.notifications_enabled boolean not null default false`. No FCM token column.

## POST_NOTIFICATIONS prompt is gated behind first-ever digest submit

**Never at app launch.** The only trigger points are:

1. **First-ever digest submit** (`DailyResultsViewModel.load` computes `isFirstSubmit = attempt != null && profile.notificationsEnabled == false && profile.streakBest == 1 && profile.streakCurrent == 1`). `streakBest == 1 && streakCurrent == 1` means the submit that just happened is what pushed them to their all-time-best streak of 1 — true exactly once per user-account, ever. Subsequent streak resets don't re-fire.
2. **Manual opt-in from Profile > Daily reminder toggle** — only when the user actively flips the switch to ON, and only if the permission isn't already granted. Toggling OFF never triggers a system prompt.

Neither path runs at splash/login/onboarding/home-landing. Verified by absence of any call-site outside those two files:

```
$ grep -r "NotificationsOptInPrompt\|RequestPermission.*POST_NOTIFICATIONS" feature/ app/
feature/feature-daily/.../results/DailyResultsScreen.kt    — first-submit trigger
feature/feature-home/.../profile/ProfileTab.kt             — manual Profile toggle
feature/feature-notifications/.../NotificationsOptInPrompt.kt — the dialog itself
```

On the user device (OPPO CPH2491, Android 15): the Android system `POST_NOTIFICATIONS` dialog appears only after the user taps "Turn on" inside our in-app dialog, and only after they've submitted their first-ever digest. `DailyDigestViewModel` log tag `RailPrepDaily` emits `digest-submit-success` one frame before the dialog mounts, proving temporal ordering.
