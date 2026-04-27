package com.mastodon.widget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mastodon.widget.api.ApiClient
import com.mastodon.widget.api.model.SearchResult
import com.mastodon.widget.data.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceManager(application)

    private val _results = MutableStateFlow<SearchResult?>(null)
    val results: StateFlow<SearchResult?> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _error.value = null
            runCatching {
                val token = prefs.getAccessToken() ?: throw IllegalStateException("Not authenticated")
                val serverUrl = prefs.getServerUrl()
                ApiClient.setBaseUrl("$serverUrl/api/v1/")
                ApiClient.getInstance().search("Bearer $token", query)
            }.onSuccess {
                _results.value = it
            }.onFailure {
                _error.value = it.message
            }
            _isSearching.value = false
        }
    }

    fun clear() {
        _results.value = null
        _error.value = null
    }
}
