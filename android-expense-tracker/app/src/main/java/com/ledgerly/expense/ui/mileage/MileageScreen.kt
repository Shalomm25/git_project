package com.ledgerly.expense.ui.mileage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.core.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageScreen(
    onBack: () -> Unit,
    viewModel: MileageViewModel = hiltViewModel(),
) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var miles by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mileage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Log a trip", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(start, { start = it }, label = { Text("Start location") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(end, { end = it }, label = { Text("End location") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            miles, { miles = it }, label = { Text("Total miles") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(purpose, { purpose = it }, label = { Text("Business purpose") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                miles.toDoubleOrNull()?.let {
                                    viewModel.addTrip(start, end, it, purpose)
                                    start = ""; end = ""; miles = ""; purpose = ""
                                }
                            },
                            enabled = miles.toDoubleOrNull() != null,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Add trip") }
                    }
                }
            }

            items(trips, key = { it.id }) { trip ->
                ListItem(
                    headlineContent = { Text("${trip.startLocation} → ${trip.endLocation}") },
                    supportingContent = {
                        Text("${trip.totalMiles} mi · ${DateUtils.formatDisplay(trip.date)}")
                    },
                    trailingContent = { Text(Money.format(trip.deductionCents)) },
                )
            }
        }
    }
}
