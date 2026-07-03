package com.turkcell.rencarapp.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.turkcell.rencarapp.data.auth.AuthTokens
import com.turkcell.rencarapp.data.auth.User
import com.turkcell.rencarapp.data.auth.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SessionStore {
    suspend fun saveSession(tokens: AuthTokens)
    suspend fun clearSession()
    suspend fun getSession(): AuthTokens?
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
}

@Singleton
class DataStoreSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) : SessionStore {

    private val dataStore: DataStore<Preferences> = context.sessionDataStore

    override suspend fun saveSession(tokens: AuthTokens) {
        dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = tokens.accessToken
            prefs[Keys.REFRESH_TOKEN] = tokens.refreshToken
            prefs[Keys.USER_ID] = tokens.user.id
            prefs[Keys.USER_EMAIL] = tokens.user.email
            prefs[Keys.USER_FULL_NAME] = tokens.user.fullName
            prefs[Keys.USER_PHONE] = tokens.user.phone.orEmpty()
            prefs[Keys.USER_ROLE] = tokens.user.role.name
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_EMAIL)
            prefs.remove(Keys.USER_FULL_NAME)
            prefs.remove(Keys.USER_PHONE)
            prefs.remove(Keys.USER_ROLE)
        }
    }

    override suspend fun getSession(): AuthTokens? {
        val prefs = dataStore.data.first()
        val accessToken = prefs[Keys.ACCESS_TOKEN] ?: return null
        val refreshToken = prefs[Keys.REFRESH_TOKEN] ?: return null
        val userId = prefs[Keys.USER_ID] ?: return null
        val email = prefs[Keys.USER_EMAIL] ?: return null
        val fullName = prefs[Keys.USER_FULL_NAME] ?: return null
        val roleName = prefs[Keys.USER_ROLE] ?: return null

        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = User(
                id = userId,
                email = email,
                fullName = fullName,
                phone = prefs[Keys.USER_PHONE].takeUnless { it.isNullOrEmpty() },
                role = UserRole.valueOf(roleName),
            ),
        )
    }

    override suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }.first()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_ROLE = stringPreferencesKey("user_role")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    private companion object {
        val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "rencar_session",
        )
    }
}
