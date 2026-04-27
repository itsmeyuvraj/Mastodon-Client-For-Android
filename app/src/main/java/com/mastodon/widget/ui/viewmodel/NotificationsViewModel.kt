package com.mastodon.widget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mastodon.widget.api.model.Notification
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotificationRepository(PreferenceManager(application))

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getNotifications()
                .onSuccess { _notifications.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun dismiss(id: String) {
        viewModelScope.launch {
            repository.dismissNotification(id)
                .onSuccess { _notifications.value = _notifications.value.filter { it.id != id } }
        }
    }
}
