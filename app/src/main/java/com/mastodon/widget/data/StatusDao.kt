package com.mastodon.widget.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mastodon.widget.api.model.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: Status)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatuses(statuses: List<Status>)

    @Query("SELECT * FROM statuses ORDER BY created_at DESC LIMIT :limit")
    fun getRecentStatuses(limit: Int = 50): Flow<List<Status>>

    @Query("SELECT * FROM statuses ORDER BY created_at DESC")
    fun getAllStatuses(): Flow<List<Status>>

    // Synchronous for widget RemoteViewsFactory (runs on background thread)
    @Query("SELECT * FROM statuses ORDER BY created_at DESC LIMIT :limit")
    fun getRecentStatusesSync(limit: Int = 20): List<Status>

    @Query("SELECT * FROM statuses WHERE id = :id")
    suspend fun getStatusById(id: String): Status?

    @Query("DELETE FROM statuses WHERE id = :id")
    suspend fun deleteStatus(id: String)

    @Query("DELETE FROM statuses")
    suspend fun deleteAllStatuses()

    @Query("SELECT COUNT(*) FROM statuses")
    fun getStatusCount(): Flow<Int>

    @Query("SELECT * FROM statuses ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestStatus(): Status?
}
