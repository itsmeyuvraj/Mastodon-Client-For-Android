package com.mastodon.widget.repository

import com.mastodon.widget.api.ApiClient
import com.mastodon.widget.api.model.Account
import com.mastodon.widget.api.model.CreateStatusRequest
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class StatusRepository(
    private val database: AppDatabase,
    private val preferenceManager: PreferenceManager
) {
    private val statusDao = database.statusDao()

    val recentStatuses: Flow<List<Status>> = statusDao.getRecentStatuses()
    val allStatuses: Flow<List<Status>> = statusDao.getAllStatuses()
    val statusCount: Flow<Int> = statusDao.getStatusCount()

    suspend fun createStatus(
        content: String,
        inReplyToId: String? = null,
        visibility: String = "public",
        spoilerText: String? = null
    ): Result<Status> = runCatching {
        val token = preferenceManager.accessToken.firstOrNull()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val request = CreateStatusRequest(
            status = content,
            in_reply_to_id = inReplyToId,
            visibility = visibility,
            spoiler_text = spoilerText
        )

        val status = ApiClient.getInstance().createStatus(
            authorization = "Bearer $token",
            request = request
        )

        statusDao.insertStatus(status)
        status
    }

    suspend fun fetchHomeTimeline(maxId: String? = null): Result<List<Status>> = runCatching {
        val token = preferenceManager.accessToken.firstOrNull()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val statuses = ApiClient.getInstance().getHomeTimeline(
            authorization = "Bearer $token",
            maxId = maxId
        )

        statusDao.insertStatuses(statuses)
        statuses
    }

    suspend fun fetchPublicTimeline(maxId: String? = null): Result<List<Status>> = runCatching {
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")
        val token = preferenceManager.accessToken.firstOrNull()

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val statuses = ApiClient.getInstance().getPublicTimeline(
            authorization = if (token != null) "Bearer $token" else "",
            maxId = maxId
        )

        statusDao.insertStatuses(statuses)
        statuses
    }

    suspend fun fetchHashtagTimeline(hashtag: String, maxId: String? = null): Result<List<Status>> = runCatching {
        val token = preferenceManager.accessToken.firstOrNull()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val statuses = ApiClient.getInstance().getHashtagTimeline(
            hashtag = hashtag,
            authorization = "Bearer $token",
            maxId = maxId
        )

        statusDao.insertStatuses(statuses)
        statuses
    }

    suspend fun getStatusContext(statusId: String): Result<Map<String, List<Status>>> = runCatching {
        val token = preferenceManager.accessToken.firstOrNull()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        ApiClient.getInstance().getStatusContext(
            statusId = statusId,
            authorization = "Bearer $token"
        )
    }

    suspend fun favourite(statusId: String): Result<Status> = runCatching {
        val token = preferenceManager.accessToken.firstOrNull()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val status = ApiClient.getInstance().favourite(
            statusId = statusId,
            authorization = "Bearer $token"
        )

        statusDao.insertStatus(status)
        status
    }

    suspend fun reblog(statusId: String): Result<Status> = runCatching {
        val token = preferenceManager.accessToken.firstOrNull()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.serverUrl.firstOrNull()
            ?: throw IllegalStateException("Server URL not set")

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val status = ApiClient.getInstance().reblog(
            statusId = statusId,
            authorization = "Bearer $token"
        )

        statusDao.insertStatus(status)
        status
    }

    suspend fun deleteStatus(statusId: String) {
        statusDao.deleteStatus(statusId)
    }
}
