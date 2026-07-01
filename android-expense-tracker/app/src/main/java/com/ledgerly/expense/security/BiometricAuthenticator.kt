package com.ledgerly.expense.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Result of a biometric/device-credential prompt. */
sealed interface BiometricResult {
    data object Success : BiometricResult
    data object Unavailable : BiometricResult
    data class Failed(val message: String) : BiometricResult
    data object Cancelled : BiometricResult
}

/**
 * Thin wrapper over AndroidX [BiometricPrompt] exposing a coroutine-friendly
 * API. Allows fingerprint/face plus device-credential fallback so users can
 * always unlock even without enrolled biometrics.
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {

    fun canAuthenticate(): Boolean {
        val allowed = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return BiometricManager.from(activity).canAuthenticate(allowed) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(
        title: String,
        subtitle: String,
    ): BiometricResult = suspendCancellableCoroutine { cont ->
        if (!canAuthenticate()) {
            cont.resume(BiometricResult.Unavailable)
            return@suspendCancellableCoroutine
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (cont.isActive) cont.resume(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val res = when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        -> BiometricResult.Cancelled
                        else -> BiometricResult.Failed(errString.toString())
                    }
                    if (cont.isActive) cont.resume(res)
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()
        prompt.authenticate(info)
    }
}
