# Compliance & Policy Checklist

**⚠ This is a working reference, not legal advice.** Before launch, have an Indian lawyer review: privacy policy, terms of service, subscription terms, refund policy, and data-handling practices.

## India DPDP Act 2023 (Digital Personal Data Protection)

### Key obligations
- [ ] **Notice & consent:** On signup, show a clear notice of what data you collect and why. Consent must be free, specific, informed, unconditional, unambiguous.
- [ ] **Purpose limitation:** Use data only for stated purposes. Don't quietly repurpose.
- [ ] **Minors:** Since some users are 18+, collect DOB at onboarding and block under-18 accounts OR require verifiable parental consent. The prototype's goal-setup screen should validate age ≥ 18.
- [ ] **Data principal rights:** Users can (a) access their data, (b) correct it, (c) erase it, (d) nominate a representative, (e) file grievances. Implement in Profile → Settings.
- [ ] **Data Protection Officer:** If >registered-threshold users (likely applies to you once you scale), appoint a DPO and publish contact.
- [ ] **Breach notification:** Report personal data breaches to the Data Protection Board within the prescribed window.
- [ ] **Localization:** Personal data can be transferred outside India only to notified countries. Keeping everything in Firebase asia-south1 (Mumbai) is the safe default.
- [ ] **Grievance redressal:** Publish grievance officer email in-app and on the website.

## Google Play policies

- [ ] **Payments:** Digital goods (Pro subscription) traditionally required Google Play Billing. Per recent Indian regulator action, third-party billing (Razorpay) is permitted for Indian users, but **use Play Billing as the default** and disclose alternates clearly. Check current policy at submission time.
- [ ] **Permissions:** Request only what you use. The app here needs: INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS (Android 13+), READ_EXTERNAL_STORAGE / scoped storage for PDF downloads, and NOTHING ELSE unless justified.
- [ ] **Background location:** Not needed. Don't request.
- [ ] **Ads:** If you add ads (not planned for launch), disclose ID usage and respect Family policy even though target audience is 18+.
- [ ] **Deceptive behavior:** Don't auto-opt users into marketing notifications — let them choose.
- [ ] **Prominent disclosure:** For phone number at signup, show "We'll use this to log you in. Message/data rates may apply." upfront.
- [ ] **Account deletion:** Google requires in-app deletion path AND a web URL. Implement both.

## In-app subscriptions rules

- [ ] Clearly state the recurring price, billing period, renewal date.
- [ ] Make cancellation as easy as signup (Profile → Manage Subscription).
- [ ] Free trials (if offered): end date visible; no surprise charges.
- [ ] Refund policy published on website and linked in app.
- [ ] GST invoice emailed on purchase. (Razorpay can generate these.)

## Children & education

- Minimum age 18 enforced at signup. Do not onboard minors — the content is exam-specific for adult aspirants.

## Content & IP

- Never reproduce official RRB question papers verbatim without written permission. PYQ content must be rewritten/tagged-and-referenced (fair use for educational purposes is narrow in India; consult lawyer).
- Don't use Railway Recruitment Board logos, Indian Railways logos, or government insignia in your branding — risk of misleading users.
- All imagery (illustrations, stock photos) must be licensed. Budget ₹5-10K/mo for a Canva Pro + Shutterstock subscription, or use open licenses (Unsplash, Pexels).

## Razorpay KYC (separate track)

- Register a Pvt Ltd (recommended) or LLP
- PAN, GST (if turnover > ₹20L), bank account in business name
- Merchant settlement account
- 4-6 week lead time; start in week 1 of the build

## Accessibility

- WCAG 2.1 AA targets: 4.5:1 contrast for body text, 3:1 for large. Design tokens respect this.
- TalkBack compatible — every interactive element has contentDescription.
- Text scales to 200% without clipping.
- No flashing content (seizure risk).
- Captions on all instructional videos.
