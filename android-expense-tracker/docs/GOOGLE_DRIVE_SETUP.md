# Google Drive Setup

Ledgerly stores receipt images in the signed-in user's own Google Drive — not on a Ledgerly-owned server. This document covers enabling the Drive API, the `drive.file` scope model, the "Ledgerly Receipts" folder structure, the upload/link flow, offline handling, and quota/privacy notes.

Related guides: [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md) (Google Sign-In) and [`GOOGLE_SHEETS_SETUP.md`](GOOGLE_SHEETS_SETUP.md) (the spreadsheet that stores each receipt's link). All three features share the same Google Cloud project and OAuth clients.

## Contents

- [1. Enable the Google Drive API](#1-enable-the-google-drive-api)
- [2. The `drive.file` scope and why it's least privilege](#2-the-drivefile-scope-and-why-its-least-privilege)
- [3. Creating/locating the "Ledgerly Receipts" folder](#3-creatinglocating-the-ledgerly-receipts-folder)
- [4. Uploading receipt images](#4-uploading-receipt-images)
- [5. Getting shareable links](#5-getting-shareable-links)
- [6. Offline queueing of uploads](#6-offline-queueing-of-uploads)
- [7. Storage & quota notes](#7-storage--quota-notes)
- [8. Security & privacy](#8-security--privacy)
- [Troubleshooting](#troubleshooting)

## 1. Enable the Google Drive API

1. In the [Google Cloud Console](https://console.cloud.google.com/), select the same project used for Firebase and Sheets (see [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md) / [`GOOGLE_SHEETS_SETUP.md`](GOOGLE_SHEETS_SETUP.md)).
2. Go to **APIs & Services > Library**, search **Google Drive API**.
3. Click it, then click **Enable**.

No separate OAuth client is required — Ledgerly reuses the **Android** and **Web** OAuth clients already created in [`GOOGLE_SHEETS_SETUP.md` Section 4](GOOGLE_SHEETS_SETUP.md#4-oauth-clients), since both Sheets and Drive access ride on the same incremental-authorization grant.

## 2. The `drive.file` scope and why it's least privilege

Ledgerly requests only:

```
https://www.googleapis.com/auth/drive.file
```

`drive.file` grants access **only** to:
- Files and folders the app itself creates via the Drive API (e.g. the "Ledgerly Receipts" folder, the receipt images inside it, and the "Ledgerly Expenses" spreadsheet from [`GOOGLE_SHEETS_SETUP.md`](GOOGLE_SHEETS_SETUP.md)).
- Files the user explicitly opens with the app through a Drive file picker (Ledgerly does not currently use this path, but the scope supports it for future "attach an existing file" features).

It explicitly does **not** grant:
- Read or write access to any other file in the user's Drive, including files created by other apps or manually by the user outside Ledgerly's folder.
- The ability to browse/list the user's entire Drive.

This matters for two reasons:
1. **Privacy:** a compromised or misbehaving client can only affect the small set of files Ledgerly itself created — it has no visibility into anything else in the user's Drive.
2. **OAuth verification:** `drive.file` is classified as a "sensitive" scope rather than "restricted." Restricted scopes (like the full `https://www.googleapis.com/auth/drive`) require an annual third-party security assessment to maintain production verification — a significant ongoing cost for a small team. `drive.file` only requires the standard verification process described in [`GOOGLE_SHEETS_SETUP.md` Section 11](GOOGLE_SHEETS_SETUP.md#11-testing--publishing-the-oauth-app).

**Practical implication for the app:** every file Ledgerly needs to read or update later (the receipts folder, individual receipt images, the spreadsheet) must have been created by the app's own API calls and have its Drive `fileId` persisted locally (in `expenses.receiptRemoteUrl` / a stored folder-ID preference). The app cannot rediscover these files by searching the user's whole Drive (a `files.list` query without `drive.file`-compatible constraints would return nothing outside the app's own created files anyway) — it relies on locally cached IDs, with a recovery path described in [Section 3](#3-creatinglocating-the-ledgerly-receipts-folder).

## 3. Creating/locating the "Ledgerly Receipts" folder

On first sync activation, the app ensures a single top-level Drive folder exists, named:

```
Ledgerly Receipts
```

Logic (idempotent, safe to re-run):

1. Check local storage (DataStore/preferences) for a previously saved `receiptsFolderId`. If present, verify it still exists and is accessible via `Drive.files().get(fileId).setFields("id, name, trashed")`. If valid and not trashed, use it.
2. If not present or invalid, search for an existing folder the app previously created:
   ```
   GET https://www.googleapis.com/drive/v3/files
     ?q=name='Ledgerly Receipts' and mimeType='application/vnd.google-apps.folder' and trashed=false
     &spaces=drive
   ```
   Because the OAuth grant is `drive.file`, this query only surfaces folders the app itself created (or the user picked) — it will not return an unrelated folder some other app happens to have named "Ledgerly Receipts". If found, cache its `id` locally.
3. If no folder is found, create one:
   ```
   POST https://www.googleapis.com/drive/v3/files
   Content-Type: application/json

   {
     "name": "Ledgerly Receipts",
     "mimeType": "application/vnd.google-apps.folder"
   }
   ```
   Persist the returned `id` locally as `receiptsFolderId`.

Within "Ledgerly Receipts," images are stored as flat files (no per-year/per-month subfolders by default) named using the pattern:

```
{transactionId}_{yyyyMMdd}_{merchantSlug}.jpg
```

e.g. `8f14e45f-ceea-167a-...-receipt_20260415_staples.jpg` — the leading Transaction ID keeps the filename traceable back to the exact `expenses.id` row even if the Drive `fileId` is somehow lost and needs to be rediscovered by filename search.

## 4. Uploading receipt images

1. Captured/imported receipt images are first written to local app storage (`receiptLocalPath` in the `expenses` table — see [`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md#expenses)), so the expense is fully usable offline immediately, independent of upload success.
2. When connectivity is available and the user has Drive sync enabled, a WorkManager worker uploads the file using a **resumable upload session** (recommended for mobile, since it tolerates connection drops mid-upload):
   ```
   POST https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable
   Content-Type: application/json; charset=UTF-8
   X-Upload-Content-Type: image/jpeg

   {
     "name": "8f14e45f..._20260415_staples.jpg",
     "parents": ["<receiptsFolderId>"]
   }
   ```
   The response `Location` header is the resumable session URI; the worker then `PUT`s the image bytes (optionally chunked) to that URI, and can resume from the last received byte offset if interrupted.
3. On success, the response includes the new file's `id`. The worker stores a shareable link derived from this `id` (see [Section 5](#5-getting-shareable-links)) into `expenses.receiptRemoteUrl`, and the value is subsequently written to the **Receipt URL** column when the corresponding row syncs to Sheets (see [`GOOGLE_SHEETS_SETUP.md` Section 7](GOOGLE_SHEETS_SETUP.md#7-spreadsheet-schema)).
4. Images are compressed/resized client-side before upload (long edge capped, JPEG quality tuned) to balance legibility for tax recordkeeping against upload time and Drive quota usage. The original full-resolution capture may be retained locally depending on user storage settings, independent of what's uploaded.

## 5. Getting shareable links

After upload, the worker sets file permissions so the link is usable without requiring the *viewer* to separately request access each time (still private to anyone without the link or without being the owner — see [Section 8](#8-security--privacy)):

1. (Optional, depending on desired sharing model) Set a permission allowing "anyone with the link" to view:
   ```
   POST https://www.googleapis.com/drive/v3/files/{fileId}/permissions
   Content-Type: application/json

   {
     "role": "reader",
     "type": "anyone"
   }
   ```
   Ledgerly's default is **not** to do this automatically for every receipt — receipts may contain sensitive personal/financial info, and the owner already has access. Instead the app stores the **owner-only** `webViewLink`, which works for the signed-in owner opening it from the Sheet or the app, and a CPA/accountant with whom the user has separately shared the "Ledgerly Receipts" folder (a manual, user-initiated action in Drive's own UI) can also view it without Ledgerly needing to manage per-file permissions.
2. Retrieve the link via `files.get`:
   ```
   GET https://www.googleapis.com/drive/v3/files/{fileId}?fields=id,webViewLink,webContentLink
   ```
   - `webViewLink` — opens the image in Drive's viewer. This is the value written to the Sheet's **Receipt URL** column.
   - `webContentLink` — direct download link; not used by default but available for export/PDF-report features that need to embed the image bytes.
3. The link is persisted in Room (`expenses.receiptRemoteUrl`) and synced to the Sheet on the next batch write.

## 6. Offline queueing of uploads

Receipt capture must never block on network availability:

1. **Local-first write:** Capturing or importing a receipt always writes the image to local storage and updates `expenses.receiptLocalPath` synchronously, regardless of connectivity. The expense and its receipt are immediately usable in the app.
2. **Upload queue:** A WorkManager `OneTimeWorkRequest` (or inclusion in the periodic sync worker's batch) is enqueued per pending upload, constrained with:
   ```kotlin
   Constraints.Builder()
       .setRequiredNetworkType(NetworkType.CONNECTED)
       // .setRequiredNetworkType(NetworkType.UNMETERED) if "Wi-Fi only upload" is enabled in Settings
       .build()
   ```
3. **Retry/backoff:** Upload workers use `BackoffPolicy.EXPONENTIAL` with WorkManager's standard retry mechanism (`Result.retry()`) for transient failures (timeouts, `5xx`, connectivity drops mid-resumable-upload).
4. **Resumable sessions survive process death:** Because the Drive resumable upload protocol exposes a byte-offset query (`PUT` with `Content-Range: bytes */*` returns the last received offset), an interrupted upload (app killed, device rebooted) can resume from where it left off rather than re-uploading the whole file, as long as the session URI (cached locally, with a reasonable TTL — Drive resumable sessions expire after about a week) is still valid; otherwise the worker starts a fresh upload.
5. **User-visible status:** Each expense's receipt has an implicit upload status derived from whether `receiptRemoteUrl` is populated; the broader `expenses.syncStatus` field (`PENDING`/`SYNCED`/`FAILED`) reflects the overall row's sync state including the receipt upload, surfaced in the UI (e.g. a small "pending upload" badge) so users on a job site with no signal can see their data is safely queued, not lost.
6. **"Wi-Fi only" setting:** Users with limited mobile data plans can restrict receipt uploads (typically the largest payloads in the sync pipeline) to unmetered networks while still allowing lightweight Sheets row sync over any connection — handled by setting different `NetworkType` constraints on the two worker types.

## 7. Storage & quota notes

- Receipt images count against the **user's own** Google Drive storage quota (15 GB free tier shared across Drive/Gmail/Photos, or whatever paid Google One tier they have) — not any Ledgerly-owned quota. This is a deliberate consequence of the "data stays in the user's Drive" model.
- Client-side compression (Section 4) meaningfully reduces footprint — a typical compressed receipt photo is well under 1 MB, so even users near a free-tier limit can store thousands of receipts before it's a practical concern. Still, surface a clear in-app message if an upload fails with `403` quota-exceeded (`storageQuotaExceeded` reason) so the user understands it's their Drive quota, not an app malfunction, and can free up space or upgrade their Google One plan.
- The Drive API itself also has standard per-project request-rate quotas (separate from storage quota), in the same family as the Sheets API quotas described in [`GOOGLE_SHEETS_SETUP.md` Section 10](GOOGLE_SHEETS_SETUP.md#10-rate-limits--batching). Default is generous for typical per-user upload volume (a handful of receipts per day), but bulk-import flows (e.g. importing years of historical receipts at once) should throttle/queue uploads rather than firing them all concurrently.
- Deleting an expense in Ledgerly does **not** automatically delete its Drive file by default, mirroring the Sheets soft-delete behavior described in [`GOOGLE_SHEETS_SETUP.md` Section 8](GOOGLE_SHEETS_SETUP.md#8-how-rows-map-to-expenses) — this avoids surprising data loss for a user who may have referenced the receipt link elsewhere (e.g. in a CPA email). Permanent deletion of orphaned receipt files is a manual action available from Settings, not an automatic background behavior.

## 8. Security & privacy

- **Data residency:** Receipt images and the tracking spreadsheet live entirely in the signed-in user's own Google Drive. Ledgerly's app servers (such as they exist — Firebase Auth/Crashlytics/Analytics backends) never receive or store the image bytes or spreadsheet contents.
- **Access scope:** As covered in [Section 2](#2-the-drivefile-scope-and-why-its-least-privilege), the app can only act on files it created. Even if an attacker fully compromised Ledgerly's OAuth client credentials, they could not enumerate or read arbitrary files in a user's Drive — only files Ledgerly itself created on that user's behalf, and only if they also had that specific user's valid token.
- **No public sharing by default:** Receipt links default to owner-only access (Section 5) rather than "anyone with the link," reducing the chance of inadvertent exposure of financial documents through link sharing.
- **Revocation:** A user can revoke Ledgerly's Drive access at any time via [Google Account > Security > Third-party access](https://myaccount.google.com/permissions). Doing so does not delete the already-created files (they remain in the user's Drive, now just inaccessible to the app), but it does break sync until the user re-authorizes — handled the same way as a Sheets token revocation (see [`GOOGLE_SHEETS_SETUP.md` Section 6](GOOGLE_SHEETS_SETUP.md#6-incremental-authorization)).
- **Local encryption:** Independent of Drive, the local copy of the database (including the `receiptLocalPath` reference and metadata) is encrypted at rest via SQLCipher — see [`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md#encryption-sqlcipher).
- **Account deletion / data export:** Because the user's data lives in their own Drive, "deleting their Ledgerly data" in the Drive sense is as simple as the user deleting the "Ledgerly Receipts" folder and "Ledgerly Expenses" spreadsheet from their own Drive — no separate data-deletion request to Ledgerly is needed for that portion of their data (local on-device data and Firebase Auth/Analytics data follow the app's standard account-deletion flow, out of scope for this document).

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `403 storageQuotaExceeded` on upload | The user's own Drive storage is full. Surface a clear message; this is not an app-side quota issue. Direct the user to [Google One](https://one.google.com/) or to free up Drive space. |
| `403 insufficientFilePermissions` reading/updating a previously-created file | The cached `fileId`/`receiptsFolderId` points to a file the app no longer has access to under `drive.file` — most commonly because the user revoked and re-granted access (re-granting `drive.file` does **not** retroactively restore access to files created under a prior grant in all cases) or deleted/moved the folder outside the app. Recovery: re-run the folder discovery/creation logic in [Section 3](#3-creatinglocating-the-ledgerly-receipts-folder), which will create a fresh folder if the old one can't be found, and re-upload any receipts whose links now 404. |
| `404` on a previously valid `fileId` | The user (or another collaborator) permanently deleted the file in Drive, or emptied trash. Treat as unrecoverable for that file; clear `receiptRemoteUrl` locally and prompt re-upload from the locally cached `receiptLocalPath` if still present. |
| Resumable upload session expired (`410 Gone`) mid-upload | Resumable session URIs expire (roughly one week of inactivity). The worker should detect `410` and start a brand-new resumable session rather than retrying the stale URI. |
| Upload silently never starts | Check WorkManager constraints — e.g. `NetworkType.UNMETERED` configured but device is on mobile data with "Wi-Fi only upload" enabled in Settings. This is expected behavior, not a bug; confirm against user settings before treating as an issue. |
| Receipt link in the Sheet returns "You need access" to the user themselves | Confirm the upload flow completed and a permission/ownership check wasn't accidentally narrowed — the file owner (the signed-in user, since the OAuth token acted on their behalf) should always be able to open `webViewLink`. If they're checking from a *different* Google account than the one Ledgerly is signed into, that's expected — receipts are private to the owning account by default (Section 5/8). |
