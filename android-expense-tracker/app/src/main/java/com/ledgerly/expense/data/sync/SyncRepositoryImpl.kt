package com.ledgerly.expense.data.sync

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.data.local.dao.BusinessDao
import com.ledgerly.expense.data.local.dao.CategoryDao
import com.ledgerly.expense.data.local.dao.ClientDao
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.remote.ExpenseSheetSchema
import com.ledgerly.expense.data.remote.GoogleDriveService
import com.ledgerly.expense.data.remote.GoogleSheetsService
import com.ledgerly.expense.di.IoDispatcher
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.model.ScheduleCCategory
import com.ledgerly.expense.domain.repository.ExpenseRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import com.ledgerly.expense.domain.repository.SyncInfo
import com.ledgerly.expense.domain.repository.SyncRepository
import com.ledgerly.expense.domain.repository.SyncState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates offline-first synchronization with the user's Google account.
 *
 * Strategy (see ARCHITECTURE.md):
 *  1. Ensure a backing spreadsheet exists (create + persist its id once).
 *  2. Upload any receipts captured offline to Drive, capturing the share URL.
 *  3. Push every PENDING/FAILED expense as a row keyed by Transaction ID,
 *     updating in place when a row already exists (last-write-wins by updatedAt).
 *  4. Mark each record SYNCED (or FAILED for retry with backoff).
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val clientDao: ClientDao,
    private val businessDao: BusinessDao,
    private val settingsRepository: SettingsRepository,
    private val sheetsService: GoogleSheetsService,
    private val driveService: GoogleDriveService,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SyncRepository {

    private val syncInfo = MutableStateFlow(SyncInfo())
    override fun observeSyncInfo(): Flow<SyncInfo> = syncInfo.asStateFlow()

    override suspend fun ensureSpreadsheet(userId: String): AppResult<String> = withContext(io) {
        try {
            val settings = settingsRepository.settings.first()
            val existingId = settings.spreadsheetId
            val id = if (existingId != null && sheetsService.spreadsheetExists(existingId)) {
                existingId
            } else {
                sheetsService.createSpreadsheet("Ledgerly — Business Expenses").also {
                    settingsRepository.setSpreadsheetId(it)
                }
            }
            val url = sheetsService.spreadsheetUrl(id)
            syncInfo.update { it.copy(spreadsheetUrl = url) }
            AppResult.Success(id)
        } catch (t: Throwable) {
            AppResult.Error(t)
        }
    }

    override suspend fun uploadPendingReceipts(userId: String): AppResult<Unit> = withContext(io) {
        try {
            val pending = expenseRepository.getPendingSync(userId)
                .filter { it.receiptLocalPath != null && it.receiptRemoteUrl == null }
            for (expense in pending) {
                val url = driveService.uploadReceipt(
                    localPath = expense.receiptLocalPath!!,
                    displayName = "${expense.merchant}-${expense.id}.jpg",
                )
                expenseRepository.upsert(expense.copy(receiptRemoteUrl = url))
            }
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error(t)
        }
    }

    override suspend fun syncNow(userId: String): AppResult<Unit> = withContext(io) {
        syncInfo.update { it.copy(state = SyncState.SYNCING, errorMessage = null) }
        try {
            val spreadsheetId = when (val r = ensureSpreadsheet(userId)) {
                is AppResult.Success -> r.data
                is AppResult.Error -> return@withContext r
            }
            uploadPendingReceipts(userId)

            val pending = expenseRepository.getPendingSync(userId)
            val categories = categoryDao.observeAll(userId).first().associateBy { it.id }
            val clients = clientDao.observeAll(userId).first().associateBy { it.id }
            val businesses = businessDao.observeAll(userId).first().associateBy { it.id }

            for (expense in pending) {
                try {
                    pushExpense(spreadsheetId, expense, categories, clients, businesses)
                } catch (t: Throwable) {
                    expenseRepository.markFailed(expense.id)
                }
            }
            // Drop tombstones that have been reflected remotely.
            expenseDao.purgeDeletedSynced()

            val remaining = expenseRepository.getPendingSync(userId).size
            syncInfo.update {
                it.copy(
                    state = if (remaining == 0) SyncState.SUCCESS else SyncState.ERROR,
                    lastSyncedAt = DateUtils.nowMillis(),
                    pendingCount = remaining,
                )
            }
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            syncInfo.update { it.copy(state = SyncState.ERROR, errorMessage = t.message) }
            AppResult.Error(t)
        }
    }

    private suspend fun pushExpense(
        spreadsheetId: String,
        expense: Expense,
        categories: Map<String, com.ledgerly.expense.data.local.entity.CategoryEntity>,
        clients: Map<String, com.ledgerly.expense.data.local.entity.ClientEntity>,
        businesses: Map<String, com.ledgerly.expense.data.local.entity.BusinessEntity>,
    ) {
        val categoryName = categories[expense.expenseCategoryId]?.name ?: "Uncategorized"
        val irsName = ScheduleCCategory.fromLine(expense.scheduleCLine)
            ?.let { "Line ${it.line} — ${it.displayName}" } ?: expense.scheduleCLine
        val row = ExpenseSheetSchema.toRow(
            expense = expense,
            categoryName = categoryName,
            irsCategoryName = irsName,
            clientName = expense.clientId?.let { clients[it]?.name },
            businessName = expense.businessId?.let { businesses[it]?.name },
        )
        val rowId = sheetsService.upsertRow(spreadsheetId, expense.id, row, expense.remoteRowId)
        expenseRepository.markSynced(expense.id, rowId.takeIf { it > 0 })
    }
}
