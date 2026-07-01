package com.ledgerly.expense.domain.repository

import com.ledgerly.expense.core.AppResult
import kotlinx.coroutines.flow.Flow

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR }

data class SyncInfo(
    val state: SyncState = SyncState.IDLE,
    val lastSyncedAt: Long? = null,
    val pendingCount: Int = 0,
    val spreadsheetUrl: String? = null,
    val errorMessage: String? = null,
)

/**
 * Drives synchronization of local expenses with the user's Google Sheet and the
 * upload of receipts to their Google Drive. Implementations push pending records,
 * resolve conflicts (last-write-wins by updatedAt) and surface progress.
 */
interface SyncRepository {
    fun observeSyncInfo(): Flow<SyncInfo>

    /** One full reconciliation pass. Safe to call repeatedly / from WorkManager. */
    suspend fun syncNow(userId: String): AppResult<Unit>

    /** Ensure the backing spreadsheet exists; returns its URL. */
    suspend fun ensureSpreadsheet(userId: String): AppResult<String>

    suspend fun uploadPendingReceipts(userId: String): AppResult<Unit>
}
