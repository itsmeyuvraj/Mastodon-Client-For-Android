package com.mastodon.widget.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.repository.StatusRepository
import java.util.concurrent.TimeUnit

class FeedUpdateService(
    context: Context,
    params: androidx.work.WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val preferenceManager = PreferenceManager(applicationContext)
            val statusRepository = StatusRepository(database, preferenceManager)

            Log.d("FeedUpdateService", "Starting feed sync work")

            statusRepository.fetchHomeTimeline()
                .onSuccess { statuses ->
                    Log.d("FeedUpdateService", "Synced ${statuses.size} statuses")
                    Result.success()
                }
                .onFailure { exception ->
                    Log.e("FeedUpdateService", "Error syncing feed", exception)
                    Result.retry()
                }

            Result.success()
        } catch (e: Exception) {
            Log.e("FeedUpdateService", "Exception in work", e)
            Result.retry()
        }
    }

    companion object {
        private const val FEED_SYNC_WORK_NAME = "feed_sync_work"

        fun scheduleFeedSync(context: Context, intervalMinutes: Long = 15) {
            val feedSyncWorkRequest = PeriodicWorkRequestBuilder<FeedUpdateService>(
                intervalMinutes,
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                FEED_SYNC_WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                feedSyncWorkRequest
            )

            Log.d("FeedUpdateService", "Scheduled periodic feed sync work")
        }

        fun cancelFeedSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(FEED_SYNC_WORK_NAME)
            Log.d("FeedUpdateService", "Cancelled feed sync work")
        }
    }
}
