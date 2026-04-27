package com.mastodon.widget.api

import com.mastodon.widget.api.model.Account
import com.mastodon.widget.api.model.Application
import com.mastodon.widget.api.model.CreateAppRequest
import com.mastodon.widget.api.model.CreateStatusRequest
import com.mastodon.widget.api.model.Notification
import com.mastodon.widget.api.model.SearchResult
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.api.model.Tag
import com.mastodon.widget.api.model.Token
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface MastodonApiService {

    // Authentication Endpoints
    @POST("apps")
    suspend fun createApplication(
        @Body request: CreateAppRequest
    ): Application

    // OAuth token endpoint lives at /oauth/token, NOT under /api/v1/
    @FormUrlEncoded
    @POST
    suspend fun getAccessToken(
        @Url url: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("scope") scope: String
    ): Token

    // Account Endpoints
    @GET("accounts/verify_credentials")
    suspend fun verifyCredentials(
        @Header("Authorization") authorization: String
    ): Account

    @GET("accounts/{id}")
    suspend fun getAccount(
        @Path("id") accountId: String,
        @Header("Authorization") authorization: String
    ): Account

    // Status Endpoints
    @GET("timelines/home")
    suspend fun getHomeTimeline(
        @Header("Authorization") authorization: String,
        @Query("max_id") maxId: String? = null,
        @Query("min_id") minId: String? = null,
        @Query("limit") limit: Int = 40
    ): List<Status>

    @GET("timelines/public")
    suspend fun getPublicTimeline(
        @Header("Authorization") authorization: String,
        @Query("local") local: Boolean = false,
        @Query("max_id") maxId: String? = null,
        @Query("min_id") minId: String? = null,
        @Query("limit") limit: Int = 40
    ): List<Status>

    @GET("timelines/tag/{hashtag}")
    suspend fun getHashtagTimeline(
        @Path("hashtag") hashtag: String,
        @Header("Authorization") authorization: String,
        @Query("max_id") maxId: String? = null,
        @Query("min_id") minId: String? = null,
        @Query("limit") limit: Int = 40
    ): List<Status>

    @GET("statuses/{id}")
    suspend fun getStatus(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    @GET("statuses/{id}/context")
    suspend fun getStatusContext(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Map<String, List<Status>>

    @POST("statuses")
    suspend fun createStatus(
        @Header("Authorization") authorization: String,
        @Body request: CreateStatusRequest
    ): Status

    @POST("statuses/{id}/favourite")
    suspend fun favourite(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    @POST("statuses/{id}/unfavourite")
    suspend fun unfavourite(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    @POST("statuses/{id}/reblog")
    suspend fun reblog(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    @POST("statuses/{id}/unreblog")
    suspend fun unreblog(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    @POST("statuses/{id}/bookmark")
    suspend fun bookmark(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    @POST("statuses/{id}/unbookmark")
    suspend fun unbookmark(
        @Path("id") statusId: String,
        @Header("Authorization") authorization: String
    ): Status

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 40,
        @Query("max_id") maxId: String? = null
    ): List<Notification>

    @POST("notifications/{id}/dismiss")
    suspend fun dismissNotification(
        @Path("id") id: String,
        @Header("Authorization") authorization: String
    )

    // Account statuses
    @GET("accounts/{id}/statuses")
    suspend fun getAccountStatuses(
        @Path("id") accountId: String,
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 40,
        @Query("exclude_replies") excludeReplies: Boolean = true
    ): List<Status>

    // Search
    @GET("search")
    suspend fun search(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("resolve") resolve: Boolean = true
    ): SearchResult

    // Update profile
    @FormUrlEncoded
    @PATCH("accounts/update_credentials")
    suspend fun updateCredentials(
        @Header("Authorization") authorization: String,
        @Field("display_name") displayName: String? = null,
        @Field("note") note: String? = null
    ): Account

    // Trends
    @GET("trends/statuses")
    suspend fun getTrendingStatuses(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20
    ): List<Status>

    @GET("trends/tags")
    suspend fun getTrendingTags(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 10
    ): List<Tag>
}
