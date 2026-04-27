package com.mastodon.widget.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.mastodon.widget.api.model.Account
import com.mastodon.widget.api.model.MediaAttachment

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAccount(account: Account?): String? {
        return account?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toAccount(json: String?): Account? {
        return json?.let { gson.fromJson(it, Account::class.java) }
    }

    @TypeConverter
    fun fromMediaAttachmentList(attachments: List<MediaAttachment>): String {
        return gson.toJson(attachments)
    }

    @TypeConverter
    fun toMediaAttachmentList(json: String): List<MediaAttachment> {
        return try {
            gson.fromJson(json, Array<MediaAttachment>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
