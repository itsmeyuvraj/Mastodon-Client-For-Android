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
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceManager(application)

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    val statuses: StateFlow<List<Status>> = _statuses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching {
                val token = prefs.getAccessToken() ?: throw IllegalStateException("Not authenticated")
                val serverUrl = prefs.getServerUrl()
                ApiClient.setBaseUrl("$serverUrl/api/v1/")
                val api = ApiClient.getInstance()
                val acct = api.verifyCredentials("Bearer $token")
                _account.value = acct
                val posts = api.getAccountStatuses(acct.id, "Bearer $token")
                _statuses.value = posts
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
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
                prefs.setUserInfo(updated.username, updated.displayName, updated.avatar)
                _updateSuccess.value = true
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearUpdateSuccess() { _updateSuccess.value = false }

    suspend fun logout() {
        prefs.clearAll()
        ApiClient.reset()
    }
}
