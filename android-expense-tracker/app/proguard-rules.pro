# Ledgerly ProGuard / R8 rules

# Keep generic signatures & annotations (needed by Room, Gson, Retrofit, Hilt).
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# --- Kotlin / Coroutines ---
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger ---
-dontwarn com.google.errorprone.annotations.**

# --- SQLCipher ---
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }

# --- Google API client (Sheets/Drive) uses reflection on model classes ---
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.sheets.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class * { @com.google.api.client.util.Key <fields>; }
-dontwarn com.google.api.client.**
-dontwarn com.google.common.**
-dontwarn org.apache.http.**
-dontwarn javax.naming.**

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- App data/domain models serialized to Sheets / persisted ---
-keep class com.ledgerly.expense.domain.model.** { *; }
-keep class com.ledgerly.expense.data.local.entity.** { *; }
