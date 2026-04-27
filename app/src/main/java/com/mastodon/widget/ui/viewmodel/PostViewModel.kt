package com.mastodon.widget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.repository.StatusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferenceManager = PreferenceManager(application)
    private val statusRepository = StatusRepository(database, preferenceManager)

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    fun postStatus(
        content: String,
        visibility: String = "public",
        spoilerText: String? = null,
        inReplyToId: String? = null
    ) {
        if (content.isBlank()) { _error.value = "Status content cannot be empty"; return }
        if (content.length > 500) { _error.value = "Status must be 500 characters or less"; return }
        viewModelScope.launch {
            _isPosting.value = true
            _error.value = null
            _success.value = false
            statusRepository.createStatus(content, inReplyToId, visibility, spoilerText)
                .onSuccess { _success.value = true; _isPosting.value = false }
                .onFailure { _error.value = it.message ?: "Failed to post"; _isPosting.value = false }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccess() { _success.value = false }
}
