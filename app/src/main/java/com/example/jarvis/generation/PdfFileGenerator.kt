package com.example.jarvis.generation

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Standard PDF Generator utilizing Android native PdfDocument.
 * Generates valid ISO 32000 PDF documents with titles, headings, paragraphs, lists, tables, and page breaks.
 */
class PdfFileGenerator : FileGenerator {
    override val format: GeneratedFileFormat = GeneratedFileFormat.PDF

    companion object {
        const val PAGE_WIDTH = 595 // Standard A4 points at 72dpi
        const val PAGE_HEIGHT = 842
        const val MARGIN_HORIZONTAL = 40
        const val MARGIN_TOP = 50
        const val MARGIN_BOTTOM = 50
        const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_HORIZONTAL * 2)
    }

    override suspend fun generate(request: GenerationRequest): ByteArray = withContext(Dispatchers.Default) {
        val document = PdfDocument()
        val stream = ByteArrayOutputStream()

        try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var currentY = MARGIN_TOP.toFloat()

            // Setup paints
            val titlePaint = Paint().apply {
                color = Color.rgb(24, 43, 73) // Deep Navy
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val headingPaint = Paint().apply {
                color = Color.rgb(40, 70, 110)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(45, 45, 45)
                textSize = 10f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val bulletPaint = Paint().apply {
                color = Color.rgb(70, 130, 180) // Steel Blue
                textSize = 10f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(210, 215, 225)
                strokeWidth = 1f
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.rgb(20, 30, 50)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val tableCellPaint = Paint().apply {
                color = Color.rgb(50, 50, 50)
                textSize = 9f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val tableBgPaint = Paint().apply {
                color = Color.rgb(240, 243, 248)
                style = Paint.Style.FILL
            }

            fun newPage() {
                document.finishPage(page)
                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN_TOP.toFloat()
            }

            fun ensureSpace(neededHeight: Float) {
                if (currentY + neededHeight > (PAGE_HEIGHT - MARGIN_BOTTOM)) {
                    newPage()
                }
            }

            // Draw Header Title
            val titleText = request.title.ifBlank { request.fileName.substringBeforeLast('.') }
            if (titleText.isNotBlank()) {
                ensureSpace(45f)
                canvas.drawText(titleText, MARGIN_HORIZONTAL.toFloat(), currentY, titlePaint)
                currentY += 24f

                // Draw accent line below title
                canvas.drawLine(
                    MARGIN_HORIZONTAL.toFloat(),
                    currentY - 6f,
                    (PAGE_WIDTH - MARGIN_HORIZONTAL).toFloat(),
                    currentY - 6f,
                    linePaint
                )
                currentY += 12f
            }

            // Draw Sections
            if (request.sections.isNotEmpty()) {
                request.sections.forEach { section ->
                    if (!section.title.isNullOrBlank()) {
                        ensureSpace(35f)
                        canvas.drawText(section.title, MARGIN_HORIZONTAL.toFloat(), currentY, headingPaint)
                        currentY += 18f
                    }

                    if (section.content.isNotBlank()) {
                        val wrappedLines = wrapText(section.content, bodyPaint, CONTENT_WIDTH.toFloat())
                        wrappedLines.forEach { line ->
                            ensureSpace(14f)
                            canvas.drawText(line, MARGIN_HORIZONTAL.toFloat(), currentY, bodyPaint)
                            currentY += 14f
                        }
                        currentY += 6f
                    }

                    if (section.items.isNotEmpty()) {
                        section.items.forEach { item ->
                            val wrappedItemLines = wrapText(item, bodyPaint, (CONTENT_WIDTH - 16).toFloat())
                            wrappedItemLines.forEachIndexed { index, line ->
                                ensureSpace(14f)
                                if (index == 0) {
                                    canvas.drawText("•", MARGIN_HORIZONTAL.toFloat() + 4f, currentY, bulletPaint)
                                    canvas.drawText(line, MARGIN_HORIZONTAL.toFloat() + 16f, currentY, bodyPaint)
                                } else {
                                    canvas.drawText(line, MARGIN_HORIZONTAL.toFloat() + 16f, currentY, bodyPaint)
                                }
                                currentY += 14f
                            }
                        }
                        currentY += 6f
                    }
                }
            } else if (request.rawContent.isNotBlank()) {
                val wrappedLines = wrapText(request.rawContent, bodyPaint, CONTENT_WIDTH.toFloat())
                wrappedLines.forEach { line ->
                    ensureSpace(14f)
                    canvas.drawText(line, MARGIN_HORIZONTAL.toFloat(), currentY, bodyPaint)
                    currentY += 14f
                }
                currentY += 10f
            }

            // Draw Tables
            if (request.tables.isNotEmpty()) {
                request.tables.forEach { table ->
                    if (!table.title.isNullOrBlank()) {
                        ensureSpace(25f)
                        canvas.drawText(table.title, MARGIN_HORIZONTAL.toFloat(), currentY, headingPaint)
                        currentY += 16f
                    }

                    val colCount = maxOf(1, table.headers.size.coerceAtLeast(table.rows.maxOfOrNull { it.size } ?: 0))
                    val colWidth = CONTENT_WIDTH.toFloat() / colCount
                    val rowHeight = 18f

                    // Draw Table Header
                    if (table.headers.isNotEmpty()) {
                        ensureSpace(rowHeight + 4f)
                        canvas.drawRect(
                            MARGIN_HORIZONTAL.toFloat(),
                            currentY - 12f,
                            (PAGE_WIDTH - MARGIN_HORIZONTAL).toFloat(),
                            currentY + rowHeight - 12f,
                            tableBgPaint
                        )
                        table.headers.forEachIndexed { i, h ->
                            val truncatedHeader = truncateToWidth(h, tableHeaderPaint, colWidth - 8f)
                            canvas.drawText(
                                truncatedHeader,
                                MARGIN_HORIZONTAL.toFloat() + (i * colWidth) + 4f,
                                currentY,
                                tableHeaderPaint
                            )
                        }
                        currentY += rowHeight
                    }

                    // Draw Table Rows
                    table.rows.forEach { row ->
                        ensureSpace(rowHeight)
                        row.forEachIndexed { i, cell ->
                            if (i < colCount) {
                                val truncatedCell = truncateToWidth(cell, tableCellPaint, colWidth - 8f)
                                canvas.drawText(
                                    truncatedCell,
                                    MARGIN_HORIZONTAL.toFloat() + (i * colWidth) + 4f,
                                    currentY,
                                    tableCellPaint
                                )
                            }
                        }
                        // row separator
                        canvas.drawLine(
                            MARGIN_HORIZONTAL.toFloat(),
                            currentY + 3f,
                            (PAGE_WIDTH - MARGIN_HORIZONTAL).toFloat(),
                            currentY + 3f,
                            linePaint
                        )
                        currentY += rowHeight
                    }
                    currentY += 10f
                }
            }

            // Finish the active page
            document.finishPage(page)
            document.writeTo(stream)
            stream.toByteArray()
        } finally {
            document.close()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.lines()

        for (p in paragraphs) {
            if (p.isBlank()) {
                result.add("")
                continue
            }
            val words = p.split(Regex("""\s+"""))
            val currentLine = StringBuilder()

            for (w in words) {
                val candidate = if (currentLine.isEmpty()) w else "$currentLine $w"
                if (paint.measureText(candidate) <= maxWidth) {
                    currentLine.clear()
                    currentLine.append(candidate)
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                        currentLine.clear()
                    }
                    // Handle single words that exceed maxWidth
                    if (paint.measureText(w) > maxWidth) {
                        var chunk = w
                        while (chunk.isNotEmpty() && paint.measureText(chunk) > maxWidth) {
                            var cut = chunk.length - 1
                            while (cut > 0 && paint.measureText(chunk.substring(0, cut)) > maxWidth) {
                                cut--
                            }
                            if (cut == 0) cut = 1
                            result.add(chunk.substring(0, cut))
                            chunk = chunk.substring(cut)
                        }
                        if (chunk.isNotEmpty()) currentLine.append(chunk)
                    } else {
                        currentLine.append(w)
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }

    private fun truncateToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }
}
