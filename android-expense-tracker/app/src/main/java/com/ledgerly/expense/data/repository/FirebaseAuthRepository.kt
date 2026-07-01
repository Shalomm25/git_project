package com.ledgerly.expense.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed [AuthRepository]. Each Firebase UID is the `ownerUserId`
 * stamped on every local record, guaranteeing per-user data isolation. Guest
 * mode uses anonymous auth so a guest can later upgrade to a real account while
 * keeping their data.
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun requireUserId(): String =
        firebaseAuth.currentUser?.uid
            ?: error("No authenticated user; auth gate should prevent this state")

    override suspend fun signInWithGoogle(idToken: String): AppResult<AuthUser> = result {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await().user!!.toAuthUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser> =
        result { firebaseAuth.signInWithEmailAndPassword(email, password).await().user!!.toAuthUser() }

    override suspend fun registerWithEmail(email: String, password: String): AppResult<AuthUser> =
        result { firebaseAuth.createUserWithEmailAndPassword(email, password).await().user!!.toAuthUser() }

    override suspend fun continueAsGuest(): AppResult<AuthUser> =
        result { firebaseAuth.signInAnonymously().await().user!!.toAuthUser() }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> =
        result { firebaseAuth.sendPasswordResetEmail(email).await() }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount(): AppResult<Unit> =
        result { firebaseAuth.currentUser?.delete()?.await() ?: Unit }

    private inline fun <T> result(block: () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (t: Throwable) {
        AppResult.Error(t, friendlyMessage(t))
    }

    private fun friendlyMessage(t: Throwable): String = when (t) {
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
            "Incorrect email or password."
        is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
            "An account already exists for this email."
        is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
            "Password is too weak — use at least 6 characters."
        else -> t.message ?: "Authentication failed. Please try again."
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        isGuest = isAnonymous,
    )
}
