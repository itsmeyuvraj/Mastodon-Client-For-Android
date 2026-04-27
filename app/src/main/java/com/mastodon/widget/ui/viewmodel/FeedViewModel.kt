package com.mastodon.widget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.repository.StatusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferenceManager = PreferenceManager(application)
    private val statusRepository = StatusRepository(database, preferenceManager)

    // Primary source of truth: Room DB, updated by both manual refresh and live streaming
    val statuses: StateFlow<List<Status>> = statusRepository.recentStatuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            statusRepository.fetchHomeTimeline()
                .onFailure { _error.value = it.message ?: "Failed to load feed" }
            _isLoading.value = false
        }
    }

    fun loadMoreFeed() {
        val lastId = statuses.value.lastOrNull()?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            statusRepository.fetchHomeTimeline(maxId = lastId)
                .onFailure { _error.value = it.message ?: "Failed to load more" }
            _isLoading.value = false
        }
    }

    fun favourite(statusId: String) {
        viewModelScope.launch {
            statusRepository.favourite(statusId)
                .onFailure { _error.value = it.message ?: "Failed to favourite" }
        }
    }

    fun reblog(statusId: String) {
        viewModelScope.launch {
            statusRepository.reblog(statusId)
                .onFailure { _error.value = it.message ?: "Failed to boost" }
        }
    }

    fun clearError() { _error.value = null }
}
