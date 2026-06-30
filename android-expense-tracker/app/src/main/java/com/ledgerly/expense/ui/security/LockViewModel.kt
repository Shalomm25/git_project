package com.ledgerly.expense.ui.security

import androidx.lifecycle.ViewModel
import com.ledgerly.expense.data.security.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val pinManager: PinManager,
) : ViewModel() {
    fun isPinSet(): Boolean = pinManager.isPinSet()
    fun verify(pin: String): Boolean = pinManager.verifyPin(pin)
}
