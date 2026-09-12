package com.mastodon.widget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mastodon.widget.api.ApiClient
import com.mastodon.widget.api.model.Account
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.data.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceManager(application)

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    val statuses: StateFlow<List<Status>> = _statuses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser: StateFlow<Boolean> = _isCurrentUser.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private var currentLoadedAccountId: String? = null

    fun loadProfile(targetAccountId: String? = null) {
        currentLoadedAccountId = targetAccountId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching {
                val token = prefs.getAccessToken() ?: throw IllegalStateException("Not authenticated")
                val serverUrl = prefs.getServerUrl()
                ApiClient.setBaseUrl("$serverUrl/api/v1/")
                val api = ApiClient.getInstance()

                val currentUserId = prefs.getUserId()
                val currentUsername = prefs.username.firstOrNull()

                val acct = if (targetAccountId == null) {
                    val myAcct = api.verifyCredentials("Bearer $token")
                    prefs.setUserId(myAcct.id)
                    prefs.setUserInfo(myAcct.username, myAcct.displayName, myAcct.avatar, myAcct.id)
                    _isCurrentUser.value = true
                    myAcct
                } else {
                    val targetAcct = api.getAccount(targetAccountId, "Bearer $token")
                    val isMe = (currentUserId != null && currentUserId == targetAccountId) ||
                        (currentUsername != null && (currentUsername.equals(targetAcct.username, ignoreCase = true) ||
                            currentUsername.equals(targetAcct.acct, ignoreCase = true)))
                    _isCurrentUser.value = isMe
                    targetAcct
                }

                _account.value = acct
                val posts = api.getAccountStatuses(acct.id, "Bearer $token")
                _statuses.value = posts
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun reloadProfile() {
        loadProfile(currentLoadedAccountId)
    }

    fun updateProfile(displayName: String, note: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val token = prefs.getAccessToken() ?: throw IllegalStateException("Not authenticated")
                val serverUrl = prefs.getServerUrl()
                ApiClient.setBaseUrl("$serverUrl/api/v1/")
                val updated = ApiClient.getInstance().updateCredentials("Bearer $token", displayName, note)
                _account.value = updated
                prefs.setUserInfo(updated.username, updated.displayName, updated.avatar, updated.id)
                _updateSuccess.value = true
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun favourite(statusId: String) {
        viewModelScope.launch {
            runCatching {
                val token = prefs.getAccessToken() ?: return@launch
                val serverUrl = prefs.getServerUrl()
                ApiClient.setBaseUrl("$serverUrl/api/v1/")
                val api = ApiClient.getInstance()
                val current = _statuses.value.find { it.id == statusId }
                val updated = if (current?.favourited == true) {
                    api.unfavourite(statusId, "Bearer $token")
                } else {
                    api.favourite(statusId, "Bearer $token")
                }
                _statuses.value = _statuses.value.map { if (it.id == statusId) updated else it }
            }
        }
    }

    fun reblog(statusId: String) {
        viewModelScope.launch {
            runCatching {
                val token = prefs.getAccessToken() ?: return@launch
                val serverUrl = prefs.getServerUrl()
                ApiClient.setBaseUrl("$serverUrl/api/v1/")
                val api = ApiClient.getInstance()
                val current = _statuses.value.find { it.id == statusId }
                val updated = if (current?.reblogged == true) {
                    api.unreblog(statusId, "Bearer $token")
                } else {
                    api.reblog(statusId, "Bearer $token")
                }
                _statuses.value = _statuses.value.map { if (it.id == statusId) updated else it }
            }
        }
    }

    fun clearUpdateSuccess() { _updateSuccess.value = false }

    suspend fun logout() {
        prefs.clearAll()
        ApiClient.reset()
    }
}
