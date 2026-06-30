# Google Sheets Setup

Ledgerly syncs every expense to a Google Sheet that lives in the signed-in user's own Google Drive — there is no Ledgerly-owned backend storing expense data. This document covers provisioning the Google Cloud project and OAuth credentials that make that possible, the spreadsheet schema, and how the app keeps the Sheet consistent with the local (Room/SQLCipher) database.

See also: [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md) (Google Sign-In / Firebase Auth, which this flow builds on) and [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md) (receipt image storage, using the same OAuth client).

## Contents

- [1. Create or reuse a Google Cloud project](#1-create-or-reuse-a-google-cloud-project)
- [2. Enable the Google Sheets API](#2-enable-the-google-sheets-api)
- [3. Configure the OAuth consent screen](#3-configure-the-oauth-consent-screen)
- [4. OAuth clients](#4-oauth-clients)
- [5. Required scopes](#5-required-scopes)
- [6. Incremental authorization](#6-incremental-authorization)
- [7. Spreadsheet schema](#7-spreadsheet-schema)
- [8. How rows map to expenses](#8-how-rows-map-to-expenses)
- [9. Conflict resolution & idempotency](#9-conflict-resolution--idempotency)
- [10. Rate limits & batching](#10-rate-limits--batching)
- [11. Testing & publishing the OAuth app](#11-testing--publishing-the-oauth-app)
- [Troubleshooting](#troubleshooting)

## 1. Create or reuse a Google Cloud project

Firebase projects **are** Google Cloud projects under the hood, so if you already completed [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md), you can reuse the same project and skip to [Section 2](#2-enable-the-google-sheets-api).

To reuse the Firebase project:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. In the project picker (top bar), select the same project ID shown in Firebase Console under **Project settings > General > Project ID** (e.g. `ledgerly-prod`).

To create a separate project instead:
1. Go to **Google Cloud Console > New Project**.
2. Name it (e.g. `ledgerly-prod`) and create it.
3. If you want this project linked to Firebase too, do so from **Firebase Console > Add project > select an existing Google Cloud project**.

## 2. Enable the Google Sheets API

1. In Google Cloud Console, go to **APIs & Services > Library**.
2. Search for **Google Sheets API**.
3. Click it, then click **Enable**.
4. (You will also enable the **Google Drive API** here — see [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md#1-enable-the-google-drive-api) — since Ledgerly uses Drive both to create/locate the spreadsheet file and to store receipt images.)

## 3. Configure the OAuth consent screen

1. Go to **APIs & Services > OAuth consent screen**.
2. Choose **User Type: External** (Ledgerly is installed by users outside your Google Workspace organization). Internal is only viable if every user is in the same Workspace org, which doesn't apply to a consumer app.
3. Fill in the app registration:
   | Field | Value |
   |---|---|
   | App name | `Ledgerly` |
   | User support email | your support address |
   | App logo | Ledgerly icon (recommended for production verification) |
   | Application home page | your marketing/landing page |
   | Application privacy policy | required for verification — must describe Sheets/Drive data use |
   | Application terms of service | recommended |
   | Authorized domains | the domain hosting your privacy policy/home page |
   | Developer contact email | your team's email |
4. On the **Scopes** step, click **Add or remove scopes** and add:
   - `https://www.googleapis.com/auth/spreadsheets` — read/write access to Sheets the app creates or that the user opens with the app.
   - `https://www.googleapis.com/auth/drive.file` — per-file Drive access, scoped to files the app creates or that the user explicitly picks (used both for the spreadsheet file and the receipts folder; see [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md#2-the-drivefile-scope-and-why-its-least-privilege)).
   - Standard `openid`, `.../auth/userinfo.email`, `.../auth/userinfo.profile` (added automatically by Firebase/Google Sign-In; confirm they're present).
5. On the **Test users** step (while in "Testing" publishing status), add the Google accounts (including your own) that will sign in during development — see [Section 11](#11-testing--publishing-the-oauth-app).
6. Save.

> `spreadsheets` and `drive.file` are both classified by Google as **sensitive scopes**, not restricted scopes, which simplifies verification (no security assessment required) but still requires the standard OAuth verification process before more than 100 test users can use the app — see [Section 11](#11-testing--publishing-the-oauth-app).

## 4. OAuth clients

Create two OAuth 2.0 client IDs under **APIs & Services > Credentials > Create Credentials > OAuth client ID**. Both are required and serve different purposes.

### Android client

1. Application type: **Android**.
2. Name: `Ledgerly Android (debug)` / `Ledgerly Android (release)` (create one per signing key, same as in Firebase — debug keystore, release/upload keystore, and Play App Signing key if applicable).
3. Package name: `com.ledgerly.expense`.
4. SHA-1 certificate fingerprint: same value(s) registered in [`FIREBASE_SETUP.md` Section 4](FIREBASE_SETUP.md#4-add-sha-1--sha-256-fingerprints) (`./gradlew signingReport` or `keytool`).
5. Create. This client authorizes the *app binary itself* to perform the OAuth flow on the device; it does not require a client secret (Android clients are public clients using PKCE under the hood via the Credential Manager / Google Sign-In SDK).

### Web client

1. Application type: **Web application**.
2. Name: `Ledgerly Web (Sign-In/idToken)`.
3. No redirect URIs are required for the basic Google Sign-In `idToken` use case Ledgerly relies on.
4. Create.

This Web client ID is the one referenced as `default_web_client_id` (auto-populated into `google-services.json` as the `client_type: 3` entry once both this Web client and the Android client share the same Firebase/Cloud project — see [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md#3-download-and-place-google-servicesjson)). It is what gets passed to:

- `GoogleSignInOptions.Builder(...).requestIdToken(webClientId)` (legacy Google Sign-In API), or
- the Credential Manager `GetSignInWithGoogleOption.Builder(serverClientId)` (current recommended API),

so that Firebase Auth can verify the user's identity **and** so the app can subsequently request offline/incremental Sheets & Drive scopes tied to the same Google account.

> Why two clients? The **Android client** is what Google validates the calling app against (package name + signing cert) when issuing the sign-in result. The **Web client** is the audience (`aud` claim) embedded in the resulting ID token, which is what Firebase Auth (a server-side system) verifies. You need both even though only one app binary exists.

## 5. Required scopes

| Scope | Used for |
|---|---|
| `openid`, `.../auth/userinfo.email`, `.../auth/userinfo.profile` | Firebase Auth sign-in identity (name, email, avatar) |
| `https://www.googleapis.com/auth/spreadsheets` | Create the Ledgerly tracking spreadsheet; append/update/read expense rows |
| `https://www.googleapis.com/auth/drive.file` | Create/locate the "Ledgerly Receipts" Drive folder and the spreadsheet file; upload receipt images; generate shareable links (see [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md)) |

Ledgerly intentionally avoids the broader `https://www.googleapis.com/auth/drive` scope — `drive.file` cannot see or touch any file in the user's Drive that the app didn't itself create or that the user didn't explicitly open with the app via the Drive picker. This is both a privacy commitment and a requirement for smoother OAuth verification (broad `drive` scope is "restricted," triggering a costly third-party security assessment; `drive.file` is merely "sensitive").

## 6. Incremental authorization

Sign-in (Firebase Auth identity) and Sheets/Drive data access are requested **separately**, at the point of need, rather than all at once at first launch:

1. **At sign-in**, the app requests only the identity scopes (`openid`/`email`/`profile`) via Credential Manager / Google Sign-In, sufficient to create the Firebase Auth session and let the user use the app fully offline (local Room/SQLCipher storage, no network required).
2. **The first time sync is enabled** (either the user explicitly turns on "Sync to Google Sheets" in Settings, or the first WorkManager sync attempt is about to run), the app requests the additional `spreadsheets` and `drive.file` scopes using the Android Authorization API (`AuthorizationClient` /  `Identity.getAuthorizationClient()`) or the legacy `GoogleSignInClient.requestScopes()`/`startActivityForResult` consent flow.
3. Google shows an incremental consent screen listing only the **newly requested** scopes — the user isn't re-asked to grant access already approved.
4. The resulting `Authorization` result yields an authorized `GoogleAccountCredential` (or OAuth access/refresh token pair) used to construct the `Sheets` and `Drive` API client objects (`com.google.api.services.sheets.v4.Sheets`, `com.google.api.services.drive.v3.Drive`).
5. If the user later revokes access (Google Account > Security > Third-party access) or the refresh token becomes invalid, the sync worker catches the resulting `UserRecoverableAuthIOException` / `4xx` from the API, surfaces a "Reconnect Google Sheets" prompt, and re-runs the incremental authorization flow rather than forcing a full re-sign-in.

This means a user can use Ledgerly entirely offline and never grant Sheets/Drive access at all, and guest-mode (anonymous Firebase Auth) users never see a Google consent screen until/unless they sign in with Google and opt into sync.

## 7. Spreadsheet schema

The first time sync is enabled, the app creates a spreadsheet named **"Ledgerly Expenses"** in the user's Drive (inside the "Ledgerly" app folder structure — see [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md)) with a single sheet/tab named `Expenses`, with a frozen header row:

| Column | Header | Type / format | Notes |
|---|---|---|---|
| A | `Transaction ID` | string (UUID) | Stable primary key, matches `expenses.id` in Room. Never regenerated. |
| B | `Date` | date (`yyyy-MM-dd`) | Expense transaction date (not creation date). |
| C | `Merchant` | string | Vendor/payee name. |
| D | `Amount` | currency (number) | Written as a decimal (e.g. `42.50`), converted from the local integer-cents value. |
| E | `Expense Category` | string | User-facing category name (e.g. "Software"). |
| F | `IRS Category` | string | Mapped Schedule C line, e.g. "Line 18 — Office expense". See [`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md#irs-schedule-c-category-mapping). |
| G | `Payment Method` | string | e.g. "Business Visa", "Cash". |
| H | `Business Purpose` | string | Free-text justification, important for audit defensibility. |
| I | `Notes` | string | Free-text. |
| J | `Receipt URL` | URL (string) | Shareable Google Drive link to the receipt image, or blank if none. See [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md#getting-shareable-links). |
| K | `Mileage` | number | Miles, blank unless this row represents (or includes) a mileage deduction. |
| L | `Client` | string | Associated client name, if any. |
| M | `Business` | string | Associated business name, for multi-business users. |
| N | `Tags` | string (comma-separated) | Free-form tags. |
| O | `Location` | string | Free-text or `lat,lng`, if captured. |
| P | `Created Timestamp` | datetime (ISO 8601, UTC) | Set once, on first sync of the row. |
| Q | `Updated Timestamp` | datetime (ISO 8601, UTC) | Updated on every subsequent sync of that row. Used for conflict resolution — see [Section 9](#9-conflict-resolution--idempotency). |

Sample header row (row 1, as raw values):

```
Transaction ID | Date | Merchant | Amount | Expense Category | IRS Category | Payment Method | Business Purpose | Notes | Receipt URL | Mileage | Client | Business | Tags | Location | Created Timestamp | Updated Timestamp
```

Formatting applied programmatically when the sheet is created (via `spreadsheets.batchUpdate`):
- Row 1 frozen, bold, with a light background fill.
- Column D (`Amount`) formatted as currency (`$#,##0.00`), matching the user's locale where feasible.
- Column B (`Date`), P, and Q formatted as date/datetime.
- Column J (`Receipt URL`) rendered as a clickable `HYPERLINK` formula or plain URL (plain URL is simpler and avoids formula-injection edge cases; Sheets auto-linkifies recognizable URLs).
- Basic data validation/dropdown on column E (`Expense Category`) and F (`IRS Category`) is a nice-to-have, not required, since the source of truth for category values is the local `categories` table.

## 8. How rows map to expenses

- One Room `expenses` row maps to exactly one Sheet row, joined on **Transaction ID** (column A = `expenses.id`).
- Mileage trips synced as standalone deductions (not tied to a specific receipt expense) are written as rows where `Transaction ID` = `mileage_trips.id`, `Mileage` is populated, and `Amount` is the computed deduction (`totalMiles * ratePerMile`), converted to cents-free decimal for the sheet.
- Soft-deleted expenses (`expenses.isDeleted = true`) are **not** deleted from the Sheet automatically by default (to avoid surprising a user who may have added manual notes/formulas around that row in their own copy of the sheet); instead the sync worker can be configured to either (a) leave the row untouched, or (b) clear sensitive columns and prefix `Notes` with `[deleted]`. The current default behavior is (a) — rows are left as a historical record, and the Room `isDeleted` flag only affects what's shown in-app.
- The sync engine tracks per-row state via two local-only columns it manages internally in Room (not in the Sheet): `expenses.syncStatus` (`PENDING` / `SYNCED` / `FAILED`) and `expenses.remoteRowId` (the 1-indexed Sheet row number last written to, used to target `UPDATE` range writes without doing a full-sheet re-scan on every sync). See [`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md#expenses).

## 9. Conflict resolution & idempotency

Because the Sheet is a file the user can also open and hand-edit in the Google Sheets app/web UI, sync must be idempotent and tolerant of external edits:

- **Idempotency key:** `Transaction ID` (column A) is the single source of truth for "does this row already exist." Before inserting a new row, the sync worker performs a lookup (either a cached row-index map built from a prior full read, or an `INDEX`/`MATCH`-equivalent client-side scan of column A) to decide between `append` and `update`.
- **Last-write-wins by timestamp:** Each sync compares the local `expenses.updatedAt` against the Sheet's `Updated Timestamp` (column Q) for that Transaction ID (when readable — see below). If the local record is newer, the local value wins and is pushed. Ledgerly's primary sync direction is local-to-Sheet (the Sheet is treated as a reporting/export target, not a second writable source for the app's own business logic), so in practice this mostly guards against double-writes from retried WorkManager jobs, not true bidirectional conflicts.
- **Retry safety:** WorkManager sync jobs are built with `ExistingWorkPolicy.REPLACE`/unique work names per entity batch, and each row write is wrapped so a retried job re-resolves the row index by Transaction ID rather than blindly appending — this prevents duplicate rows if a job is interrupted after writing but before recording `SYNCED` status locally.
- **Out-of-band edits:** If a user manually edits a row in the Sheet (e.g. fixes a typo in Merchant), Ledgerly does not currently pull that edit back into Room (one-way sync, local-to-Sheet). This is a known, intentional v1 limitation, called out in the in-app sync settings copy. A future two-way sync mode is tracked on the project roadmap.
- **Header/structure drift:** Sync verifies the header row (row 1, columns A–Q) matches the expected schema before writing. If a user has reordered or deleted columns, sync fails safely with a recoverable error rather than writing data into the wrong columns, and prompts the user to either restore the header or let the app recreate a fresh "Ledgerly Expenses" spreadsheet.

## 10. Rate limits & batching

Google Sheets API v4 default quotas (per project, can be checked/raised in **APIs & Services > Sheets API > Quotas**):

| Quota | Default limit |
|---|---|
| Read requests per minute per user | 60 |
| Write requests per minute per user | 60 |
| Read requests per minute per project | 300 |
| Write requests per minute per project | 300 |

Guidance baked into the sync engine:

- **Batch writes.** Use `spreadsheets.values.batchUpdate` (or `batchUpdate` with multiple `UpdateCellsRequest`/`AppendCellsRequest`) to write many pending expenses in a single API call instead of one call per row. A WorkManager sync run collects all `PENDING` expenses since the last successful sync and writes them as one (or a small number of) batch request(s).
- **Batch reads.** When the row-index cache needs rebuilding (e.g. first sync, or after detecting structure drift), read the full `Transaction ID` column (`Expenses!A2:A`) in a single `values.get` call rather than per-row lookups.
- **Exponential backoff.** On `429 RESOURCE_EXHAUSTED` or `403` quota errors, the sync worker backs off exponentially (handled naturally by WorkManager's built-in retry/backoff policy on `Result.retry()`, configured with `BackoffPolicy.EXPONENTIAL`) rather than hammering the API.
- **Debounce rapid local edits.** Rapid successive edits to the same expense within a short window are coalesced into a single sync write (the worker syncs current state, not a change log), reducing write volume for "fix a typo three times" scenarios.
- **Chunk large initial syncs.** A first-time sync for a user migrating a large local history chunks writes into batches (e.g. 200–500 rows per `batchUpdate` request) to stay well under per-minute quotas and keep individual requests responsive.

If you anticipate heavy usage, request a quota increase via **APIs & Services > Sheets API > Quotas** in Cloud Console — increases are typically granted automatically for reasonable requests on verified apps.

## 11. Testing & publishing the OAuth app

The OAuth consent screen has three relevant publishing states:

1. **Testing** (default after creation): Only the up-to-100 explicitly added **test users** (Section 3, step 5) can complete the consent flow. Refresh tokens issued in this state expire after 7 days, which is fine for development but unacceptable for production background sync.
2. **In production, unverified:** If your scopes are limited to non-sensitive ones this is viable, but Ledgerly's scopes (`spreadsheets`, `drive.file`) are **sensitive**, so Google will show an "unverified app" warning interstitial to any user outside your test user list, and may cap usage.
3. **In production, verified:** Required before general release. Submit for verification from **OAuth consent screen > Publishing status > Publish app**, then **Prepare for verification**.

To submit for verification you will need:
- A live privacy policy URL describing exactly what Sheets/Drive data is accessed and why.
- A homepage URL.
- A demo video (screen recording) showing the OAuth consent flow and how the requested scopes are used in-app (creating the spreadsheet, uploading a receipt, etc.).
- Justification text for each sensitive scope explaining why `drive.file` (not full `drive`) and `spreadsheets` are necessary and proportionate.

Verification typically takes a few business days to a few weeks. Until verified, keep your test user list current so internal QA and beta testers aren't blocked — add testers under **OAuth consent screen > Test users**.

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| Consent screen shows "Google hasn't verified this app" | Expected in Testing/unverified-Production state. Either add the signed-in account as a test user, or complete verification (Section 11) before wide release. |
| `403 PERMISSION_DENIED` / `insufficientPermissions` calling `spreadsheets.values.*` | The user hasn't granted the `spreadsheets` scope yet — confirm incremental authorization (Section 6) completed and the resulting credential actually includes the scope (`GoogleSignInAccount.grantedScopes` or the `Authorization` result's granted scopes). |
| `403` with reason `insufficientFilePermissions` on a Sheets call against an existing file | The file wasn't created by this app and wasn't explicitly opened via Drive picker under `drive.file` — see [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md#troubleshooting). The app should always create the spreadsheet itself so it's automatically in `drive.file`'s grant. |
| `429` errors during a large initial sync | Hitting per-minute write quota (Section 10). Confirm batching is in effect and WorkManager backoff is configured; consider requesting a quota increase. |
| Refresh token stops working after ~7 days | The OAuth consent screen is still in **Testing** status (Section 11), which issues short-lived tokens. Publish to Production (and verify) for long-lived refresh tokens. |
| Scope request silently doesn't show new permissions | The user previously denied or partially granted scopes; re-check via `Identity.getAuthorizationClient()`'s returned `Authorization` or have the user revisit [Google Account permissions](https://myaccount.google.com/permissions) to remove and re-grant Ledgerly's access, then retry. |
| Row written to the wrong columns / header mismatch error | The user (or a script) modified row 1. Sync will refuse to write; either restore the original header order from [Section 7](#7-spreadsheet-schema) or use the in-app "Recreate sync sheet" option. |
