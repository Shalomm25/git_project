package com.ledgerly.expense.domain.model

/**
 * Tracks whether a locally-stored record has been reconciled with the user's
 * Google Sheet. Drives the offline-first sync engine (see ARCHITECTURE.md).
 */
enum class SyncStatus {
    /** Created/edited locally, not yet pushed. */
    PENDING,

    /** Successfully reflected in the remote sheet. */
    SYNCED,

    /** Last sync attempt failed; will be retried with backoff. */
    FAILED,
}
