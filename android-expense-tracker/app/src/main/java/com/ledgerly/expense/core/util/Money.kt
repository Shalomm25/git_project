package com.ledgerly.expense.core.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Money is stored everywhere as an integer number of minor units (cents) in a
 * [Long] to avoid floating point rounding errors. These helpers convert to/from
 * user-facing decimal strings and formatted currency.
 */
object Money {

    /** Convert a user-entered decimal string (e.g. "12.50") to cents (1250). */
    fun parseToCents(input: String): Long? {
        val cleaned = input.trim().replace(",", "").removePrefix("$")
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        return (value * 100).roundToLong()
    }

    /** Convert cents (1250) to a plain decimal string ("12.50"). */
    fun centsToPlainString(cents: Long): String =
        String.format(Locale.US, "%.2f", cents / 100.0)

    /** Format cents as localized currency ("$12.50"). */
    fun format(cents: Long, currencyCode: String = "USD", locale: Locale = Locale.US): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        runCatching { format.currency = Currency.getInstance(currencyCode) }
        return format.format(cents / 100.0)
    }

    /** Compact format for charts/tiles ("$1.2K", "$3.4M"). */
    fun formatCompact(cents: Long, currencyCode: String = "USD"): String {
        val dollars = cents / 100.0
        val symbol = runCatching { Currency.getInstance(currencyCode).symbol }.getOrDefault("$")
        return when {
            dollars >= 1_000_000 -> "$symbol${"%.1f".format(dollars / 1_000_000)}M"
            dollars >= 1_000 -> "$symbol${"%.1f".format(dollars / 1_000)}K"
            else -> format(cents, currencyCode)
        }
    }
}
