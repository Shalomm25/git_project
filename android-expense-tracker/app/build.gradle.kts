import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Firebase plugins. Commented apply-points below are activated automatically
    // once a valid google-services.json is present. They are declared here so the
    // project resolves even before Firebase is configured — see docs/FIREBASE_SETUP.md.
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Load the Web OAuth client id from local.properties (falls back to a placeholder
// so the project still builds for contributors who haven't configured Google yet).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val googleWebClientId: String =
    (localProps.getProperty("GOOGLE_WEB_CLIENT_ID") ?: System.getenv("GOOGLE_WEB_CLIENT_ID"))
        ?: "REPLACE_WITH_WEB_CLIENT_ID.apps.googleusercontent.com"

android {
    namespace = "com.ledgerly.expense"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ledgerly.expense"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.ledgerly.expense.HiltTestRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

        // Room schema export for migration tracking / tests.
        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    signingConfigs {
        create("release") {
            val keystorePath = (project.findProperty("KEYSTORE_PATH") as String?)
                ?: System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = (project.findProperty("KEYSTORE_PASSWORD") as String?)
                    ?: System.getenv("KEYSTORE_PASSWORD")
                keyAlias = (project.findProperty("KEY_ALIAS") as String?)
                    ?: System.getenv("KEY_ALIAS")
                keyPassword = (project.findProperty("KEY_PASSWORD") as String?)
                    ?: System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            // Crashlytics is noisy in dev; disable mapping upload for debug builds.
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only attach the release signing config if a keystore was provided.
            if ((project.findProperty("KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")) != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "META-INF/*.kotlin_module"
            )
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // --- Core / lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // --- Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // --- DI: Hilt ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // --- Persistence: Room (encrypted via SQLCipher) ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)

    // --- Background work ---
    implementation(libs.work.runtime.ktx)
    implementation(libs.datastore.preferences)

    // --- Camera (receipts) ---
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.coil.compose)

    // --- Security ---
    implementation(libs.biometric)
    implementation(libs.security.crypto)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.auth)

    // --- Google Sheets / Drive APIs ---
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.sheets)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.http.client.gson)
    implementation(libs.google.auth.library.oauth2)

    // --- Coroutines ---
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
    implementation(libs.accompanist.permissions)

    // --- Unit tests ---
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)

    // --- Instrumented / UI tests ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.compose.ui.test.manifest)
}
