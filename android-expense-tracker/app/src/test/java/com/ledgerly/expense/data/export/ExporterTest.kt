package com.ledgerly.expense.data.export

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.ZipFile

class ExporterTest {

    @get:Rule val tmp = TemporaryFolder()

    private val header = listOf("Id", "Merchant", "Amount")
    private val rows = listOf(
        listOf("1", "Staples", "49.99"),
        listOf("2", "Cafe, Inc", "12.00"), // contains a comma -> must be quoted
        listOf("3", "He said \"hi\"", "5.00"),
    )

    @Test
    fun `csv quotes fields with commas and quotes`() {
        val file = tmp.newFile("out.csv")
        CsvExporter.write(file, header, rows)
        val lines = file.readLines()

        assertThat(lines[0]).isEqualTo("Id,Merchant,Amount")
        assertThat(lines[2]).isEqualTo("2,\"Cafe, Inc\",12.00")
        assertThat(lines[3]).isEqualTo("3,\"He said \"\"hi\"\"\",5.00")
    }

    @Test
    fun `xlsx is a valid zip containing the expected OPC parts`() {
        val file = tmp.newFile("out.xlsx")
        XlsxExporter.write(file, "Expenses", header, rows)

        ZipFile(file).use { zip ->
            val names = zip.entries().toList().map { it.name }
            assertThat(names).containsAtLeast(
                "[Content_Types].xml",
                "xl/workbook.xml",
                "xl/worksheets/sheet1.xml",
            )
            val sheet = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml"))
                .readBytes().decodeToString()
            // Numeric cells written as <v>, text as inlineStr.
            assertThat(sheet).contains("<v>49.99</v>")
            assertThat(sheet).contains("Staples")
        }
    }
}
