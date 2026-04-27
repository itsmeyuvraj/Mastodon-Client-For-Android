package com.mastodon.widget.repository

import com.mastodon.widget.api.ApiClient
import com.mastodon.widget.api.model.Notification
import com.mastodon.widget.data.PreferenceManager

class NotificationRepository(private val preferenceManager: PreferenceManager) {

    suspend fun getNotifications(): Result<List<Notification>> = runCatching {
        val token = preferenceManager.getAccessToken()
            ?: throw IllegalStateException("Not authenticated")
        val serverUrl = preferenceManager.getServerUrl()
        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        ApiClient.getInstance().getNotifications("Bearer $token")
    }

    suspend fun dismissNotification(id: String): Result<Unit> = runCatching {
        val token = preferenceManager.getAccessToken()
            ?: throw IllegalStateException("Not authenticated")
        val serverUrl = preferenceManager.getServerUrl()
        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        ApiClient.getInstance().dismissNotification(id, "Bearer $token")
    }
}
