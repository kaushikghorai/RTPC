package com.example.rtpc.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

data class PageSource(
    val uri: Uri,
    val pageIndex: Int,
    val preview: Bitmap? = null
)

object PdfGenerator {

    /**
     * Extracts preview bitmaps for all pages of a PDF.
     */
    fun getPdfPages(context: Context, uri: Uri): List<PageSource> {
        val pages = mutableListOf<PageSource>()
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pages.add(PageSource(uri, i, bitmap))
                    }
                }
                renderer.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pages
    }

    /**
     * Merges specific pages from multiple PDF URIs into a single output stream.
     */
    fun mergeSelectedPages(context: Context, selectedPages: List<PageSource>, outputStream: OutputStream): Boolean {
        val openStreams = mutableListOf<InputStream>()
        return try {
            val finalDoc = PDDocument()
            val cache = mutableMapOf<Uri, PDDocument>()
            
            selectedPages.forEach { pageSource ->
                val sourceDoc = cache.getOrPut(pageSource.uri) {
                    val inputStream = context.contentResolver.openInputStream(pageSource.uri)!!
                    openStreams.add(inputStream)
                    PDDocument.load(inputStream)
                }
                val page = sourceDoc.getPage(pageSource.pageIndex)
                finalDoc.importPage(page)
            }
            
            finalDoc.save(outputStream)
            finalDoc.close()
            cache.values.forEach { it.close() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            openStreams.forEach { try { it.close() } catch (_: Exception) {} }
            try { outputStream.close() } catch (_: Exception) {}
        }
    }

    /**
     * Converts an image to a temporary PDF file.
     */
    fun convertImageToPdf(context: Context, uri: Uri): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    bitmap.recycle()
                }
            }
            
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pdf")
            FileOutputStream(tempFile).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts a TXT file to a temporary PDF file.
     */
    fun convertTxtToPdf(context: Context, uri: Uri): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val paint = android.graphics.Paint()
            paint.textSize = 12f
            
            val textLines = mutableListOf<String>()
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                reader.readLines().forEach { textLines.add(it) }
            }
            
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            var y = 40f
            textLines.forEach { line ->
                page.canvas.drawText(line, 40f, y, paint)
                y += 15f
            }
            pdfDocument.finishPage(page)
            
            val tempFile = File(context.cacheDir, "temp_txt_${System.currentTimeMillis()}.pdf")
            FileOutputStream(tempFile).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
