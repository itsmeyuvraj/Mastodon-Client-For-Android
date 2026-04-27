package com.mastodon.widget.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.mastodon.widget.MastodonApplication
import com.mastodon.widget.R
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.widget.MastodonWidgetProvider
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.sse.*
import java.util.concurrent.TimeUnit

class StreamingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var eventSource: EventSource? = null
    private val gson = Gson()

    private val streamingClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.SECONDS) // No timeout for SSE
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch { connect() }
        return START_STICKY
    }

    private suspend fun connect() {
        val prefs = PreferenceManager(this)
        val serverUrl = prefs.getServerUrl()
        val token = prefs.getAccessToken() ?: return

        val streamUrl = "$serverUrl/api/v1/streaming/user"
        val request = Request.Builder()
            .url(streamUrl)
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (type == "update" && data.isNotBlank() && data != ":thump") {
                    serviceScope.launch { handleNewStatus(data) }
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                // Reconnect after a short delay
                serviceScope.launch {
                    delay(5_000)
                    connect()
                }
            }
        }

        eventSource?.cancel()
        eventSource = EventSources.createFactory(streamingClient).newEventSource(request, listener)
    }

    private suspend fun handleNewStatus(json: String) {
        try {
            val status = gson.fromJson(json, Status::class.java)
            AppDatabase.getDatabase(this).statusDao().insertStatus(status)
            MastodonWidgetProvider.triggerUpdate(this)
        } catch (_: Exception) {
            // Skip malformed events
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "mastodon_streaming"
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Live Feed", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mastodon Widget")
            .setContentText("Streaming live feed")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        eventSource?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 7001

        fun start(context: Context) {
            context.startForegroundService(Intent(context, StreamingService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StreamingService::class.java))
        }
    }
}
