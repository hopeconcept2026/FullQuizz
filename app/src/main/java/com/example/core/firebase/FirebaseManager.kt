package com.example.core.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Singleton Firebase Manager handling Authentication (Google Sign-In via Credential Manager)
 * and Cloud Firestore synchronization.
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"

    private var isInitialized = false
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance().apply {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
                firestoreSettings = settings
            }

            _currentUser.value = auth?.currentUser
            auth?.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
                Log.d(TAG, "Auth state changed: user=${firebaseAuth.currentUser?.displayName}")
            }
            isInitialized = true
            Log.i(TAG, "Firebase initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init error: ${e.message}", e)
        }
    }

    fun getFirestore(): FirebaseFirestore? = firestore
    fun getAuth(): FirebaseAuth? = auth

    /**
     * Google Sign-In using Android Jetpack Credential Manager
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = "346904375603-abcdef123456.apps.googleusercontent.com"
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _isAuthenticating.value = true
        _authError.value = null
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = try {
                credentialManager.getCredential(context = context, request = request)
            } catch (e: GetCredentialCancellationException) {
                _isAuthenticating.value = false
                return@withContext Result.failure(Exception("Connexion annulée par l'utilisateur."))
            } catch (e: GetCredentialException) {
                _isAuthenticating.value = false
                Log.w(TAG, "Credential Manager error: ${e.message}")
                return@withContext Result.failure(e)
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                val authResult = auth?.signInWithCredential(authCredential)?.await()
                val user = authResult?.user

                if (user != null) {
                    _currentUser.value = user
                    _isAuthenticating.value = false
                    Log.i(TAG, "Successfully signed in to Firebase with Google: ${user.email}")
                    Result.success(user)
                } else {
                    _isAuthenticating.value = false
                    val err = "Échec de l'authentification Firebase."
                    _authError.value = err
                    Result.failure(Exception(err))
                }
            } else {
                _isAuthenticating.value = false
                val err = "Type d'identifiant Google inattendu."
                _authError.value = err
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            _isAuthenticating.value = false
            _authError.value = e.message
            Log.e(TAG, "Sign in exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out user from Firebase and clear Credential Manager state
     */
    suspend fun signOut(context: Context) = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            _authError.value = null
            Log.i(TAG, "Signed out successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}", e)
        }
    }
}
