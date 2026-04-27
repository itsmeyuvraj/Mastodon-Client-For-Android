package com.mastodon.widget.widget

import android.content.Context
import android.text.Html
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mastodon.widget.R
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

class FeedRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var statuses: List<Status> = emptyList()
    private val db by lazy { AppDatabase.getDatabase(context) }

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Runs on background thread provided by the system
        statuses = db.statusDao().getRecentStatusesSync(25)
    }

    override fun onDestroy() {}

    override fun getCount(): Int = statuses.size

    override fun getViewAt(position: Int): RemoteViews {
        val status = statuses[position]
        val displayStatus = status.reblog ?: status
        val views = RemoteViews(context.packageName, R.layout.widget_status_item)

        val displayName = displayStatus.account?.displayName?.takeIf { it.isNotBlank() }
            ?: displayStatus.account?.username ?: "Unknown"
        views.setTextViewText(R.id.widget_item_author, displayName)
        views.setTextViewText(R.id.widget_item_username, "@${displayStatus.account?.username ?: ""}")

        val content = Html.fromHtml(displayStatus.content, Html.FROM_HTML_MODE_COMPACT)
            .toString().trim()
        views.setTextViewText(R.id.widget_item_content, content)

        views.setTextViewText(
            R.id.widget_item_stats,
            "♥ ${displayStatus.favouritesCount}  ⇄ ${displayStatus.reblogsCount}  💬 ${displayStatus.repliesCount}"
        )

        views.setTextViewText(R.id.widget_item_time, formatRelativeTime(displayStatus.createdAt))

        if (status.reblog != null) {
            views.setTextViewText(
                R.id.widget_item_reblog_label,
                "🔁 boosted by @${status.account?.username}"
            )
            views.setViewVisibility(R.id.widget_item_reblog_label, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_item_reblog_label, android.view.View.GONE)
        }

        // Empty fill-in intent so the template PendingIntent fires on item click
        views.setOnClickFillInIntent(R.id.widget_item_root, android.content.Intent())

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = statuses[position].id.hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    private fun formatRelativeTime(isoDate: String): String {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
        )
        return try {
            val date = formats.firstNotNullOfOrNull { runCatching { it.parse(isoDate) }.getOrNull() }
                ?: return ""
            val diffSec = (Date().time - date.time) / 1000
            when {
                diffSec < 60 -> "${diffSec}s"
                diffSec < 3600 -> "${diffSec / 60}m"
                diffSec < 86400 -> "${diffSec / 3600}h"
                else -> "${diffSec / 86400}d"
            }
        } catch (_: Exception) {
            ""
        }
    }
}
