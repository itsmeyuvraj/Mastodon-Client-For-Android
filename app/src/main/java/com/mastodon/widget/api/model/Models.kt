package com.mastodon.widget.api.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Mastodon Account
data class Account(
    val id: String,
    val username: String,
    val acct: String,
    @SerializedName("display_name")
    val displayName: String,
    val avatar: String,
    @SerializedName("avatar_static")
    val avatarStatic: String,
    val note: String = "",
    val url: String = "",
    val header: String = "",
    @SerializedName("statuses_count")
    val postsCount: Int = 0,
    @SerializedName("followers_count")
    val followersCount: Int = 0,
    @SerializedName("following_count")
    val followingCount: Int = 0,
    val locked: Boolean = false
)

// Mastodon Status/Post
@Entity(tableName = "statuses")
data class Status(
    @PrimaryKey val id: String,
    val content: String = "",
    val account: Account? = null,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("in_reply_to_id")
    val inReplyToId: String? = null,
    @SerializedName("in_reply_to_account_id")
    val inReplyToAccountId: String? = null,
    val sensitive: Boolean = false,
    @SerializedName("spoiler_text")
    val spoilerText: String? = null,
    val visibility: String = "public",
    val language: String? = null,
    val uri: String = "",
    val url: String = "",
    @SerializedName("replies_count")
    val repliesCount: Int = 0,
    @SerializedName("reblogs_count")
    val reblogsCount: Int = 0,
    @SerializedName("favourites_count")
    val favouritesCount: Int = 0,
    val edited: Boolean? = null,
    @SerializedName("edited_at")
    val editedAt: String? = null,
    val favourited: Boolean = false,
    val reblogged: Boolean = false,
    val muted: Boolean = false,
    val bookmarked: Boolean = false,
    val pinned: Boolean = false,
    val media_attachments: List<MediaAttachment> = emptyList()
) {
    // Self-referential field — excluded from Room, populated from API response
    @Ignore
    val reblog: Status? = null
}

// Media Attachment
data class MediaAttachment(
    val id: String,
    val type: String,
    val url: String,
    @SerializedName("preview_url")
    val previewUrl: String = "",
    @SerializedName("remote_url")
    val remoteUrl: String? = null,
    val text_url: String? = null,
    val description: String? = null,
    val blurhash: String? = null
)

// Create Status Request
data class CreateStatusRequest(
    val status: String,
    val in_reply_to_id: String? = null,
    val sensitive: Boolean = false,
    @SerializedName("spoiler_text")
    val spoiler_text: String? = null,
    val visibility: String = "public",
    val language: String? = null,
    val media_ids: List<String> = emptyList(),
    val poll: PollRequest? = null
)

// Poll Request
data class PollRequest(
    val options: List<String>,
    val expires_in: Int,
    val multiple: Boolean = false,
    val hide_totals: Boolean = false
)

// Authentication Token
data class Token(
    val access_token: String,
    val token_type: String,
    val scope: String,
    val created_at: Long
)

// Application registration request (Mastodon POST /api/v1/apps)
data class CreateAppRequest(
    @SerializedName("client_name") val clientName: String,
    @SerializedName("redirect_uris") val redirectUris: String,
    val scopes: String = "read write follow push",
    val website: String? = null
)

// Application registration response
data class Application(
    val id: String = "",
    val name: String = "",
    val website: String? = null,
    @SerializedName("redirect_uri") val redirect_uri: String = "",
    @SerializedName("client_id") val client_id: String = "",
    @SerializedName("client_secret") val client_secret: String = "",
    @SerializedName("vapid_key") val vapid_key: String? = null
)

// Mastodon Notification
data class Notification(
    val id: String,
    val type: String,
    @SerializedName("created_at")
    val createdAt: String = "",
    val account: Account,
    val status: Status? = null
)

// Search Result
data class SearchResult(
    val accounts: List<Account> = emptyList(),
    val statuses: List<Status> = emptyList(),
    val hashtags: List<Tag> = emptyList()
)

// Hashtag
data class Tag(
    val name: String,
    val url: String,
    @SerializedName("history")
    val history: List<TagHistory> = emptyList()
)

// Hashtag history entry
data class TagHistory(
    val day: String = "",
    val uses: String = "",
    val accounts: String = ""
)

// Edit Profile Request
data class EditProfileRequest(
    @SerializedName("display_name") val displayName: String? = null,
    val note: String? = null
)
