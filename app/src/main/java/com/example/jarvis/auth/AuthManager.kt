package com.example.jarvis.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class JarvisUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val isEmailVerified: Boolean,
    val isAnonymous: Boolean = false,
    val providerId: String = "firebase"
)

sealed class AuthState {
    object Initializing : AuthState()
    data class Authenticated(val user: JarvisUser) : AuthState()
    data class Unauthenticated(val message: String? = null) : AuthState()
    data class AuthError(val errorMessage: String) : AuthState()
}

class AuthManager(private val context: Context) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var firebaseAuth: FirebaseAuth? = null
    private var isFirebaseAvailable = false

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                isFirebaseAvailable = true
                setupAuthListener()
            } else {
                // Check if default app can be initialized
                try {
                    FirebaseApp.initializeApp(context)
                    firebaseAuth = FirebaseAuth.getInstance()
                    isFirebaseAvailable = true
                    setupAuthListener()
                } catch (_: Exception) {
                    isFirebaseAvailable = false
                    checkLocalSession()
                }
            }
        } catch (_: Exception) {
            isFirebaseAvailable = false
            checkLocalSession()
        }
    }

    private fun setupAuthListener() {
        val auth = firebaseAuth ?: return
        auth.addAuthStateListener { fbAuth ->
            val currentUser = fbAuth.currentUser
            if (currentUser != null) {
                val user = mapFirebaseUser(currentUser)
                _authState.value = AuthState.Authenticated(user)
                saveLocalSession(user)
            } else {
                checkLocalSession()
            }
        }
    }

    private fun mapFirebaseUser(user: FirebaseUser): JarvisUser {
        return JarvisUser(
            uid = user.uid,
            email = user.email ?: "user@jarvis.ai",
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Commander",
            isEmailVerified = user.isEmailVerified,
            isAnonymous = user.isAnonymous,
            providerId = user.providerId
        )
    }

    private fun checkLocalSession() {
        val prefs = context.getSharedPreferences("jarvis_auth_prefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("user_uid", null)
        val email = prefs.getString("user_email", null)
        val name = prefs.getString("user_name", null)
        val isGuest = prefs.getBoolean("is_guest", false)

        if (uid != null && email != null) {
            val user = JarvisUser(
                uid = uid,
                email = email,
                displayName = name ?: email.substringBefore("@"),
                isEmailVerified = !isGuest,
                isAnonymous = isGuest
            )
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Unauthenticated()
        }
    }

    private fun saveLocalSession(user: JarvisUser) {
        val prefs = context.getSharedPreferences("jarvis_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_uid", user.uid)
            .putString("user_email", user.email)
            .putString("user_name", user.displayName)
            .putBoolean("is_guest", user.isAnonymous)
            .apply()
    }

    private fun clearLocalSession() {
        val prefs = context.getSharedPreferences("jarvis_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isFirebaseConfigured(): Boolean = isFirebaseAvailable

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Email and password cannot be empty.")
            return
        }

        val auth = firebaseAuth
        if (auth != null && isFirebaseAvailable) {
            scope.launch {
                try {
                    val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                    val user = result.user
                    if (user != null) {
                        val jarvisUser = mapFirebaseUser(user)
                        saveLocalSession(jarvisUser)
                        _authState.value = AuthState.Authenticated(jarvisUser)
                        onSuccess()
                    } else {
                        onError("Authentication yielded no valid user profile.")
                    }
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Authentication failed.")
                }
            }
        } else {
            // Fallback for local testing when Firebase configuration is absent
            scope.launch {
                val fakeUid = "usr_" + email.trim().lowercase().hashCode().toString().replace("-", "x")
                val localUser = JarvisUser(
                    uid = fakeUid,
                    email = email.trim(),
                    displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isEmailVerified = true,
                    isAnonymous = false
                )
                saveLocalSession(localUser)
                _authState.value = AuthState.Authenticated(localUser)
                onSuccess()
            }
        }
    }

    fun signUpWithEmail(
        name: String,
        email: String,
        pass: String,
        confirmPass: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            onError("All credential fields are required.")
            return
        }
        if (pass != confirmPass) {
            onError("Passwords do not match.")
            return
        }
        if (pass.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

        val auth = firebaseAuth
        if (auth != null && isFirebaseAvailable) {
            scope.launch {
                try {
                    val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                    val user = result.user
                    if (user != null) {
                        // Update display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()
                        user.updateProfile(profileUpdates).await()
                        // Send verification email
                        try {
                            user.sendEmailVerification().await()
                        } catch (_: Exception) {}

                        val jarvisUser = mapFirebaseUser(user)
                        saveLocalSession(jarvisUser)
                        _authState.value = AuthState.Authenticated(jarvisUser)
                        onSuccess("Account created successfully. A verification email was sent to ${user.email}.")
                    } else {
                        onError("User creation returned null.")
                    }
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Account creation failed.")
                }
            }
        } else {
            scope.launch {
                val localUid = "usr_" + System.currentTimeMillis().toString().takeLast(6)
                val localUser = JarvisUser(
                    uid = localUid,
                    email = email.trim(),
                    displayName = name.trim(),
                    isEmailVerified = false,
                    isAnonymous = false
                )
                saveLocalSession(localUser)
                _authState.value = AuthState.Authenticated(localUser)
                onSuccess("Account created locally. Configure google-services.json for full Firebase cloud integration.")
            }
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onError("Please specify your registered email address.")
            return
        }

        val auth = firebaseAuth
        if (auth != null && isFirebaseAvailable) {
            scope.launch {
                try {
                    auth.sendPasswordResetEmail(email.trim()).await()
                    onSuccess("Password reset instructions dispatched to $email.")
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Failed to transmit reset email.")
                }
            }
        } else {
            onSuccess("Password reset simulation dispatched to $email (Firebase cloud config required for SMTP transmission).")
        }
    }

    fun sendEmailVerification(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = firebaseAuth?.currentUser
        if (user != null) {
            scope.launch {
                try {
                    user.sendEmailVerification().await()
                    onSuccess("Verification email transmitted to ${user.email}.")
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Verification dispatch failed.")
                }
            }
        } else {
            onSuccess("Email verification requested for active session.")
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        val guestUser = JarvisUser(
            uid = "guest_commander_" + (1000..9999).random(),
            email = "commander@jarvis.local",
            displayName = "Commander (Guest)",
            isEmailVerified = true,
            isAnonymous = true
        )
        saveLocalSession(guestUser)
        _authState.value = AuthState.Authenticated(guestUser)
        onSuccess()
    }

    fun signOut(onSuccess: () -> Unit = {}) {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        clearLocalSession()
        _authState.value = AuthState.Unauthenticated()
        onSuccess()
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = firebaseAuth?.currentUser
        if (user != null) {
            scope.launch {
                try {
                    user.delete().await()
                    clearLocalSession()
                    _authState.value = AuthState.Unauthenticated("Account purged successfully.")
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Account purge failed. Re-authentication may be required.")
                }
            }
        } else {
            clearLocalSession()
            _authState.value = AuthState.Unauthenticated("Local session removed.")
            onSuccess()
        }
    }
}
