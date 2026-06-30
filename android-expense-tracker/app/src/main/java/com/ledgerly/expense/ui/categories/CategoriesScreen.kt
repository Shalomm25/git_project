package com.ledgerly.expense.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerly.expense.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Category?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(categories, key = { it.id }) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    supportingContent = {
                        Text("Schedule C · ${category.scheduleC?.displayName ?: category.scheduleCLine}")
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.delete(category) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                )
            }
        }
    }

    if (showDialog) {
        CategoryEditDialog(
            existing = editing,
            scheduleCOptions = viewModel.scheduleCOptions.map { it.line to it.displayName },
            onDismiss = { showDialog = false },
            onConfirm = { name, line, color ->
                viewModel.addOrUpdate(editing, name, line, color)
                showDialog = false
            },
        )
    }
}

@Composable
private fun CategoryEditDialog(
    existing: Category?,
    scheduleCOptions: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, scheduleCLine: String, colorHex: String) -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var line by remember { mutableStateOf(existing?.scheduleCLine ?: scheduleCOptions.first().first) }
    val color = existing?.colorHex ?: "#0E7C5A"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                Text("Maps to IRS Schedule C line:", style = MaterialTheme.typography.labelMedium)
                // A compact selector; a full implementation uses an ExposedDropdownMenu.
                scheduleCOptions.firstOrNull { it.first == line }?.let { (l, label) ->
                    Text("Line $l — $label", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, line, color) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
