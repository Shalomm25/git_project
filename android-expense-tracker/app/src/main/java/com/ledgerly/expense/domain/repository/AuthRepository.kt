package com.ledgerly.expense.domain.repository

import com.ledgerly.expense.core.AppResult
import kotlinx.coroutines.flow.Flow

/** Authenticated user session, abstracted away from Firebase. */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isGuest: Boolean,
)

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    fun requireUserId(): String

    suspend fun signInWithGoogle(idToken: String): AppResult<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser>
    suspend fun registerWithEmail(email: String, password: String): AppResult<AuthUser>
    suspend fun continueAsGuest(): AppResult<AuthUser>
    suspend fun sendPasswordReset(email: String): AppResult<Unit>
    suspend fun signOut()
    /** Permanently deletes the account and triggers local data wipe. */
    suspend fun deleteAccount(): AppResult<Unit>
}
