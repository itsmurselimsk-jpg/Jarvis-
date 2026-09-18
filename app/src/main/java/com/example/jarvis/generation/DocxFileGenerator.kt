package com.example.jarvis.generation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Standard DOCX File Generator.
 * Assembles a valid Office Open XML document (.docx) as a proper ZIP container
 * containing [Content_Types].xml, _rels/.rels, word/document.xml, and word/_rels/document.xml.rels.
 * Supports document title, headings, paragraphs, bullet lists, and tables with safe XML escaping.
 */
class DocxFileGenerator : FileGenerator {
    override val format: GeneratedFileFormat = GeneratedFileFormat.DOCX

    override suspend fun generate(request: GenerationRequest): ByteArray = withContext(Dispatchers.Default) {
        val bos = ByteArrayOutputStream()
        val zos = ZipOutputStream(bos)

        // 1. [Content_Types].xml
        writeZipEntry(zos, "[Content_Types].xml", buildContentTypesXml())

        // 2. _rels/.rels
        writeZipEntry(zos, "_rels/.rels", buildRootRelsXml())

        // 3. word/_rels/document.xml.rels
        writeZipEntry(zos, "word/_rels/document.xml.rels", buildDocumentRelsXml())

        // 4. word/document.xml
        val documentXml = buildDocumentXml(request)
        writeZipEntry(zos, "word/document.xml", documentXml)

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
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>""".trimIndent()
    }

    private fun buildRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>""".trimIndent()
    }

    private fun buildDocumentRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>""".trimIndent()
    }

    private fun buildDocumentXml(request: GenerationRequest): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.appendLine("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.appendLine("  <w:body>")

        // Document Title
        val title = request.title.ifBlank { request.fileName.substringBeforeLast('.') }
        if (title.isNotBlank()) {
            sb.appendLine("    <w:p>")
            sb.appendLine("      <w:pPr>")
            sb.appendLine("        <w:jc w:val=\"left\"/>")
            sb.appendLine("        <w:spacing w:after=\"280\" w:before=\"100\"/>")
            sb.appendLine("        <w:rPr>")
            sb.appendLine("          <w:b/>")
            sb.appendLine("          <w:color w:val=\"182B49\"/>")
            sb.appendLine("          <w:sz w:val=\"40\"/>") // 20pt
            sb.appendLine("        </w:rPr>")
            sb.appendLine("      </w:pPr>")
            sb.appendLine("      <w:r><w:t xml:space=\"preserve\">${escapeXml(title)}</w:t></w:r>")
            sb.appendLine("    </w:p>")
        }

        // Sections
        if (request.sections.isNotEmpty()) {
            request.sections.forEach { section ->
                if (!section.title.isNullOrBlank()) {
                    val fontSize = if (section.level <= 1) "30" else "26" // 15pt or 13pt
                    sb.appendLine("    <w:p>")
                    sb.appendLine("      <w:pPr>")
                    sb.appendLine("        <w:spacing w:after=\"160\" w:before=\"240\"/>")
                    sb.appendLine("        <w:rPr>")
                    sb.appendLine("          <w:b/>")
                    sb.appendLine("          <w:color w:val=\"28466E\"/>")
                    sb.appendLine("          <w:sz w:val=\"$fontSize\"/>")
                    sb.appendLine("        </w:rPr>")
                    sb.appendLine("      </w:pPr>")
                    sb.appendLine("      <w:r><w:t xml:space=\"preserve\">${escapeXml(section.title)}</w:t></w:r>")
                    sb.appendLine("    </w:p>")
                }

                if (section.content.isNotBlank()) {
                    section.content.lines().forEach { line ->
                        sb.appendLine("    <w:p>")
                        sb.appendLine("      <w:pPr><w:spacing w:after=\"120\"/></w:pPr>")
                        if (section.isCodeBlock) {
                            sb.appendLine("      <w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/><w:sz w:val=\"20\"/></w:rPr>")
                        }
                        sb.appendLine("      <w:r><w:t xml:space=\"preserve\">${escapeXml(line)}</w:t></w:r>")
                        sb.appendLine("    </w:p>")
                    }
                }

                if (section.items.isNotEmpty()) {
                    section.items.forEach { item ->
                        sb.appendLine("    <w:p>")
                        sb.appendLine("      <w:pPr>")
                        sb.appendLine("        <w:ind w:left=\"400\"/>")
                        sb.appendLine("        <w:spacing w:after=\"80\"/>")
                        sb.appendLine("      </w:pPr>")
                        sb.appendLine("      <w:r><w:rPr><w:b/><w:color w:val=\"4682B4\"/></w:rPr><w:t xml:space=\"preserve\">•  </w:t></w:r>")
                        sb.appendLine("      <w:r><w:t xml:space=\"preserve\">${escapeXml(item)}</w:t></w:r>")
                        sb.appendLine("    </w:p>")
                    }
                }
            }
        } else if (request.rawContent.isNotBlank()) {
            request.rawContent.lines().forEach { line ->
                sb.appendLine("    <w:p>")
                sb.appendLine("      <w:pPr><w:spacing w:after=\"120\"/></w:pPr>")
                sb.appendLine("      <w:r><w:t xml:space=\"preserve\">${escapeXml(line)}</w:t></w:r>")
                sb.appendLine("    </w:p>")
            }
        }

        // Tables
        if (request.tables.isNotEmpty()) {
            request.tables.forEach { table ->
                if (!table.title.isNullOrBlank()) {
                    sb.appendLine("    <w:p>")
                    sb.appendLine("      <w:pPr>")
                    sb.appendLine("        <w:spacing w:after=\"120\" w:before=\"200\"/>")
                    sb.appendLine("        <w:rPr><w:b/><w:sz w:val=\"26\"/></w:rPr>")
                    sb.appendLine("      </w:pPr>")
                    sb.appendLine("      <w:r><w:t xml:space=\"preserve\">${escapeXml(table.title)}</w:t></w:r>")
                    sb.appendLine("    </w:p>")
                }

                val colCount = maxOf(1, table.headers.size.coerceAtLeast(table.rows.maxOfOrNull { it.size } ?: 0))
                sb.appendLine("    <w:tbl>")
                sb.appendLine("      <w:tblPr>")
                sb.appendLine("        <w:tblW w:w=\"0\" w:type=\"auto\"/>")
                sb.appendLine("        <w:tblBorders>")
                sb.appendLine("          <w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CCCCCC\"/>")
                sb.appendLine("          <w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CCCCCC\"/>")
                sb.appendLine("          <w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"EEEEEE\"/>")
                sb.appendLine("        </w:tblBorders>")
                sb.appendLine("      </w:tblPr>")

                // Table Headers
                if (table.headers.isNotEmpty()) {
                    sb.appendLine("      <w:tr>")
                    for (i in 0 until colCount) {
                        val headerText = table.headers.getOrElse(i) { "Col ${i + 1}" }
                        sb.appendLine("        <w:tc>")
                        sb.appendLine("          <w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F0F4F8\"/></w:tcPr>")
                        sb.appendLine("          <w:p><w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">${escapeXml(headerText)}</w:t></w:r></w:p>")
                        sb.appendLine("        </w:tc>")
                    }
                    sb.appendLine("      </w:tr>")
                }

                // Table Rows
                table.rows.forEach { row ->
                    sb.appendLine("      <w:tr>")
                    for (i in 0 until colCount) {
                        val cellText = row.getOrElse(i) { "" }
                        sb.appendLine("        <w:tc>")
                        sb.appendLine("          <w:p><w:r><w:t xml:space=\"preserve\">${escapeXml(cellText)}</w:t></w:r></w:p>")
                        sb.appendLine("        </w:tc>")
                    }
                    sb.appendLine("      </w:tr>")
                }
                sb.appendLine("    </w:tbl>")
            }
        }

        sb.appendLine("  </w:body>")
        sb.appendLine("</w:document>")
        return sb.toString()
    }
}
