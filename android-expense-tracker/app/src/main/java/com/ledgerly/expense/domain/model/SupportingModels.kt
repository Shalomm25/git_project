package com.ledgerly.expense.domain.model

import java.time.LocalDate

/** A client/project an expense can be attributed to. */
data class Client(
    val id: String,
    val name: String,
    val contact: String? = null,
    val ownerUserId: String,
)

/** A business entity (supports a future multi-business mode). */
data class Business(
    val id: String,
    val name: String,
    val ein: String? = null,
    val ownerUserId: String,
)

/** A payment method (card, cash, bank account...). */
data class PaymentMethod(
    val id: String,
    val name: String,
    val type: PaymentType,
    val ownerUserId: String,
)

enum class PaymentType { CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CHECK, DIGITAL_WALLET, OTHER }

/** A logged mileage trip; standard IRS rate applied to compute the deduction. */
data class MileageTrip(
    val id: String,
    val date: LocalDate,
    val startLocation: String,
    val endLocation: String,
    val totalMiles: Double,
    val businessPurpose: String?,
    val ratePerMileCents: Long,
    val expenseId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val ownerUserId: String,
) {
    /** Deduction value in cents = miles * IRS standard mileage rate. */
    val deductionCents: Long get() = Math.round(totalMiles * ratePerMileCents)
}

enum class RecurrenceFrequency { WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY }

/** A recurring expense template that auto-generates expenses and reminders. */
data class RecurringExpense(
    val id: String,
    val templateExpenseId: String,
    val frequency: RecurrenceFrequency,
    val nextDueDate: LocalDate,
    val lastGeneratedAt: LocalDate? = null,
    val isActive: Boolean = true,
    val ownerUserId: String,
)
