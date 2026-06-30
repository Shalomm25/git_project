package com.ledgerly.expense.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hashes and verifies the app-lock PIN using PBKDF2 with a per-user random salt.
 * The PIN is never stored in plaintext; only the derived hash + salt live in
 * [SecurePreferences].
 */
@Singleton
class PinManager @Inject constructor(
    private val securePreferences: SecurePreferences,
) {
    fun isPinSet(): Boolean = securePreferences.pinHash() != null

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(pin, salt)
        securePreferences.setPinHash(hash.toHex(), salt.toHex())
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = securePreferences.pinHash() ?: return false
        val salt = securePreferences.pinSalt()?.hexToBytes() ?: return false
        val candidate = derive(pin, salt).toHex()
        // Constant-time comparison to avoid timing side channels.
        return MessageDigest.isEqual(candidate.toByteArray(), storedHash.toByteArray())
    }

    fun clearPin() = securePreferences.clearPin()

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec).encoded
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH = 256
    }
}
