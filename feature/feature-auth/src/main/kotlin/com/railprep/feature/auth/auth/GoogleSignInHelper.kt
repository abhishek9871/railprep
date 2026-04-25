package com.railprep.feature.auth.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "AuthGoogle"

data class GoogleSignInPayload(
    val idToken: String,
    val rawNonce: String,
    val displayName: String?,
    val email: String?,
)

sealed class GoogleSignInError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    class UserCancelled(cause: Throwable? = null) : GoogleSignInError("Sign-in cancelled", cause)
    class NoCredential(cause: Throwable) : GoogleSignInError("No Google account on this device.", cause)
    class Misconfigured(cause: Throwable) : GoogleSignInError("Google sign-in is misconfigured.", cause)
    class Parse(cause: Throwable) : GoogleSignInError("Could not read Google credential.", cause)
    class Provider(cause: Throwable) : GoogleSignInError("Google sign-in failed.", cause)
}

/**
 * Thin wrapper around Credential Manager + Google ID Token. Generates a raw UUID nonce, hands
 * the SHA-256 of it to Google via setNonce, and returns the raw nonce so Supabase can verify
 * server-side via `signInWith(IDToken) { nonce = rawNonce }`.
 */
class GoogleSignInHelper(
    private val context: Context,
    private val webClientId: String,
) {
    suspend fun requestIdToken(): GoogleSignInPayload {
        require(webClientId.isNotBlank()) {
            "GOOGLE_WEB_CLIENT_ID is missing from local.properties — see README."
        }

        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = sha256Hex(rawNonce)

        // Button-initiated flow — always shows the Google account picker including accounts that
        // system-account discovery misses on some OEM skins (e.g. ColorOS).
        val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
            .setNonce(hashedNonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        val response = try {
            CredentialManager.create(context).getCredential(context, request)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInError.UserCancelled(e)
        } catch (e: NoCredentialException) {
            throw GoogleSignInError.NoCredential(e)
        } catch (e: GetCredentialProviderConfigurationException) {
            Log.w(TAG, "Credential provider misconfigured: ${e.message}")
            throw GoogleSignInError.Misconfigured(e)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential provider failure: ${e.javaClass.simpleName} ${e.message}")
            throw GoogleSignInError.Provider(e)
        }

        val parsed = try {
            GoogleIdTokenCredential.createFrom(response.credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw GoogleSignInError.Parse(e)
        }

        return GoogleSignInPayload(
            idToken = parsed.idToken,
            rawNonce = rawNonce,
            displayName = parsed.displayName,
            email = parsed.id,
        )
    }
}

private fun sha256Hex(input: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray())
        .joinToString(separator = "") { "%02x".format(it) }
