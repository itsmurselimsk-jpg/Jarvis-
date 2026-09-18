package com.example.jarvis.generation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Standard XLSX File Generator.
 * Assembles a valid Office Open XML spreadsheet (.xlsx) as a proper ZIP container
 * containing [Content_Types].xml, _rels/.rels, xl/_rels/workbook.xml.rels,
 * xl/workbook.xml, and xl/worksheets/sheet1.xml.
 * Supports typed cells (numbers vs text strings) and headers.
 */
class XlsxFileGenerator : FileGenerator {
    override val format: GeneratedFileFormat = GeneratedFileFormat.XLSX

    override suspend fun generate(request: GenerationRequest): ByteArray = withContext(Dispatchers.Default) {
        val bos = ByteArrayOutputStream()
        val zos = ZipOutputStream(bos)

        // 1. [Content_Types].xml
        writeZipEntry(zos, "[Content_Types].xml", buildContentTypesXml())

        // 2. _rels/.rels
        writeZipEntry(zos, "_rels/.rels", buildRootRelsXml())

        // 3. xl/_rels/workbook.xml.rels
        writeZipEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml())

        // 4. xl/workbook.xml
        writeZipEntry(zos, "xl/workbook.xml", buildWorkbookXml(request))

        // 5. xl/worksheets/sheet1.xml
        writeZipEntry(zos, "xl/worksheets/sheet1.xml", buildSheetXml(request))

        zos.finish()
        zos.flush()
        bos.toByteArray()
    }

    private fun writeZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(Charsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildContentTypesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>""".trimIndent()
    }

    private fun buildRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".trimIndent()
    }

    private fun buildWorkbookRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""".trimIndent()
    }

    private fun buildWorkbookXml(request: GenerationRequest): String {
        val sheetName = request.title.ifBlank { "Sheet1" }.replace(Regex("""[:\\/?*\[\]]"""), " ").take(30)
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="${escapeXml(sheetName)}" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>""".trimIndent()
    }

    private fun buildSheetXml(request: GenerationRequest): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.appendLine("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.appendLine("  <sheetData>")

        var rowIndex = 1

        if (request.tables.isNotEmpty()) {
            val table = request.tables.first()
            // Header Row
            if (table.headers.isNotEmpty()) {
                sb.appendLine("    <row r=\"$rowIndex\">")
                table.headers.forEachIndexed { colIdx, headerText ->
                    val cellRef = getCellReference(colIdx, rowIndex)
                    sb.appendLine("      <c r=\"$cellRef\" t=\"inlineStr\"><is><t>${escapeXml(headerText)}</t></is></c>")
                }
                sb.appendLine("    </row>")
                rowIndex++
            }

            // Data Rows
            table.rows.forEach { row ->
                sb.appendLine("    <row r=\"$rowIndex\">")
                row.forEachIndexed { colIdx, cellValue ->
                    val cellRef = getCellReference(colIdx, rowIndex)
                    val trimmed = cellValue.trim()
                    val isNumber = trimmed.matches(Regex("""^-?\d+(\.\d+)?$"""))
                    if (isNumber) {
                        sb.appendLine("      <c r=\"$cellRef\"><v>$trimmed</v></c>")
                    } else {
                        sb.appendLine("      <c r=\"$cellRef\" t=\"inlineStr\"><is><t>${escapeXml(cellValue)}</t></is></c>")
                    }
                }
                sb.appendLine("    </row>")
                rowIndex++
            }
        } else if (request.rawContent.isNotBlank()) {
            // Parse CSV / delimited rawContent
            val lines = request.rawContent.lines().filter { it.isNotBlank() }
            lines.forEach { line ->
                val cells = line.split(Regex("""[,;\t|]""")).map { it.trim() }
                sb.appendLine("    <row r=\"$rowIndex\">")
                cells.forEachIndexed { colIdx, cellValue ->
                    val cellRef = getCellReference(colIdx, rowIndex)
                    val isNumber = cellValue.matches(Regex("""^-?\d+(\.\d+)?$"""))
                    if (isNumber) {
                        sb.appendLine("      <c r=\"$cellRef\"><v>$cellValue</v></c>")
                    } else {
                        sb.appendLine("      <c r=\"$cellRef\" t=\"inlineStr\"><is><t>${escapeXml(cellValue)}</t></is></c>")
                    }
                }
                sb.appendLine("    </row>")
                rowIndex++
            }
        } else if (request.sections.isNotEmpty()) {
            // Write sections as key-value rows
            sb.appendLine("    <row r=\"$rowIndex\">")
            sb.appendLine("      <c r=\"A1\" t=\"inlineStr\"><is><t>Section</t></is></c>")
            sb.appendLine("      <c r=\"B1\" t=\"inlineStr\"><is><t>Content</t></is></c>")
            sb.appendLine("    </row>")
            rowIndex++

            request.sections.forEach { s ->
                sb.appendLine("    <row r=\"$rowIndex\">")
                sb.appendLine("      <c r=\"A$rowIndex\" t=\"inlineStr\"><is><t>${escapeXml(s.title ?: "")}</t></is></c>")
                sb.appendLine("      <c r=\"B$rowIndex\" t=\"inlineStr\"><is><t>${escapeXml(s.content.ifBlank { s.items.joinToString(", ") })}</t></is></c>")
                sb.appendLine("    </row>")
                rowIndex++
            }
        }

        sb.appendLine("  </sheetData>")
        sb.appendLine("</worksheet>")
        return sb.toString()
    }

    private fun getCellReference(colIndex: Int, rowIndex: Int): String {
        var n = colIndex
        var colStr = ""
        while (n >= 0) {
            colStr = ('A'.code + (n % 26)).toChar() + colStr
            n = (n / 26) - 1
        }
        return "$colStr$rowIndex"
    }
}
