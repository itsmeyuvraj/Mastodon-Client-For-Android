package com.mastodon.widget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mastodon_widget_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val CLIENT_ID = stringPreferencesKey("client_id")
        private val CLIENT_SECRET = stringPreferencesKey("client_secret")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val AVATAR_URL = stringPreferencesKey("avatar_url")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { it[SERVER_URL] ?: "https://mastodon.social" }
    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val clientId: Flow<String?> = context.dataStore.data.map { it[CLIENT_ID] }
    val clientSecret: Flow<String?> = context.dataStore.data.map { it[CLIENT_SECRET] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME] }
    val displayName: Flow<String?> = context.dataStore.data.map { it[DISPLAY_NAME] }
    val avatarUrl: Flow<String?> = context.dataStore.data.map { it[AVATAR_URL] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val lastSyncTime: Flow<Long> = context.dataStore.data.map { it[LAST_SYNC_TIME] ?: 0L }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url }
    }

    suspend fun setAccessToken(token: String) {
        context.dataStore.edit { it[ACCESS_TOKEN] = token }
    }

    suspend fun setClientCredentials(clientId: String, clientSecret: String) {
        context.dataStore.edit {
            it[CLIENT_ID] = clientId
            it[CLIENT_SECRET] = clientSecret
        }
    }

    suspend fun setUserId(id: String) {
        context.dataStore.edit { it[USER_ID] = id }
    }

    suspend fun setUserInfo(username: String, displayName: String, avatarUrl: String) {
        context.dataStore.edit {
            it[USERNAME] = username
            it[DISPLAY_NAME] = displayName
            it[AVATAR_URL] = avatarUrl
            it[IS_LOGGED_IN] = true
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { it[IS_LOGGED_IN] = loggedIn }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { it[LAST_SYNC_TIME] = time }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // Convenience suspending accessors for non-reactive use
    suspend fun getServerUrl(): String = serverUrl.firstOrNull() ?: "https://mastodon.social"
    suspend fun getAccessToken(): String? = accessToken.firstOrNull()
    suspend fun getClientId(): String? = clientId.firstOrNull()
    suspend fun getClientSecret(): String? = clientSecret.firstOrNull()
    suspend fun isUserLoggedIn(): Boolean = isLoggedIn.firstOrNull() ?: false
}
