package com.mastodon.widget.repository

import com.mastodon.widget.api.ApiClient
import com.mastodon.widget.api.model.Account
import com.mastodon.widget.data.PreferenceManager

class AccountRepository(private val preferenceManager: PreferenceManager) {

    suspend fun verifyCredentials(): Result<Account> = runCatching {
        val token = preferenceManager.getAccessToken()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.getServerUrl()

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        val account = ApiClient.getInstance().verifyCredentials(authorization = "Bearer $token")
        preferenceManager.setUserInfo(account.username, account.displayName, account.avatar)
        account
    }

    suspend fun getAccount(accountId: String): Result<Account> = runCatching {
        val token = preferenceManager.getAccessToken()
            ?: throw IllegalStateException("User not authenticated")
        val serverUrl = preferenceManager.getServerUrl()

        ApiClient.setBaseUrl("$serverUrl/api/v1/")
        ApiClient.getInstance().getAccount(accountId = accountId, authorization = "Bearer $token")
    }

    suspend fun logout() {
        preferenceManager.clearAll()
        ApiClient.reset()
    }
}
