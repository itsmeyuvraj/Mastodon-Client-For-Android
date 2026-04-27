package com.mastodon.widget.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.repository.StatusRepository
import kotlinx.coroutines.*

class FeedSyncService : Service() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var statusRepository: StatusRepository

    override fun onCreate() {
        super.onCreate()
        Log.d("FeedSyncService", "Service created")

        val database = AppDatabase.getDatabase(this)
        val preferenceManager = PreferenceManager(this)
        statusRepository = StatusRepository(database, preferenceManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FeedSyncService", "onStartCommand called")

        scope.launch {
            try {
                statusRepository.fetchHomeTimeline()
                    .onSuccess {
                        com.mastodon.widget.widget.MastodonWidgetProvider.triggerUpdate(this@FeedSyncService)
                    }
                    .onFailure { e ->
                        Log.e("FeedSyncService", "Error fetching feed", e)
                    }
            } catch (e: Exception) {
                Log.e("FeedSyncService", "Exception in sync", e)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.d("FeedSyncService", "Service destroyed")
    }
}
