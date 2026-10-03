package cs.uwaterloo.timora.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import cs.uwaterloo.domain.entity.User
import cs.uwaterloo.domain.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthRepository(
    context: Context,
    private val userModel: UserModel
) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.dataStore

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    suspend fun isUserLoggedIn(): Boolean {
        val userId = dataStore.data.first()[USER_ID_KEY]
        return !userId.isNullOrEmpty()
    }

    suspend fun getCurrentUserId(): String? {
        return dataStore.data.first()[USER_ID_KEY]
    }

    suspend fun getCurrentUserEmail(): String? {
        return dataStore.data.first()[USER_EMAIL_KEY]
    }

    fun getCurrentUserIdFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun signInWithGoogle(credential: GoogleIdTokenCredential): Result<User> {
        return try {
            val email = credential.id
            val displayName = credential.displayName ?: ""
            val photoUrl = credential.profilePictureUri?.toString() ?: ""
            val userId = credential.id

            if (email.isEmpty()) {
                return Result.failure(Exception("Email not found in Google credential"))
            }

            val existingUser = userModel.getUserByEmail(email)

            val user = if (existingUser != null) {
                existingUser
            } else {
                val newUser = User(
                    id = userId,
                    email = email,
                    name = displayName,
                    avatarUrl = photoUrl,
                    bio = "",
                    hostedEventIds = emptySet(),
                    attendedEventIds = emptySet(),
                    invitationList = emptySet()
                )
                userModel.createUser(newUser)
            }

            saveUserSession(user.id, user.email)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveUserSession(userId: String, email: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun signOut() {
        dataStore.edit { preferences ->
            preferences.clear()
        }

        try {
            val credentialManager = CredentialManager.create(appContext)
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
        } catch (e: ClearCredentialException) {
        }
    }

    suspend fun getCurrentUser(): User? {
        val userId = getCurrentUserId() ?: return null
        return try {
            userModel.getUserById(userId)
        } catch (e: Exception) {
            null
        }
    }
}
