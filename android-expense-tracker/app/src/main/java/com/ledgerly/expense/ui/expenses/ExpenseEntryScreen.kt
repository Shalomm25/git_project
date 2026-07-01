package com.ledgerly.expense.ui.expenses

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerly.expense.core.util.DateUtils

/**
 * Fast expense entry. Amount and merchant are front-and-center; category is a
 * one-tap chip row; receipt capture is a single button. Designed to record an
 * expense in as few taps as possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ExpenseEntryViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) viewModel.setReceipt(pendingCameraPath)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) viewModel.setReceipt(ReceiptCapture.importFrom(context, uri))
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = form.date
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        viewModel.update { it.copy(date = picked) }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            if (form.isEditing) "Edit expense" else "New expense",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = form.amountText,
            onValueChange = { v -> viewModel.update { it.copy(amountText = v) } },
            label = { Text("Amount") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.merchant,
            onValueChange = { v -> viewModel.update { it.copy(merchant = v) } },
            label = { Text("Merchant / Vendor") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        AssistChip(
            onClick = { showDatePicker = true },
            label = { Text(DateUtils.formatDisplay(form.date)) },
            leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
        )

        Text("Category", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = form.categoryId == category.id,
                    onClick = { viewModel.update { it.copy(categoryId = category.id) } },
                    label = { Text(category.name) },
                )
            }
        }

        if (paymentMethods.isNotEmpty()) {
            Text("Payment method", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                paymentMethods.forEach { method ->
                    FilterChip(
                        selected = form.paymentMethod == method.name,
                        onClick = { viewModel.update { it.copy(paymentMethod = method.name) } },
                        label = { Text(method.name) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.businessPurpose,
            onValueChange = { v -> viewModel.update { it.copy(businessPurpose = v) } },
            label = { Text("Business purpose") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.notes,
            onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.mileage,
                onValueChange = { v -> viewModel.update { it.copy(mileage = v) } },
                label = { Text("Mileage") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = form.tagsText,
                onValueChange = { v -> viewModel.update { it.copy(tagsText = v) } },
                label = { Text("Tags") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            if (form.receiptLocalPath != null) "Receipt attached ✓" else "Receipt",
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val (file, uri) = ReceiptCapture.newReceiptUri(context)
                    pendingCameraPath = file.absolutePath
                    cameraLauncher.launch(uri)
                },
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                Text("  Camera")
            }
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                Text("  Import")
            }
        }

        if (form.error != null) {
            Text(
                form.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = { viewModel.save(onSaved) },
            enabled = form.isValid && !form.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (form.isEditing) "Save changes" else "Add expense")
        }

        if (form.isEditing) {
            OutlinedButton(
                onClick = { viewModel.delete(onSaved) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Text("  Delete expense")
            }
        }

        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}
