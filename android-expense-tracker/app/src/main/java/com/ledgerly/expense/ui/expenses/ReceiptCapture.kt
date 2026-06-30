package com.ledgerly.expense.ui.expenses

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helpers for receipt image files. Photos are written into app-private storage
 * and surfaced to the camera/gallery via [FileProvider] so no broad storage
 * permission is required.
 */
object ReceiptCapture {

    private fun receiptsDir(context: Context): File =
        File(context.filesDir, "receipts").apply { if (!exists()) mkdirs() }

    /** Create a new empty file + content Uri for the camera to write into. */
    fun newReceiptUri(context: Context): Pair<File, Uri> {
        val file = File(receiptsDir(context), "receipt_${System.nanoTime()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    /** Copy an imported gallery image into app storage; returns the local path. */
    fun importFrom(context: Context, source: Uri): String? {
        val dest = File(receiptsDir(context), "receipt_${System.nanoTime()}.jpg")
        return runCatching {
            context.contentResolver.openInputStream(source)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        }.getOrNull()
    }
}
