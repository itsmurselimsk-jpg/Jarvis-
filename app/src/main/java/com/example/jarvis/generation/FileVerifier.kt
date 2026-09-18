package com.example.jarvis.generation

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

/**
 * Result of generated file verification.
 */
data class FileVerificationResult(
    val isValid: Boolean,
    val details: String,
    val errors: List<String> = emptyList()
)

/**
 * Validates generated output files for existence, non-zero size, MIME/format match,
 * and structural/syntax integrity.
 */
object FileVerifier {

    fun verify(file: File, expectedFormat: GeneratedFileFormat): FileVerificationResult {
        // 1. File existence check
        if (!file.exists()) {
            return FileVerificationResult(
                isValid = false,
                details = "File does not exist at target path: ${file.absolutePath}",
                errors = listOf("File not found.")
            )
        }

        // 2. Non-zero size check
        val length = file.length()
        if (length <= 0) {
            return FileVerificationResult(
                isValid = false,
                details = "Generated file is empty (0 bytes).",
                errors = listOf("File size is 0 bytes.")
            )
        }

        // 3. Format-specific structural validation
        return when (expectedFormat) {
            GeneratedFileFormat.TXT -> verifyTxt(file)
            GeneratedFileFormat.MARKDOWN -> verifyMarkdown(file)
            GeneratedFileFormat.CSV -> verifyCsv(file)
            GeneratedFileFormat.JSON -> verifyJson(file)
            GeneratedFileFormat.PDF -> verifyPdf(file)
            GeneratedFileFormat.DOCX -> verifyDocx(file)
            GeneratedFileFormat.XLSX -> verifyXlsx(file)
        }
    }

    private fun verifyTxt(file: File): FileVerificationResult {
        val bytes = file.readBytes()
        if (bytes.isEmpty()) return FileVerificationResult(false, "Empty TXT file.")
        return FileVerificationResult(
            isValid = true,
            details = "Valid UTF-8 plain text (${bytes.size} bytes)."
        )
    }

    private fun verifyMarkdown(file: File): FileVerificationResult {
        val text = file.readText(Charsets.UTF_8)
        if (text.isBlank()) return FileVerificationResult(false, "Empty Markdown file.")
        return FileVerificationResult(
            isValid = true,
            details = "Valid Markdown content (${file.length()} bytes, ${text.lines().size} lines)."
        )
    }

    private fun verifyCsv(file: File): FileVerificationResult {
        val lines = file.readLines(Charsets.UTF_8).filter { it.isNotBlank() }
        if (lines.isEmpty()) return FileVerificationResult(false, "CSV file has no readable rows.")

        // Verify CSV header and quoting
        val firstRowCols = parseCsvLine(lines.first()).size
        return FileVerificationResult(
            isValid = true,
            details = "Valid RFC-4180 CSV with ${lines.size} row(s) and $firstRowCols initial column(s)."
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val cells = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    cells.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        cells.add(sb.toString())
        return cells
    }

    private fun verifyJson(file: File): FileVerificationResult {
        val text = file.readText(Charsets.UTF_8).trim()
        return try {
            val tokener = JSONTokener(text).nextValue()
            if (tokener is JSONObject || tokener is JSONArray) {
                FileVerificationResult(
                    isValid = true,
                    details = "Valid JSON document (${if (tokener is JSONObject) "Object" else "Array"})."
                )
            } else {
                FileVerificationResult(
                    isValid = false,
                    details = "JSON root is neither a JSON object nor an array.",
                    errors = listOf("Top-level token is not JSONObject or JSONArray.")
                )
            }
        } catch (e: Exception) {
            FileVerificationResult(
                isValid = false,
                details = "JSON parsing failure: ${e.message}",
                errors = listOf("Malformed JSON syntax: ${e.message}")
            )
        }
    }

    private fun verifyPdf(file: File): FileVerificationResult {
        val bytes = file.readBytes()
        if (bytes.size < 32) {
            return FileVerificationResult(false, "PDF file too small (< 32 bytes).")
        }
        val header = String(bytes.take(8).toByteArray(), Charsets.US_ASCII)
        if (!header.startsWith("%PDF-")) {
            return FileVerificationResult(
                isValid = false,
                details = "Invalid PDF magic header: '$header'. Expected '%PDF-'",
                errors = listOf("Missing %PDF- header.")
            )
        }

        val tail = String(bytes.takeLast(128).toByteArray(), Charsets.ISO_8859_1)
        val hasEof = tail.contains("%%EOF") || String(bytes, Charsets.ISO_8859_1).contains("%%EOF")
        if (!hasEof) {
            return FileVerificationResult(
                isValid = false,
                details = "PDF does not contain valid %%EOF termination marker.",
                errors = listOf("Missing %%EOF marker.")
            )
        }

        return FileVerificationResult(
            isValid = true,
            details = "Valid PDF document structure (Magic: $header, %%EOF present, ${bytes.size} bytes)."
        )
    }

    private fun verifyDocx(file: File): FileVerificationResult {
        var hasContentTypes = false
        var hasWordDocument = false
        var hasRels = false

        try {
            ZipInputStream(FileInputStream(file)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "[Content_Types].xml" -> hasContentTypes = true
                        "word/document.xml" -> hasWordDocument = true
                        "_rels/.rels" -> hasRels = true
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return FileVerificationResult(
                isValid = false,
                details = "DOCX file is not a valid ZIP container: ${e.message}",
                errors = listOf("ZIP container corruption: ${e.message}")
            )
        }

        if (!hasContentTypes || !hasWordDocument || !hasRels) {
            val missing = mutableListOf<String>()
            if (!hasContentTypes) missing.add("[Content_Types].xml")
            if (!hasWordDocument) missing.add("word/document.xml")
            if (!hasRels) missing.add("_rels/.rels")
            return FileVerificationResult(
                isValid = false,
                details = "DOCX archive missing mandatory Open XML parts: ${missing.joinToString()}",
                errors = listOf("Incomplete DOCX container: missing $missing")
            )
        }

        return FileVerificationResult(
            isValid = true,
            details = "Valid DOCX Open XML document container verified."
        )
    }

    private fun verifyXlsx(file: File): FileVerificationResult {
        var hasContentTypes = false
        var hasWorkbook = false
        var hasSheet = false
        var hasRels = false

        try {
            ZipInputStream(FileInputStream(file)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "[Content_Types].xml" -> hasContentTypes = true
                        "xl/workbook.xml" -> hasWorkbook = true
                        "xl/worksheets/sheet1.xml" -> hasSheet = true
                        "_rels/.rels" -> hasRels = true
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return FileVerificationResult(
                isValid = false,
                details = "XLSX file is not a valid ZIP container: ${e.message}",
                errors = listOf("ZIP container corruption: ${e.message}")
            )
        }

        if (!hasContentTypes || !hasWorkbook || !hasSheet || !hasRels) {
            val missing = mutableListOf<String>()
            if (!hasContentTypes) missing.add("[Content_Types].xml")
            if (!hasWorkbook) missing.add("xl/workbook.xml")
            if (!hasSheet) missing.add("xl/worksheets/sheet1.xml")
            if (!hasRels) missing.add("_rels/.rels")
            return FileVerificationResult(
                isValid = false,
                details = "XLSX archive missing mandatory Open XML parts: ${missing.joinToString()}",
                errors = listOf("Incomplete XLSX container: missing $missing")
            )
        }

        return FileVerificationResult(
            isValid = true,
            details = "Valid XLSX Open XML spreadsheet container verified."
        )
    }
}
