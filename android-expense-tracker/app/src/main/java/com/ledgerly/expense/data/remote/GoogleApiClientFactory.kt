package com.ledgerly.expense.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds authenticated Google Sheets/Drive clients for the currently signed-in
 * Google account. Uses the least-privilege scopes: `spreadsheets` (to write the
 * ledger) and `drive.file` (to create/manage only files this app created).
 */
@Singleton
class GoogleApiClientFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val scopes: List<String> = listOf(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE_FILE)

    /** Null when the user has not granted Google access yet. */
    fun credentialOrNull(): GoogleAccountCredential? {
        val account = GoogleSignIn.getLastSignedInAccount(context)?.account ?: return null
        return GoogleAccountCredential.usingOAuth2(context, scopes).apply {
            selectedAccount = account
        }
    }

    fun sheets(credential: GoogleAccountCredential): Sheets =
        Sheets.Builder(transport, jsonFactory, credential)
            .setApplicationName(APP_NAME)
            .build()

    fun drive(credential: GoogleAccountCredential): Drive =
        Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName(APP_NAME)
            .build()

    private val transport by lazy { GoogleNetHttpTransport.newTrustedTransport() }
    private val jsonFactory by lazy { GsonFactory.getDefaultInstance() }

    private companion object {
        const val APP_NAME = "Ledgerly"
    }
}
