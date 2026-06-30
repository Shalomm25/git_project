package com.ledgerly.expense.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.core.util.Money
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.model.SyncStatus

/**
 * A single expense row: colored category dot, merchant + category/date, amount,
 * and sync/receipt status affordances. Tapping opens the entry screen.
 */
@Composable
fun ExpenseListItem(
    expense: Expense,
    categoryName: String,
    categoryColor: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(expense.merchant, style = MaterialTheme.typography.titleMedium) },
        supportingContent = {
            Text("$categoryName · ${DateUtils.formatDisplay(expense.date)}")
        },
        leadingContent = {
            Surface(
                color = runCatching { Color(android.graphics.Color.parseColor(categoryColor)) }
                    .getOrDefault(MaterialTheme.colorScheme.primary),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Receipt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).clip(CircleShape),
                    )
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                Text(
                    Money.format(expense.amountCents, expense.currency),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (!expense.hasReceipt) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Missing receipt",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    val syncIcon = when (expense.syncStatus) {
                        SyncStatus.SYNCED -> Icons.Outlined.CloudDone
                        else -> Icons.Outlined.Cloud
                    }
                    Icon(
                        syncIcon,
                        contentDescription = expense.syncStatus.name,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        },
    )
}
