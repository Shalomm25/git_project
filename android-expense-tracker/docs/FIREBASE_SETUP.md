# Firebase Setup

This guide walks through provisioning the Firebase project that backs Ledgerly's authentication, crash reporting, and analytics. Follow it once per environment (development, staging, production) you intend to build against.

Related guides: [`GOOGLE_SHEETS_SETUP.md`](GOOGLE_SHEETS_SETUP.md) and [`GOOGLE_DRIVE_SETUP.md`](GOOGLE_DRIVE_SETUP.md) use the same underlying Google Cloud project as Firebase, so it's worth reading this doc first.

## Contents

- [1. Create the Firebase project](#1-create-the-firebase-project)
- [2. Register the Android app](#2-register-the-android-app)
- [3. Download and place `google-services.json`](#3-download-and-place-google-servicesjson)
- [4. Add SHA-1 / SHA-256 fingerprints](#4-add-sha-1--sha-256-fingerprints)
- [5. Enable Authentication providers](#5-enable-authentication-providers)
- [6. Enable Crashlytics](#6-enable-crashlytics)
- [7. Enable Analytics](#7-enable-analytics)
- [8. (Optional) Firebase App Distribution](#8-optional-firebase-app-distribution)
- [9. `google-services.json` and version control](#9-google-servicesjson-and-version-control)
- [Troubleshooting](#troubleshooting)

## 1. Create the Firebase project

1. Go to the [Firebase Console](https://console.firebase.google.com/) and sign in with the Google account that should own the project.
2. Click **Add project**.
3. Name the project (e.g. `Ledgerly` for production, `Ledgerly Dev` for a development environment). Note the auto-generated **Project ID** — you'll need it later for Google Cloud Console steps in the Sheets/Drive guides.
4. Accept the Google Analytics prompt (Analytics is required for this app) and select or create a Google Analytics account.
5. Click **Create project** and wait for provisioning to finish.

> **Tip:** If you maintain separate dev/staging/prod builds, create one Firebase project per environment and use [build flavors](https://developer.android.com/build/build-variants) with per-flavor `google-services.json` files. This doc assumes a single project for simplicity; replicate the steps per flavor as needed.

## 2. Register the Android app

1. From the Firebase Console project overview, click the **Android** icon ("Add app").
2. Fill in the registration form:
   | Field | Value |
   |---|---|
   | Android package name | `com.ledgerly.expense` |
   | App nickname (optional) | `Ledgerly` |
   | Debug signing certificate SHA-1 | Add now or later — see [Section 4](#4-add-sha-1--sha-256-fingerprints) |
3. Click **Register app**.

The package name **must** exactly match `applicationId` in `app/build.gradle.kts`. If you build multiple flavors with suffixed application IDs (e.g. `com.ledgerly.expense.debug`), register a separate Firebase Android app per suffix.

## 3. Download and place `google-services.json`

1. After registering the app, click **Download google-services.json**.
2. Place the file at:
   ```
   android-expense-tracker/app/google-services.json
   ```
3. Sync Gradle (`File > Sync Project with Gradle Files` in Android Studio, or `./gradlew --refresh-dependencies`). The Google Services Gradle plugin (already configured in `app/build.gradle.kts` and the root `build.gradle.kts` via the version catalog) reads this file at build time and generates Firebase resource values.

You do **not** need to manually edit the file — re-download it from the console whenever you add a new SHA fingerprint or OAuth client, since those are embedded in it.

### Sample `google-services.json` structure (placeholders only)

Use this as a reference for what the file looks like. **Do not hand-write this file** — always download the real one from the console, since the embedded `oauth_client` entries and API keys must match what Google issued.

```json
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "ledgerly-prod",
    "storage_bucket": "ledgerly-prod.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:abcdef1234567890abcdef",
        "android_client_info": {
          "package_name": "com.ledgerly.expense"
        }
      },
      "oauth_client": [
        {
          "client_id": "000000000000-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.ledgerly.expense",
            "certificate_hash": "0000000000000000000000000000000000000000"
          }
        },
        {
          "client_id": "000000000000-yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        { "current_key": "AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" }
      ],
      "services": {
        "appinvite_service": { "other_platform_oauth_client": [] }
      }
    }
  ],
  "configuration_version": "1"
}
```

Notes on this structure:
- `oauth_client` with `client_type: 1` is the **Android OAuth client** (scoped to your package name + SHA-1).
- `oauth_client` with `client_type: 3` is the **Web OAuth client**, used as the `serverClientId` / `requestIdToken` argument when configuring `GoogleSignInOptions` — this is what lets Firebase Auth (and the Sheets/Drive APIs, via incremental authorization) exchange a Google Sign-In result for tokens. See [`GOOGLE_SHEETS_SETUP.md`](GOOGLE_SHEETS_SETUP.md#oauth-clients) for details.
- This file is environment-specific. Never reuse a development project's `google-services.json` for a release build.

## 4. Add SHA-1 / SHA-256 fingerprints

Google Sign-In (and any API that uses Android OAuth clients, including the Sheets/Drive incremental-authorization flow) validates the calling app using its signing certificate fingerprint. You must register the fingerprint for **every signing configuration** you build with: the shared debug keystore, and each release/upload keystore.

### Option A: Gradle `signingReport` (recommended)

From the project root:

```bash
./gradlew signingReport
```

This prints SHA-1 and SHA-256 for every variant/build type configured in `app/build.gradle.kts`, e.g.:

```
Variant: debug
Config: debug
Store: /home/user/.android/debug.keystore
Alias: AndroidDebugKey
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
SHA-256: 11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF

Variant: release
Config: release
Store: /home/user/keystores/ledgerly-release.jks
Alias: ledgerly
SHA1: ...
SHA-256: ...
```

### Option B: `keytool` (manual / CI)

For the default debug keystore:

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android
```

For a release/upload keystore (replace path/alias with the values from `gradle.properties`'s `KEYSTORE_PATH` / `KEY_ALIAS`):

```bash
keytool -list -v \
  -keystore /path/to/ledgerly-release.jks \
  -alias ledgerly \
  -storepass <KEYSTORE_PASSWORD> -keypass <KEY_PASSWORD>
```

If you publish through Google Play App Signing, also fetch the **App Signing certificate** SHA fingerprints from **Play Console > Setup > App integrity** and register those too — Play re-signs your release APK/AAB with a different key than your upload key.

### Registering the fingerprint

1. In the Firebase Console, go to **Project settings > General**, scroll to **Your apps**, select the `com.ledgerly.expense` Android app.
2. Under **SHA certificate fingerprints**, click **Add fingerprint**.
3. Paste the SHA-1 (and separately, the SHA-256) value with colons as shown by `signingReport`/`keytool`.
4. Repeat for every keystore: debug, local release/upload, and Play App Signing.
5. Re-download `google-services.json` after adding fingerprints — the file embeds OAuth client data tied to registered fingerprints, so old downloads won't have the new ones.

## 5. Enable Authentication providers

In the Firebase Console, go to **Build > Authentication > Sign-in method** and enable:

| Provider | Purpose in Ledgerly | Configuration notes |
|---|---|---|
| **Email/Password** | Standard account creation/sign-in | Enable "Email/Password". Leave "Email link (passwordless sign-in)" off unless you plan to support it. |
| **Google** | Primary sign-in method; also the identity used for Sheets/Drive OAuth consent | Enable, set a support email. Firebase auto-creates a **Web client ID** here — this is the same Web client referenced in `google-services.json` (`client_type: 3`) and used for `GoogleSignInOptions.requestIdToken(...)`. |
| **Anonymous** | Guest mode — lets a user try the app with local-only storage before creating an account | Enable. Guest accounts can later be upgraded to a permanent account via `linkWithCredential` when the user signs in with Google or email/password, preserving their local expense data. |

After enabling Google Sign-In, confirm under **Authentication > Sign-in method > Google > Web SDK configuration** that the Web client ID matches the one used in app code (`R.string.default_web_client_id`, generated from `google-services.json`).

## 6. Enable Crashlytics

1. In the Firebase Console, go to **Build > Crashlytics**.
2. Click **Enable Crashlytics** (or **Get started**).
3. The Crashlytics Gradle plugin and SDK dependency are already wired in the version catalog and `app/build.gradle.kts`. No additional console configuration is required for crash reporting to start.
4. Force a test crash to verify the pipeline end-to-end:
   ```kotlin
   // Temporary, for verification only — do not commit
   Button(onClick = { throw RuntimeException("Crashlytics test crash") }) {
       Text("Test Crash")
   }
   ```
5. Run the app, trigger the crash, relaunch the app (Crashlytics uploads on next app start), then check **Crashlytics** in the console — it can take a few minutes for the first event to appear.
6. For de-obfuscated stack traces on release/minified builds, ensure the Crashlytics Gradle plugin's mapping-file upload task runs as part of your release build (`uploadCrashlyticsMappingFileRelease`), which it does automatically when the plugin is applied and minification is enabled.

## 7. Enable Analytics

Analytics is enabled automatically when the Firebase project is created with Google Analytics linked (Section 1). To verify:

1. In the Firebase Console, go to **Engage > Analytics > Dashboard**.
2. Run the app on a device/emulator with Play Services and use it for a minute or two.
3. Use **DebugView** (`Engage > Analytics > DebugView`) for real-time event verification during development:
   ```bash
   adb shell setprop debug.firebase.analytics.app com.ledgerly.expense
   ```
   Disable when done:
   ```bash
   adb shell setprop debug.firebase.analytics.app .none.
   ```
4. Standard events Ledgerly logs include screen views (via Compose Navigation + the Analytics Navigation listener), `expense_created`, `expense_synced`, `receipt_captured`, and `sheet_export_completed`. Define any custom event/parameter names consistently in a single analytics events object in code; this doc does not prescribe source structure.

## 8. (Optional) Firebase App Distribution

Useful for distributing debug/internal builds to testers (QA, beta users) without Play Store review.

1. In the Firebase Console, go to **Release & Monitor > App Distribution**.
2. Click **Get started**, and create at least one **tester group** (e.g. `internal-qa`).
3. Upload a build manually for a first test, or wire it into CI:
   ```bash
   ./gradlew assembleDebug appDistributionUploadDebug \
     --artifactType="APK" \
     --testers="tester1@example.com,tester2@example.com" \
     --releaseNotes="Internal QA build"
   ```
   This requires the Firebase App Distribution Gradle plugin (add to `app/build.gradle.kts` if not already present) and a service account or Firebase CLI login for non-interactive CI uploads.
4. Authenticate CI with a service account: **Project settings > Service accounts > Generate new private key**, store the JSON as a CI secret, and reference it via `serviceCredentialsFile` in the plugin config or the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
5. Testers receive an email invite and install the Firebase App Tester app to receive builds.

This step is optional and primarily relevant once you have a CI pipeline producing distributable builds.

## 9. `google-services.json` and version control

`app/google-services.json` is listed in `.gitignore` and **must never be committed** — it is environment-specific and (while not a secret on its own, since it ships inside the APK) should not be assumed identical across forks, contributors, or environments.

For new contributors, a template is checked in at `app/google-services.json.template` so the expected shape and placement are obvious without exposing real project IDs/keys:

```json
{
  "project_info": {
    "project_number": "REPLACE_WITH_PROJECT_NUMBER",
    "project_id": "REPLACE_WITH_PROJECT_ID",
    "storage_bucket": "REPLACE_WITH_PROJECT_ID.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "REPLACE_WITH_MOBILESDK_APP_ID",
        "android_client_info": {
          "package_name": "com.ledgerly.expense"
        }
      },
      "oauth_client": [],
      "api_key": [
        { "current_key": "REPLACE_WITH_API_KEY" }
      ],
      "services": {
        "appinvite_service": { "other_platform_oauth_client": [] }
      }
    }
  ],
  "configuration_version": "1"
}
```

Setup checklist for new contributors:

1. Complete Sections 1–4 above to provision your own Firebase project (or request access to the shared development project from a maintainer).
2. Download your own `google-services.json` and place it at `app/google-services.json` (this path is gitignored; the template above is informational only and is not consumed by the build).
3. Never paste real `api_key`, `mobilesdk_app_id`, or `project_number` values into commits, issues, or PRs.

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `File google-services.json is missing` build error | The file isn't at `app/google-services.json`. Re-download from **Project settings > General > Your apps** and place it exactly there. |
| Google Sign-In fails with `DEVELOPER_ERROR` / status code 10 | The SHA-1 of the keystore you built with isn't registered in Firebase, or you're using a stale `google-services.json` downloaded before adding the fingerprint. Re-run `./gradlew signingReport`, add the SHA-1/SHA-256, then re-download the JSON. |
| Sign-in works on one machine but not another | Each developer's debug keystore (`~/.android/debug.keystore`) is unique unless explicitly shared. Either share one debug keystore across the team (store it securely, not in git) or have every developer register their own SHA-1. |
| Crashlytics dashboard shows no events | The SDK only sends crash reports on the **next app launch** after a crash, not at crash time. Force-close and relaunch. Also confirm `firebase-crashlytics` Gradle plugin is applied and INTERNET permission is present. |
| Analytics events don't appear in DebugView | Confirm `adb shell setprop debug.firebase.analytics.app com.ledgerly.expense` was run after the app process started, and that the device has Google Play Services. |
| `Default FirebaseApp is not initialized` at runtime | `google-services.json` was added but Gradle wasn't re-synced, or the Google Services plugin isn't applied in `app/build.gradle.kts`. Sync Gradle and rebuild. |
| App Distribution upload fails with auth error | Confirm the service account JSON has the **Firebase App Distribution Admin** role and `GOOGLE_APPLICATION_CREDENTIALS` (or the plugin's `serviceCredentialsFile`) points to a valid, readable file in CI. |
