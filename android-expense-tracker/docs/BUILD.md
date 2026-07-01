# Build Guide

This document covers everything needed to build, sign, and run **Ledgerly** from source — from a clean machine to a signed release artifact.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Cloning the Repository](#cloning-the-repository)
- [Configuring local.properties](#configuring-localproperties)
- [Firebase Setup](#firebase-setup)
- [Google Cloud Console — OAuth Setup](#google-cloud-console--oauth-setup)
- [Building a Debug Build](#building-a-debug-build)
- [Signing Configuration](#signing-configuration)
- [Building a Release Build](#building-a-release-build)
- [Build Outputs](#build-outputs)
- [Installing via adb](#installing-via-adb)
- [Running Tests](#running-tests)
- [Troubleshooting](#troubleshooting)

## Prerequisites

| Requirement | Version |
|---|---|
| JDK | 17 |
| Android Studio | Ladybug (2024.2.1) or newer |
| Android SDK Platform | 35 |
| Android Build-Tools | matching SDK 35 |
| minSdk | 26 |
| Kotlin | 2.0 (managed via version catalog) |
| Gradle | via included wrapper (`./gradlew`) — no local install required |

Verify your JDK version:

```bash
java -version
# openjdk version "17..."
```

If Android Studio bundles its own JDK (JBR), you can point Gradle at it instead of a system JDK via **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.

## Cloning the Repository

```bash
git clone https://github.com/ledgerly/android-expense-tracker.git
cd android-expense-tracker
```

Open the project in Android Studio (**File → Open**, select the `android-expense-tracker` directory) or continue entirely from the command line using the Gradle wrapper.

## Configuring local.properties

`local.properties` is machine-specific and is not checked into version control. Android Studio generates it automatically on first sync, but you can create it manually if building headlessly:

```properties
sdk.dir=/path/to/Android/sdk
```

On most CI runners, the `ANDROID_HOME` / `ANDROID_SDK_ROOT` environment variable is sufficient and `local.properties` is not required.

## Firebase Setup

Ledgerly uses Firebase for Auth, Crashlytics, and Analytics.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create (or select) a project.
2. Add an Android app with package name `com.ledgerly.expense`.
3. Register the app's SHA-1 (and SHA-256, recommended) signing certificate fingerprints for both your debug and release keystores — required for Google Sign-In to work:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
4. Download the generated `google-services.json`.
5. Place it at:
   ```
   app/google-services.json
   ```
6. In the Firebase Console, enable:
   - **Authentication** → Sign-in methods: Google, Email/Password
   - **Crashlytics**
   - **Analytics**

`app/google-services.json` is gitignored — every developer and CI environment must supply their own.

## Google Cloud Console — OAuth Setup

Ledgerly's Google Sheets sync and Google Drive export require OAuth 2.0 credentials in the **same Google Cloud project** backing your Firebase project (Firebase projects are Google Cloud projects).

### 1. Enable the required APIs

In [Google Cloud Console](https://console.cloud.google.com/) → **APIs & Services → Library**, enable:

- **Google Sheets API**
- **Google Drive API**

### 2. Configure the OAuth consent screen

**APIs & Services → OAuth consent screen**:

- User type: External (or Internal if using a Google Workspace org)
- Add scopes:
  - `https://www.googleapis.com/auth/spreadsheets`
  - `https://www.googleapis.com/auth/drive.file`
  - `email`, `profile`
- Add test users while the app is in "Testing" publishing status

### 3. Create OAuth client IDs

**APIs & Services → Credentials → Create Credentials → OAuth client ID**

You need two client IDs:

| Client type | Used for | Notes |
|---|---|---|
| **Android** | Native Google Sign-In on-device | Requires package name `com.ledgerly.expense` and the SHA-1 of your signing certificate (debug and release each need their own Android OAuth client, or register both fingerprints) |
| **Web application** | Server-side token verification / Firebase Auth's `default_web_client_id` | This is the client ID referenced as `R.string.default_web_client_id`, auto-populated into `google-services.json` once linked in Firebase |

Steps:

1. Create the **Android** OAuth client: enter package name `com.ledgerly.expense` and your SHA-1 fingerprint (from the `keytool` command above; repeat for each keystore — debug, release, and any Play App Signing certificate).
2. Create the **Web application** OAuth client (or use the one Firebase Auth created automatically when you enabled Google Sign-In).
3. Re-download `google-services.json` after adding the Android OAuth client so it's reflected locally.

### 4. Verify scopes match runtime requests

The app requests `spreadsheets` and `drive.file` scopes at sign-in time (incremental authorization). If you change scopes, update the consent screen configuration accordingly and re-test sign-in, since previously granted consent does not retroactively include new scopes.

## Building a Debug Build

Debug builds are signed automatically with the auto-generated Android debug keystore and do not require any signing configuration.

```bash
./gradlew assembleDebug
```

Or install directly to a connected device/emulator:

```bash
./gradlew installDebug
```

## Signing Configuration

Release builds must be signed with your own keystore.

### Generate a keystore

```bash
keytool -genkey -v -keystore ledgerly-release.keystore \
  -alias ledgerly \
  -keyalg RSA -keysize 2048 -validity 10000
```

Store the resulting `.keystore` file somewhere **outside** the repository (e.g., a secrets directory or CI secret store). Never commit it.

### Configure gradle.properties

Add the following to your local (gitignored) `gradle.properties`, or supply them as environment variables / `-P` flags in CI:

```properties
KEYSTORE_PATH=/absolute/path/to/ledgerly-release.keystore
KEYSTORE_PASSWORD=your-keystore-password
KEY_ALIAS=ledgerly
KEY_PASSWORD=your-key-password
```

The app's `build.gradle.kts` release `signingConfig` reads these properties (falling back gracefully, or failing the build with a clear error, if they're absent) so that debug builds and CI checkout/build steps that don't need release signing aren't blocked.

For CI, pass these as secrets injected as Gradle properties, for example:

```bash
./gradlew bundleRelease \
  -PKEYSTORE_PATH="$KEYSTORE_PATH" \
  -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
  -PKEY_ALIAS="$KEY_ALIAS" \
  -PKEY_PASSWORD="$KEY_PASSWORD"
```

## Building a Release Build

### APK (for direct distribution / sideloading / internal testing)

```bash
./gradlew assembleRelease
```

### AAB (Android App Bundle, required for Google Play Store)

```bash
./gradlew bundleRelease
```

Both tasks run R8 minification/shrinking and resource shrinking as configured in the release build type, and require a valid signing configuration as described above.

## Build Outputs

| Artifact | Path |
|---|---|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| Release AAB | `app/build/outputs/bundle/release/app-release.aab` |
| Mapping file (R8) | `app/build/outputs/mapping/release/mapping.txt` |

Keep `mapping.txt` for each release — it's required to de-obfuscate Crashlytics stack traces.

## Installing via adb

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

For a release APK, ensure "Install unknown apps" is permitted for your install source on the target device, then:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

## Running Tests

### Unit tests

Run on the JVM, no device required:

```bash
./gradlew test
```

Reports are written to `app/build/reports/tests/`.

### Instrumented tests

Require a connected device or running emulator:

```bash
./gradlew connectedAndroidTest
```

Reports are written to `app/build/reports/androidTests/connected/`.

### Running a single test class

```bash
./gradlew test --tests "com.ledgerly.expense.domain.usecase.AddExpenseUseCaseTest"
```

## Troubleshooting

| Problem | Likely Cause / Fix |
|---|---|
| `File google-services.json is missing` | Download it from Firebase Console for the `com.ledgerly.expense` Android app and place it at `app/google-services.json`. |
| Google Sign-In fails with `DEVELOPER_ERROR` (status code 10) | SHA-1 fingerprint of your keystore is not registered on an Android OAuth client in Google Cloud Console, or `google-services.json` is stale — re-download after adding the fingerprint. |
| Sheets sync fails with `403` from the Sheets API | Google Sheets API (or Drive API) is not enabled in the linked Google Cloud project, or the OAuth consent screen scopes don't include `spreadsheets` / `drive.file`. |
| `Keystore was tampered with, or password was incorrect` | `KEYSTORE_PASSWORD` or `KEY_PASSWORD` in `gradle.properties` doesn't match the keystore. Re-check for typos or copy/paste whitespace. |
| Release build fails signing with `KEYSTORE_PATH not found` | Ensure the path in `gradle.properties` is absolute and the file exists on the build machine; in CI, confirm the secret-injection step ran before the build step. |
| Room schema / migration crash after a model change | A Room migration is missing for the updated schema version. Add a `Migration` object or, for local development only, clear app data. |
| `Duplicate class` errors from Firebase/Google Play Services | Mismatched BOM/library versions in `gradle/libs.versions.toml`. Make sure all Firebase artifacts pull their versions from the Firebase BOM entry. |
| Gradle sync stuck on "Resolving dependencies" | Check corporate proxy/VPN settings, or try `./gradlew build --refresh-dependencies`. |
| Encrypted Room database fails to open ("file is not a database") | SQLCipher passphrase source (Keystore-backed) changed or app data was restored from an incompatible backup. Uninstall/reinstall during development, since the encrypted DB cannot be opened without the original passphrase. |
| `connectedAndroidTest` reports "no connected devices" | Start an emulator (`emulator -avd <name>`) or connect a physical device with USB debugging enabled, then verify with `adb devices`. |

For anything not covered here, check the GitHub Issues tab or open a new issue with your Gradle/Android Studio version and the full stack trace.
