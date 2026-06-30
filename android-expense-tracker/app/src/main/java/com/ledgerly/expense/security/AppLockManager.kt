package com.ledgerly.expense.security

import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.domain.repository.AppLockMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the runtime lock state for the app. The UI observes [isLocked] and shows
 * the lock screen when true. Locking is requested when the app is backgrounded
 * for longer than the configured auto-lock window.
 */
@Singleton
class AppLockManager @Inject constructor() {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var backgroundedAt: Long? = null
    private var lockMode: AppLockMode = AppLockMode.NONE
    private var autoLockMillis: Long = 60_000

    fun configure(mode: AppLockMode, autoLockMinutes: Int) {
        lockMode = mode
        autoLockMillis = autoLockMinutes.coerceAtLeast(0) * 60_000L
        if (mode == AppLockMode.NONE) _isLocked.value = false
    }

    /** Call when the app first launches; lock immediately if protection is on. */
    fun onAppStart() {
        if (lockMode != AppLockMode.NONE) _isLocked.value = true
    }

    fun onEnterBackground() {
        if (lockMode != AppLockMode.NONE) backgroundedAt = DateUtils.nowMillis()
    }

    fun onEnterForeground() {
        val since = backgroundedAt ?: return
        if (lockMode != AppLockMode.NONE && DateUtils.nowMillis() - since >= autoLockMillis) {
            _isLocked.value = true
        }
        backgroundedAt = null
    }

    fun unlock() {
        _isLocked.value = false
    }
}
