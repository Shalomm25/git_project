package com.ledgerly.expense.data.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.ledgerly.expense.core.util.Money
import com.ledgerly.expense.domain.model.ScheduleCReport
import java.io.File

/**
 * Renders a tax-ready Schedule C summary to a US-Letter PDF using the platform
 * [PdfDocument] (no third-party PDF library). Paginates automatically when the
 * line list overflows a page.
 */
object PdfExporter {

    private const val PAGE_WIDTH = 612 // 8.5in * 72
    private const val PAGE_HEIGHT = 792 // 11in * 72
    private const val MARGIN = 48f

    fun write(file: File, report: ScheduleCReport, currency: String) {
        val doc = PdfDocument()
        val title = Paint().apply { textSize = 20f; typeface = Typeface.DEFAULT_BOLD; color = Color.BLACK }
        val subtitle = Paint().apply { textSize = 11f; color = Color.DKGRAY }
        val body = Paint().apply { textSize = 12f; color = Color.BLACK }
        val bold = Paint().apply { textSize = 12f; typeface = Typeface.DEFAULT_BOLD; color = Color.BLACK }
        val rule = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val amountPaint = Paint(body).apply { textAlign = Paint.Align.RIGHT }
        val amountBold = Paint(bold).apply { textAlign = Paint.Align.RIGHT }

        var pageNum = 1
        var page = doc.startPage(pageInfo(pageNum))
        var canvas = page.canvas
        var y = MARGIN + 8

        canvas.drawText("Ledgerly — Schedule C Summary", MARGIN, y, title)
        y += 22
        canvas.drawText("Tax year ${report.taxYear}", MARGIN, y, subtitle)
        y += 14
        canvas.drawText("Estimated deductible business expenses. Not tax advice.", MARGIN, y, subtitle)
        y += 24
        canvas.drawText("IRS Line", MARGIN, y, bold)
        canvas.drawText("Amount", PAGE_WIDTH - MARGIN, y, amountBold)
        y += 6
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rule)
        y += 18

        for (line in report.lines) {
            if (y > PAGE_HEIGHT - MARGIN - 80) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(pageInfo(pageNum))
                canvas = page.canvas
                y = MARGIN + 8
            }
            canvas.drawText(
                "Line ${line.category.line} · ${line.category.displayName} (${line.expenseCount})",
                MARGIN, y, body,
            )
            canvas.drawText(Money.format(line.totalCents, currency), PAGE_WIDTH - MARGIN, y, amountPaint)
            y += 20
        }

        y += 6
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rule)
        y += 20
        canvas.drawText("Mileage (${report.mileageTotalMiles} mi)", MARGIN, y, body)
        canvas.drawText(Money.format(report.mileageDeductionCents, currency), PAGE_WIDTH - MARGIN, y, amountPaint)
        y += 24
        canvas.drawText("Total deductible", MARGIN, y, bold)
        canvas.drawText(Money.format(report.totalDeductibleCents, currency), PAGE_WIDTH - MARGIN, y, amountBold)

        doc.finishPage(page)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    private fun pageInfo(pageNum: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
}
