package com.mastodon.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mastodon.widget.R
import com.mastodon.widget.service.FeedSyncService
import com.mastodon.widget.ui.MainActivity
import com.mastodon.widget.ui.PostActivity

class MastodonWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            context.startService(Intent(context, FeedSyncService::class.java))
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MastodonWidgetProvider::class.java))
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_feed)
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.mastodon.widget.ACTION_REFRESH"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Wire up the live feed ListView
            val serviceIntent = Intent(context, FeedRemoteViewsService::class.java)
            views.setRemoteAdapter(R.id.widget_feed, serviceIntent)
            views.setEmptyView(R.id.widget_feed, R.id.widget_empty_text)

            // Tapping a feed item opens MainActivity
            val openAppIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_feed, openAppIntent)

            // Refresh button
            val refreshIntent = PendingIntent.getBroadcast(
                context, 1,
                Intent(context, MastodonWidgetProvider::class.java).apply { action = ACTION_REFRESH },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshIntent)

            // New Post button
            val postIntent = PendingIntent.getActivity(
                context, 2,
                Intent(context, PostActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_post_btn, postIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_feed)
        }

        fun triggerUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MastodonWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, MastodonWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
