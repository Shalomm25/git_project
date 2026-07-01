package com.ledgerly.expense.data.export

import java.io.File

/** Writes expense rows to an RFC 4180 CSV file (opens cleanly in Excel/Sheets). */
object CsvExporter {

    fun write(file: File, header: List<String>, rows: List<List<Any?>>) {
        file.bufferedWriter().use { writer ->
            writer.appendLine(header.joinToString(",") { escape(it) })
            rows.forEach { row ->
                writer.appendLine(row.joinToString(",") { escape(it?.toString().orEmpty()) })
            }
        }
    }

    /** Quote fields containing comma, quote or newline; double embedded quotes. */
    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
