package com.example.blood_donor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val LANG_KEY = stringPreferencesKey("preferred_language")
        private val REMEMBER_PHONE_KEY = stringPreferencesKey("remembered_phone_number")
        private val SAVED_PHONE_KEY = stringPreferencesKey("saved_phone")
        private val SAVED_PASSWORD_KEY = stringPreferencesKey("saved_password_encrypted")
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    val preferredLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANG_KEY] ?: "en"
    }

    val hasLanguageSelectedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences.contains(LANG_KEY)
    }

    val rememberedPhoneFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REMEMBER_PHONE_KEY]
    }

    val savedPhoneFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SAVED_PHONE_KEY]
    }

    val savedPasswordEncryptedFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SAVED_PASSWORD_KEY]
    }

    val serverUrlFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SERVER_URL_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
    }

    suspend fun savePreferredLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANG_KEY] = lang
        }
    }

    suspend fun saveRememberedPhone(phone: String) {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_PHONE_KEY] = phone
        }
    }

    suspend fun clearRememberedPhone() {
        context.dataStore.edit { preferences ->
            preferences.remove(REMEMBER_PHONE_KEY)
        }
    }

    suspend fun saveCredentials(phone: String, passwordRaw: String) {
        val encrypted = com.example.blood_donor.utils.CryptoManager.encrypt(passwordRaw)
        context.dataStore.edit { preferences ->
            preferences[SAVED_PHONE_KEY] = phone
            preferences[SAVED_PASSWORD_KEY] = encrypted
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences.remove(SAVED_PHONE_KEY)
            preferences.remove(SAVED_PASSWORD_KEY)
        }
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }
}
