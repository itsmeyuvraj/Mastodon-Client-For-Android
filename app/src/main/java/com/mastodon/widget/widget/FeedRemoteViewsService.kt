package com.mastodon.widget.widget

import android.content.Intent
import android.widget.RemoteViewsService

class FeedRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return FeedRemoteViewsFactory(applicationContext)
    }
}
