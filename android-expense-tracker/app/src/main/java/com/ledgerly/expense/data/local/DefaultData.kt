package com.ledgerly.expense.data.local

import com.ledgerly.expense.data.local.entity.CategoryEntity
import com.ledgerly.expense.data.local.entity.PaymentMethodEntity
import com.ledgerly.expense.domain.model.PaymentType
import com.ledgerly.expense.domain.model.ScheduleCCategory
import java.util.UUID

/**
 * Seed data for new users. Every default expense category is pre-mapped to an
 * IRS Schedule C line so expenses are tax-classified the moment they're entered.
 */
object DefaultData {

    private data class Seed(
        val name: String,
        val scheduleC: ScheduleCCategory,
        val icon: String,
        val color: String,
    )

    private val categories = listOf(
        Seed("Advertising", ScheduleCCategory.ADVERTISING, "campaign", "#E8590C"),
        Seed("Office Supplies", ScheduleCCategory.OFFICE_EXPENSE, "inventory", "#1971C2"),
        Seed("Software", ScheduleCCategory.OFFICE_EXPENSE, "apps", "#6741D9"),
        Seed("Computer Equipment", ScheduleCCategory.DEPRECIATION, "computer", "#3B5BDB"),
        Seed("Internet", ScheduleCCategory.UTILITIES, "wifi", "#0CA678"),
        Seed("Phone", ScheduleCCategory.UTILITIES, "smartphone", "#0C8599"),
        Seed("Business Meals", ScheduleCCategory.MEALS, "restaurant", "#F08C00"),
        Seed("Travel", ScheduleCCategory.TRAVEL, "flight_takeoff", "#1098AD"),
        Seed("Airfare", ScheduleCCategory.TRAVEL, "flight", "#1098AD"),
        Seed("Hotels", ScheduleCCategory.TRAVEL, "hotel", "#0B7285"),
        Seed("Gas", ScheduleCCategory.CAR_AND_TRUCK, "local_gas_station", "#E03131"),
        Seed("Parking", ScheduleCCategory.CAR_AND_TRUCK, "local_parking", "#C2255C"),
        Seed("Tolls", ScheduleCCategory.CAR_AND_TRUCK, "toll", "#A61E4D"),
        Seed("Shipping", ScheduleCCategory.OFFICE_EXPENSE, "local_shipping", "#5C940D"),
        Seed("Education", ScheduleCCategory.OTHER_EXPENSES, "school", "#5F3DC4"),
        Seed("Professional Services", ScheduleCCategory.LEGAL_AND_PROFESSIONAL, "gavel", "#364FC7"),
        Seed("Insurance", ScheduleCCategory.INSURANCE, "shield", "#2B8A3E"),
        Seed("Rent", ScheduleCCategory.RENT_OTHER, "home_work", "#9C36B5"),
        Seed("Utilities", ScheduleCCategory.UTILITIES, "bolt", "#0CA678"),
        Seed("Home Office", ScheduleCCategory.HOME_OFFICE, "chair", "#7048E8"),
        Seed("Vehicle Maintenance", ScheduleCCategory.CAR_AND_TRUCK, "build", "#E8590C"),
        Seed("Banking Fees", ScheduleCCategory.COMMISSIONS_AND_FEES, "account_balance", "#1864AB"),
        Seed("Equipment", ScheduleCCategory.DEPRECIATION, "precision_manufacturing", "#3B5BDB"),
        Seed("Contractors", ScheduleCCategory.CONTRACT_LABOR, "engineering", "#2B8A3E"),
        Seed("Licenses", ScheduleCCategory.TAXES_AND_LICENSES, "badge", "#0B7285"),
        Seed("Taxes", ScheduleCCategory.TAXES_AND_LICENSES, "receipt_long", "#495057"),
        Seed("Miscellaneous", ScheduleCCategory.OTHER_EXPENSES, "more_horiz", "#868E96"),
    )

    fun defaultCategories(userId: String): List<CategoryEntity> =
        categories.mapIndexed { index, seed ->
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                ownerUserId = userId,
                name = seed.name,
                scheduleCLine = seed.scheduleC.line,
                iconKey = seed.icon,
                colorHex = seed.color,
                isDefault = true,
                sortOrder = index,
            )
        }

    fun defaultPaymentMethods(userId: String): List<PaymentMethodEntity> = listOf(
        "Business Credit Card" to PaymentType.CREDIT_CARD,
        "Business Debit Card" to PaymentType.DEBIT_CARD,
        "Cash" to PaymentType.CASH,
        "Bank Transfer" to PaymentType.BANK_TRANSFER,
        "Check" to PaymentType.CHECK,
    ).map { (name, type) ->
        PaymentMethodEntity(UUID.randomUUID().toString(), userId, name, type)
    }
}
