package com.ledgerly.expense.domain.model

/**
 * IRS Schedule C (Form 1040) expense lines. Every app expense category maps to
 * exactly one of these so we can produce a tax-ready Schedule C summary. Line
 * references follow the 2024 Schedule C, Part II (Expenses) and Part V (Other).
 *
 * This is the tax-architecture backbone: keeping the IRS mapping in one typed
 * place lets reports, exports, and a future tax estimator share a single source
 * of truth without re-deriving categorizations.
 */
enum class ScheduleCCategory(
    val line: String,
    val displayName: String,
) {
    ADVERTISING("8", "Advertising"),
    CAR_AND_TRUCK("9", "Car and truck expenses"),
    COMMISSIONS_AND_FEES("10", "Commissions and fees"),
    CONTRACT_LABOR("11", "Contract labor"),
    DEPLETION("12", "Depletion"),
    DEPRECIATION("13", "Depreciation and Section 179"),
    EMPLOYEE_BENEFITS("14", "Employee benefit programs"),
    INSURANCE("15", "Insurance (other than health)"),
    INTEREST_MORTGAGE("16a", "Interest — mortgage"),
    INTEREST_OTHER("16b", "Interest — other"),
    LEGAL_AND_PROFESSIONAL("17", "Legal and professional services"),
    OFFICE_EXPENSE("18", "Office expense"),
    PENSION_PROFIT_SHARING("19", "Pension and profit-sharing plans"),
    RENT_VEHICLES_EQUIPMENT("20a", "Rent/lease — vehicles, machinery, equipment"),
    RENT_OTHER("20b", "Rent/lease — other business property"),
    REPAIRS_AND_MAINTENANCE("21", "Repairs and maintenance"),
    SUPPLIES("22", "Supplies"),
    TAXES_AND_LICENSES("23", "Taxes and licenses"),
    TRAVEL("24a", "Travel"),
    MEALS("24b", "Deductible meals"),
    UTILITIES("25", "Utilities"),
    WAGES("26", "Wages"),
    OTHER_EXPENSES("27a", "Other expenses (Part V)"),
    HOME_OFFICE("30", "Home office (Form 8829)");

    companion object {
        fun fromLine(line: String): ScheduleCCategory? = entries.firstOrNull { it.line == line }
    }
}
