package com.mastodon.widget

import android.app.Application
import com.mastodon.widget.data.AppDatabase
import com.mastodon.widget.data.PreferenceManager

class MastodonApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val preferenceManager by lazy { PreferenceManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MastodonApplication
            private set
    }
}
