package com.ledgerly.expense.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [EncryptedSharedPreferences] (AES-256, key material in the Android
 * Keystore) for secrets that must never touch plaintext storage: the SQLCipher
 * database passphrase and the user's app-lock PIN hash.
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Returns the database passphrase, generating and storing a 256-bit random
     * one on first access. The passphrase itself is encrypted at rest by the
     * Keystore-backed master key.
     */
    fun getOrCreateDatabasePassphrase(): ByteArray {
        prefs.getString(KEY_DB_PASSPHRASE, null)?.let { return it.hexToBytes() }
        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(KEY_DB_PASSPHRASE, random.toHex()).apply()
        return random
    }

    fun setPinHash(hash: String, salt: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash).putString(KEY_PIN_SALT, salt).apply()
    }

    fun pinHash(): String? = prefs.getString(KEY_PIN_HASH, null)
    fun pinSalt(): String? = prefs.getString(KEY_PIN_SALT, null)
    fun clearPin() = prefs.edit().remove(KEY_PIN_HASH).remove(KEY_PIN_SALT).apply()

    /** Wipe all secrets (used on sign-out / account deletion). */
    fun clearAll() = prefs.edit().clear().apply()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        const val FILE_NAME = "ledgerly_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
    }
}
