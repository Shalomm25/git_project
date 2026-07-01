package com.ledgerly.expense.data.local

import com.ledgerly.expense.data.local.entity.BusinessEntity
import com.ledgerly.expense.data.local.entity.CategoryEntity
import com.ledgerly.expense.data.local.entity.ClientEntity
import com.ledgerly.expense.data.local.entity.ExpenseEntity
import com.ledgerly.expense.data.local.entity.MileageTripEntity
import com.ledgerly.expense.data.local.entity.PaymentMethodEntity
import com.ledgerly.expense.data.local.entity.RecurringExpenseEntity
import com.ledgerly.expense.domain.model.Business
import com.ledgerly.expense.domain.model.Category
import com.ledgerly.expense.domain.model.Client
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.model.MileageTrip
import com.ledgerly.expense.domain.model.PaymentMethod
import com.ledgerly.expense.domain.model.RecurringExpense
import java.time.LocalDate

/** Pure mapping functions between Room entities and domain models. */

private fun List<String>.toCsv(): String? = if (isEmpty()) null else joinToString(",")
private fun String?.fromCsv(): List<String> =
    this?.split(",")?.map(String::trim)?.filter(String::isNotEmpty) ?: emptyList()

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    date = LocalDate.ofEpochDay(date),
    merchant = merchant,
    amountCents = amountCents,
    currency = currency,
    expenseCategoryId = expenseCategoryId,
    scheduleCLine = scheduleCLine,
    paymentMethod = paymentMethod,
    businessPurpose = businessPurpose,
    notes = notes,
    receiptLocalPath = receiptLocalPath,
    receiptRemoteUrl = receiptRemoteUrl,
    mileage = mileage,
    latitude = latitude,
    longitude = longitude,
    tags = tags.fromCsv(),
    clientId = clientId,
    businessId = businessId,
    recurringId = recurringId,
    syncStatus = syncStatus,
    remoteRowId = remoteRowId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    ownerUserId = ownerUserId,
)

fun Expense.toEntity(isDeleted: Boolean = false): ExpenseEntity = ExpenseEntity(
    id = id,
    ownerUserId = ownerUserId,
    date = date.toEpochDay(),
    merchant = merchant,
    amountCents = amountCents,
    currency = currency,
    expenseCategoryId = expenseCategoryId,
    scheduleCLine = scheduleCLine,
    paymentMethod = paymentMethod,
    businessPurpose = businessPurpose,
    notes = notes,
    receiptLocalPath = receiptLocalPath,
    receiptRemoteUrl = receiptRemoteUrl,
    mileage = mileage,
    latitude = latitude,
    longitude = longitude,
    tags = tags.toCsv(),
    clientId = clientId,
    businessId = businessId,
    recurringId = recurringId,
    syncStatus = syncStatus,
    remoteRowId = remoteRowId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
)

fun CategoryEntity.toDomain() = Category(id, name, scheduleCLine, iconKey, colorHex, isDefault, sortOrder, ownerUserId)
fun Category.toEntity() = CategoryEntity(id, ownerUserId, name, scheduleCLine, iconKey, colorHex, isDefault, sortOrder)

fun ClientEntity.toDomain() = Client(id, name, contact, ownerUserId)
fun Client.toEntity() = ClientEntity(id, ownerUserId, name, contact)

fun BusinessEntity.toDomain() = Business(id, name, ein, ownerUserId)
fun Business.toEntity() = BusinessEntity(id, ownerUserId, name, ein)

fun PaymentMethodEntity.toDomain() = PaymentMethod(id, name, type, ownerUserId)
fun PaymentMethod.toEntity() = PaymentMethodEntity(id, ownerUserId, name, type)

fun MileageTripEntity.toDomain() = MileageTrip(
    id, LocalDate.ofEpochDay(date), startLocation, endLocation, totalMiles,
    businessPurpose, ratePerMileCents, expenseId, syncStatus, ownerUserId,
)
fun MileageTrip.toEntity() = MileageTripEntity(
    id, ownerUserId, date.toEpochDay(), startLocation, endLocation, totalMiles,
    businessPurpose, ratePerMileCents, expenseId, syncStatus,
)

fun RecurringExpenseEntity.toDomain() = RecurringExpense(
    id, templateExpenseId, frequency, LocalDate.ofEpochDay(nextDueDate),
    lastGeneratedAt?.let(LocalDate::ofEpochDay), isActive, ownerUserId,
)
fun RecurringExpense.toEntity() = RecurringExpenseEntity(
    id, ownerUserId, templateExpenseId, frequency, nextDueDate.toEpochDay(),
    lastGeneratedAt?.toEpochDay(), isActive,
)
