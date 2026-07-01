package com.ledgerly.expense.data.export

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Minimal, dependency-free .xlsx writer. An xlsx file is just a ZIP archive of
 * Open Packaging Convention XML parts; we emit the few parts Excel/Sheets need
 * and write cells as inline strings (no shared-strings table required).
 *
 * This keeps the APK lean — no Apache POI — while still producing a genuine
 * spreadsheet file rather than a renamed CSV.
 */
object XlsxExporter {

    fun write(file: File, sheetName: String, header: List<String>, rows: List<List<Any?>>) {
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            zip.put("[Content_Types].xml", contentTypes())
            zip.put("_rels/.rels", rootRels())
            zip.put("xl/workbook.xml", workbook(sheetName))
            zip.put("xl/_rels/workbook.xml.rels", workbookRels())
            zip.put("xl/worksheets/sheet1.xml", sheet(header, rows))
        }
    }

    private fun ZipOutputStream.put(path: String, content: String) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun contentTypes() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private fun rootRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbook(sheetName: String) = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets><sheet name="${escape(sheetName)}" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private fun workbookRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""

    private fun sheet(header: List<String>, rows: List<List<Any?>>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        appendRow(sb, 1, header)
        rows.forEachIndexed { index, row -> appendRow(sb, index + 2, row) }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun appendRow(sb: StringBuilder, rowNum: Int, cells: List<Any?>) {
        sb.append("<row r=\"$rowNum\">")
        cells.forEachIndexed { col, value ->
            val ref = "${columnLetter(col)}$rowNum"
            val text = value?.toString().orEmpty()
            val numeric = text.toDoubleOrNull() != null && text.isNotEmpty()
            if (numeric) {
                sb.append("<c r=\"$ref\"><v>$text</v></c>")
            } else {
                sb.append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${escape(text)}</t></is></c>")
            }
        }
        sb.append("</row>")
    }

    private fun columnLetter(index: Int): String {
        var i = index
        val sb = StringBuilder()
        do {
            sb.insert(0, ('A' + (i % 26)))
            i = i / 26 - 1
        } while (i >= 0)
        return sb.toString()
    }

    private fun escape(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")
}
