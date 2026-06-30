# Ledgerly Privacy Policy

**Last updated: June 30, 2026**

> **Template notice:** This document is a comprehensive starting-point template prepared for Ledgerly's Google Play Store submission. It is **not legal advice**. Before publishing the app or this policy, have it reviewed by a qualified attorney familiar with applicable privacy law (including U.S. state laws such as the CCPA/CPRA, the EU/UK GDPR, and Google Play Developer Program requirements) to ensure it accurately reflects your actual data practices and complies with all laws applicable to your business and users.

---

## 1. Introduction

Ledgerly Software ("**Ledgerly**," "**we**," "**us**," or "**our**") provides the Ledgerly mobile application ("**Ledgerly**," the "**App**," or the "**Service**"), a business expense tracker designed to help 1099 contractors, freelancers, consultants, and small business owners track business expenses and prepare a U.S. IRS Schedule C return.

This Privacy Policy explains what information we collect, how we use and protect it, who we share it with, and the choices and rights available to you. By downloading, installing, or using Ledgerly, you agree to the collection and use of information as described in this Privacy Policy.

If you do not agree with this Privacy Policy, please do not use the App.

---

## 2. Information We Collect

We collect the following categories of information:

### 2.1 Account Information
When you sign up or sign in, we collect:
- Your name and email address (via Firebase Authentication and/or Google Sign-In)
- A unique account/user identifier
- Authentication tokens necessary to keep you signed in

We do not receive or store your Google account password. Authentication is handled securely by Firebase Authentication and Google's own sign-in infrastructure.

### 2.2 Expense and Financial Data
To provide the App's core functionality, we collect information you enter or import, including:
- Expense records (amount, date, merchant/vendor name, category, payment method, notes)
- Income and business details you choose to enter for Schedule C preparation
- Mileage logs and trip details you enter or that are calculated by the App
- Optional client or business contact information you manually type into the App (e.g., a client name or business you associate with an expense)

### 2.3 Receipt Images
If you use the camera or photo import features, we collect:
- Photos of receipts you capture with your device camera
- Receipt images you select from your device's photo library (via the Photos/Media permission)
- Data extracted from receipts (e.g., merchant name, amount, date) if optical character recognition (OCR) is used

### 2.4 Location Data (Optional)
If you opt in to mileage/GPS tracking features, we collect:
- Precise device location (latitude/longitude) while you are actively logging a trip
- Calculated trip distance and route information derived from location data

Location data is **only** collected when you explicitly enable mileage tracking. You may use Ledgerly without ever granting location access; manual mileage entry remains available.

### 2.5 Device and Usage Information
We automatically collect limited technical information to operate and improve the App, including:
- Device model, operating system version, and language/locale settings
- App version, install identifiers, and general usage events (e.g., screens viewed, features used)
- IP address (processed transiently by our infrastructure and service providers; not used to precisely track your location)

### 2.6 Crash and Diagnostic Data
We use Firebase Crashlytics and Firebase Analytics to collect:
- Crash logs, stack traces, and error reports
- Performance and stability metrics
- Aggregated, non-identifying usage statistics to help us fix bugs and improve the App

---

## 3. How We Use Information

We use the information described above to:

- Create and manage your Ledgerly account
- Provide core App functionality: recording expenses, capturing receipts, tracking mileage, generating reports, and preparing Schedule C summaries
- Sync and store your receipt images and generated spreadsheets in **your own** Google Drive and Google Sheets account, at your direction
- Authenticate you securely and protect your account (PIN lock, biometric lock)
- Diagnose, debug, and fix crashes and technical issues
- Understand aggregate usage trends to improve features and user experience
- Send you transactional notifications (e.g., reminders to log expenses, sync confirmations) via push notifications, where permitted
- Respond to support requests sent to support@ledgerly.app
- Comply with legal obligations and enforce our Terms of Service

We do **not** use your expense data, receipt contents, or financial information to serve third-party advertising, and we do not build advertising profiles based on your financial records.

---

## 4. Google API Services & Limited Use Disclosure

Ledgerly integrates with the **Google Sheets API** and **Google Drive API** to let you export and store your expense spreadsheets and receipt images directly in your own Google Drive/Google Sheets account, using OAuth 2.0 with your explicit, scope-limited consent.

Ledgerly's use and transfer of information received from Google APIs adheres to the **[Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy)**, including the **Limited Use** requirements. Specifically:

- We only request the minimum Google Drive/Sheets scopes necessary to create, read, and update files that the App itself creates on your behalf (e.g., a per-app data folder and your generated expense spreadsheet).
- Data accessed via Google APIs is used **solely** to provide and improve user-facing features within Ledgerly (storing receipts and generating your spreadsheet).
- We do **not** use data obtained through Google APIs for advertising purposes.
- We do **not** allow humans to read this data except: (a) with your affirmative consent for a specific purpose (e.g., a support request you initiate), (b) for security purposes such as investigating abuse, (c) to comply with applicable law, or (d) where the data has been aggregated and anonymized.
- We do **not** transfer Google user data to third parties except as necessary to provide or improve the App's features (e.g., essential cloud infrastructure providers, subject to confidentiality obligations), to comply with law, or as part of a merger/acquisition as described in Section 6.
- You can revoke Ledgerly's access to your Google Account at any time via your [Google Account permissions page](https://myaccount.google.com/permissions).

---

## 5. Data Storage & Security

### 5.1 Where Your Data Lives
Ledgerly is designed with a privacy-first, "your data stays yours" architecture:

- **On-device storage:** Expense records, receipt thumbnails, and app settings are stored locally on your device in an encrypted database (SQLCipher) and encrypted preference storage (Android `EncryptedSharedPreferences`).
- **Your Google account:** Full-resolution receipt images and your generated expense spreadsheet are stored in **your own** Google Drive and Google Sheets, under your Google account — not on Ledgerly's servers. We do not retain copies of these files on our infrastructure beyond what is technically required to facilitate the sync (e.g., transient processing).
- **Account and diagnostic data:** Authentication metadata, crash reports, and aggregated analytics are processed and stored by Firebase (Google Cloud infrastructure) on our behalf.

### 5.2 Security Measures
We employ industry-standard safeguards, including:

- **Encryption at rest:** Local databases are encrypted using SQLCipher (AES-256); sensitive preferences use Android's `EncryptedSharedPreferences`.
- **Encryption in transit:** All network communication uses TLS (HTTPS).
- **App lock:** Optional PIN code and biometric (fingerprint/face) authentication via Android's `BiometricPrompt`/`USE_BIOMETRIC` to restrict in-app access.
- **OAuth-scoped access:** Google Drive/Sheets access uses narrowly scoped OAuth tokens rather than broad account access, and tokens are stored securely on-device.
- **Access controls:** Role-based access controls and audit logging restrict employee access to backend systems.

No method of electronic storage or transmission is 100% secure. While we strive to protect your information, we cannot guarantee absolute security.

---

## 6. Data Sharing

**We do not sell your personal information.** We do not share your expense data, receipts, or financial records with advertisers or data brokers.

We share information only with the following categories of recipients, each acting as a service provider/data processor under contractual confidentiality and data protection obligations:

| Recipient | Purpose | Data Involved |
|---|---|---|
| **Google Firebase** (Authentication, Crashlytics, Analytics, Cloud Messaging) | Account sign-in, crash reporting, push notifications, aggregated usage analytics | Account identifiers, device/usage data, crash logs |
| **Google Drive API / Google Sheets API** | Storing receipt images and generated spreadsheets in your own Google account, at your direction | Receipt images, expense spreadsheet data (stored in your Google account, not ours) |
| **Cloud infrastructure providers** | Hosting backend services that support app functionality | Encrypted account and app metadata as required for operation |

We may also disclose information if required to:
- Comply with a legal obligation, subpoena, or governmental request
- Protect the rights, property, or safety of Ledgerly, our users, or the public
- Investigate fraud, security incidents, or violations of our Terms of Service
- Facilitate a merger, acquisition, financing, or sale of business assets (you will be notified of any change in ownership or use of your data)

---

## 7. Your Rights

Depending on your location, you may have rights regarding your personal information, including the right to:

- **Access** the personal data we hold about you
- **Export** your data (Ledgerly natively supports exporting your expenses to your own Google Sheets/Drive at any time, and you may request a copy of your account data from us)
- **Correct** inaccurate or incomplete data
- **Delete** your account and associated data (see Section 10)
- **Restrict or object to** certain processing of your data
- **Withdraw consent** (e.g., revoke location permission or Google Drive access) at any time without affecting the lawfulness of prior processing

### California Residents (CCPA/CPRA)
If you are a California resident, you have the right to know what personal information we collect, request deletion of your personal information, and not be discriminated against for exercising these rights. As stated above, **we do not sell or "share" (as defined under the CPRA) your personal information** for cross-context behavioral advertising.

### European Economic Area, UK, and Other Jurisdictions (GDPR)
If you are located in the EEA, UK, or another jurisdiction with similar data protection laws, you have rights under the General Data Protection Regulation (GDPR) or equivalent local law, including the rights listed above and the right to lodge a complaint with your local data protection authority. Our legal bases for processing include your consent, performance of a contract (providing the App's services), and our legitimate interests in operating and securing the App.

To exercise any of these rights, contact us at **privacy@ledgerly.app**.

---

## 8. Children's Privacy

Ledgerly is not directed to, and is not intended for use by, children under the age of 13 (or the applicable minimum age in your jurisdiction). We do not knowingly collect personal information from children. If we learn that we have collected personal information from a child without verified parental consent, we will take steps to delete that information promptly. If you believe a child has provided us with personal information, please contact us at privacy@ledgerly.app.

---

## 9. Permissions Explained

Ledgerly requests the following Android permissions. Each is used only for the stated purpose, and most are optional:

| Permission | Why Ledgerly Uses It |
|---|---|
| **CAMERA** | To let you photograph receipts directly within the App for expense documentation. |
| **ACCESS_FINE_LOCATION** *(optional, opt-in)* | To automatically calculate mileage and trip distance when you enable GPS-based mileage tracking. You can use manual mileage entry instead. |
| **INTERNET** | To sync data with Firebase services and your Google Drive/Sheets account. |
| **POST_NOTIFICATIONS** | To send optional reminders (e.g., to log expenses or confirm a successful sync). |
| **READ_MEDIA_IMAGES** | To let you import existing receipt photos from your device's gallery instead of only using the camera. |
| **USE_BIOMETRIC** | To enable optional fingerprint/face unlock for the in-app PIN/biometric lock feature, protecting your data if your device is shared or lost. |

You can review and revoke any permission at any time in your device's **Settings > Apps > Ledgerly > Permissions**. Revoking a permission may limit related features but will not prevent you from using the rest of the App.

---

## 10. Data Retention

- **Account and expense data** are retained for as long as your account remains active, or as needed to provide you the Service.
- **Receipt images and spreadsheets** stored in your own Google Drive/Sheets remain under your control and Google's retention policies; deleting them in Google Drive removes them from your Google account independent of the App.
- **Crash and diagnostic logs** are retained by Firebase Crashlytics/Analytics for a limited period (typically up to 90 days for raw crash data, longer for aggregated/anonymized statistics) to support debugging and product improvement.
- Upon account deletion (Section 11), we delete or anonymize your personal data within our systems within 30 days, except where retention is required to comply with legal, tax, accounting, or fraud-prevention obligations.

---

## 11. Account Deletion

You can delete your Ledgerly account and associated data at any time:

1. **In-app:** Go to **Settings > Account > Delete Account**, confirm your identity, and follow the prompts. This permanently deletes your account profile, locally stored expense data, and synced metadata held by Ledgerly.
2. **By email:** Send a deletion request to **support@ledgerly.app** from your registered email address, and we will process it within 30 days.

Please note:
- Files already saved to **your own** Google Drive/Sheets (receipts, exported spreadsheets) are **not** automatically deleted by Ledgerly, because they live in your Google account, not ours. You may delete them directly in Google Drive, or revoke Ledgerly's Google account access at [myaccount.google.com/permissions](https://myaccount.google.com/permissions).
- Some information may be retained in backups or as required by law (e.g., tax/financial recordkeeping obligations) for a limited period after deletion.

---

## 12. Third-Party Services

Ledgerly relies on the following third-party services, each governed by its own privacy policy:

- **Google Firebase** (Authentication, Crashlytics, Analytics, Cloud Messaging) — [https://firebase.google.com/support/privacy](https://firebase.google.com/support/privacy)
- **Google Sign-In** — [https://policies.google.com/privacy](https://policies.google.com/privacy)
- **Google Drive API / Google Sheets API** — [https://policies.google.com/privacy](https://policies.google.com/privacy)
- **Google Play Services** — [https://policies.google.com/privacy](https://policies.google.com/privacy)

We encourage you to review these third parties' privacy policies to understand how they handle data.

---

## 13. International Users

Ledgerly is operated from the United States. If you access the App from outside the United States, your information may be transferred to, stored, and processed in the United States or other countries where our service providers (including Google/Firebase) operate data centers. By using the App, you consent to this transfer, in accordance with applicable data protection safeguards (such as Standard Contractual Clauses, where required).

---

## 14. Changes to This Policy

We may update this Privacy Policy from time to time to reflect changes in our practices, technology, legal requirements, or for other operational reasons. We will post the updated policy in the App and/or on our website with a revised "Last updated" date. For material changes, we will provide additional notice (such as an in-app notification or email) where required by law. Your continued use of the App after changes take effect constitutes acceptance of the updated policy.

---

## 15. Contact Us

If you have questions, concerns, or requests regarding this Privacy Policy or your personal data, please contact us:

**Ledgerly Software**
General support: **support@ledgerly.app**
Privacy inquiries / data requests: **privacy@ledgerly.app**

---

*This Privacy Policy is a template prepared for development and Google Play Store submission purposes. Ledgerly Software should have this document reviewed and finalized by qualified legal counsel prior to publishing the App or this policy to ensure full compliance with all applicable laws.*
