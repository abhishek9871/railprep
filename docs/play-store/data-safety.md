# RailPrep Play Data Safety

Use this as the Play Console Data Safety answer key for the first internal testing upload.

## Data Collection

RailPrep collects:

- Email address: account creation, authentication, progress restore.
- Name/profile basics from Google sign-in when the user chooses Google login.
- App activity: test attempts, answers, scores, bookmarks, saved questions, notes, streaks, language choice and notification preference.
- Diagnostics: short-lived error logs without email, phone number or free-text notes.
- Purchase status when Google Play Billing subscriptions are enabled.

RailPrep does not collect:

- Precise or approximate location.
- Contacts.
- SMS or call logs.
- Photos, videos or audio recordings.
- Advertising ID.
- Payment-card data.
- Health, fitness or financial account data.

## Sharing

No user data is sold. Data is processed by Supabase for backend storage/auth and by Google Play for app distribution and billing. Google sign-in is used only when the user chooses it.

## Security

- Data is encrypted in transit using HTTPS/TLS.
- Supabase stores backend data with platform security controls.
- Postgres Row Level Security restricts user-owned records to the authenticated user.
- Users can request account deletion through the in-app Privacy & account screen.

## User Controls

- Account deletion: Profile > Privacy & account > Delete my account.
- Data export: Profile > Privacy & account > Download my data.
- Notifications: Profile > Daily reminder toggle.
- Language: Profile > Language.

## Children

RailPrep is not directed at children under 13. It targets RRB NTPC exam aspirants.
