package io.github.raghavsatyadev.support.sign_in

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreApp

/**
 * Utility class to facilitate Google Sign-In using Credential Manager.
 *
 * @property activity The activity context used for initiating sign-in.
 */
class GoogleSignInUtil(private val activity: Activity) {
    // Initialize Credential Manager
    private val credentialManager: CredentialManager = CredentialManager.Companion.create(activity)
    private val credentialRequest: GetCredentialRequest

    init {
        // Retrieve the server client ID from resources
        val serverClientId = CoreApp.Companion.instance.getString(R.string.google_web_client_id)

        // Configure Google ID option for authorized accounts
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        // Build the credential request with the Google ID option
        credentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    /**
     * Initiates the sign-in process.
     *
     * @param onSuccess Callback function invoked upon successful sign-in with
     *    the obtained ID token.
     * @param onFailure Callback function invoked upon sign-in failure with the
     *    encountered exception.
     */
    suspend fun startSignIn(
        onSuccess: (idToken: String) -> Unit,
        onFailure: (exception: Exception) -> Unit,
    ) {
        try {
            // Request credentials using Credential Manager
            val result = credentialManager.getCredential(
                request = credentialRequest,
                context = activity,
            )
            // Handle successful sign-in
            handleSignInSuccess(result, onSuccess, onFailure)
        } catch (e: Exception) {
            // Handle sign-in failure
            onFailure(e)
        }
    }

    /**
     * Handles successful sign-in by processing the obtained credentials.
     *
     * @param result The response containing the obtained credential.
     * @param onSuccess Callback function invoked with the obtained ID token.
     * @param onFailure Callback function invoked with the encountered
     *    exception.
     */
    private fun handleSignInSuccess(
        result: GetCredentialResponse,
        onSuccess: (idToken: String) -> Unit,
        onFailure: (exception: Exception) -> Unit,
    ) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Parse the Google ID token credential
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.Companion.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        // Invoke the success callback with the obtained ID token
                        onSuccess(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        // Invoke the failure callback with the parsing exception
                        onFailure(e)
                    }
                } else {
                    // Invoke the failure callback with an unexpected credential type exception
                    onFailure(Exception("Unexpected type of credential"))
                }
            }

            else -> {
                // Invoke the failure callback with an unexpected credential type exception
                onFailure(Exception("Unexpected type of credential"))
            }
        }
    }
}