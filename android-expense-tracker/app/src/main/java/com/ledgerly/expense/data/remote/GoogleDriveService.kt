package com.ledgerly.expense.data.remote

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.model.Permission
import com.ledgerly.expense.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Uploads receipt images to a "Ledgerly Receipts" folder in the user's own
 * Google Drive (scope `drive.file`) and returns a shareable link to store in
 * the spreadsheet. Receipts never leave the user's account.
 */
class GoogleDriveService @Inject constructor(
    private val factory: GoogleApiClientFactory,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private fun client(): Drive =
        factory.credentialOrNull()?.let(factory::drive)
            ?: error("Google account not connected")

    suspend fun uploadReceipt(localPath: String, displayName: String): String = withContext(io) {
        val drive = client()
        val folderId = ensureReceiptsFolder(drive)
        val file = File(localPath)
        require(file.exists()) { "Receipt file not found: $localPath" }

        val metadata = DriveFile().apply {
            name = displayName
            parents = listOf(folderId)
        }
        val media = FileContent(guessMime(file.name), file)
        val uploaded = drive.files().create(metadata, media)
            .setFields("id, webViewLink")
            .execute()

        // Anyone-with-link reader so the URL stored in Sheets is openable.
        runCatching {
            drive.permissions().create(
                uploaded.id,
                Permission().setType("anyone").setRole("reader"),
            ).execute()
        }
        uploaded.webViewLink ?: "https://drive.google.com/file/d/${uploaded.id}/view"
    }

    private fun ensureReceiptsFolder(drive: Drive): String {
        val query = "mimeType = 'application/vnd.google-apps.folder' and " +
            "name = '$FOLDER_NAME' and trashed = false"
        val existing = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
            .files
        existing?.firstOrNull()?.let { return it.id }

        val folder = DriveFile().apply {
            name = FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        return drive.files().create(folder).setFields("id").execute().id
    }

    private fun guessMime(name: String): String = when {
        name.endsWith(".png", true) -> "image/png"
        name.endsWith(".webp", true) -> "image/webp"
        name.endsWith(".pdf", true) -> "application/pdf"
        else -> "image/jpeg"
    }

    private companion object {
        const val FOLDER_NAME = "Ledgerly Receipts"
    }
}
