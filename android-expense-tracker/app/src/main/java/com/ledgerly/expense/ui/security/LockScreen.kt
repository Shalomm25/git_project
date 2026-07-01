package com.ledgerly.expense.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ledgerly.expense.domain.repository.AppLockMode

/**
 * Full-screen lock gate. Offers biometric unlock when configured, with PIN as
 * the fallback. Shown above all app content while [com.ledgerly.expense.security.AppLockManager]
 * reports the app as locked.
 */
@Composable
fun LockScreen(
    lockMode: AppLockMode,
    onBiometricRequest: () -> Unit,
    onUnlocked: () -> Unit,
    viewModel: LockViewModel = hiltViewModel(),
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text("Ledgerly is locked", style = MaterialTheme.typography.headlineSmall)

        if (lockMode == AppLockMode.BIOMETRIC) {
            Button(
                onClick = onBiometricRequest,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) { Text("Unlock") }
        }

        if (lockMode == AppLockMode.PIN || viewModel.isPinSet()) {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8) { pin = it; error = null } },
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = error != null,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = {
                    if (viewModel.verify(pin)) onUnlocked() else error = "Incorrect PIN"
                },
                enabled = pin.length >= 4,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text("Unlock with PIN") }
        }

        if (lockMode == AppLockMode.BIOMETRIC) {
            TextButton(onClick = onBiometricRequest, modifier = Modifier.padding(top = 8.dp)) {
                Text("Use biometrics")
            }
        }
    }
}
