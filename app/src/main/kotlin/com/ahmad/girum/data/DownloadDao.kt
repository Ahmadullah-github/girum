package com.ahmad.girum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadItemEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: String): DownloadItemEntity?

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getByStatus(status: String, limit: Int): List<DownloadItemEntity>

    @Query("SELECT COUNT(*) FROM downloads WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DownloadItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DownloadItemEntity>)

    @Query(
        """
        UPDATE downloads
        SET status = :status,
            error = :error,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateStatus(
        id: String,
        status: String,
        error: String? = null,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE downloads
        SET status = :status,
            downloadedBytes = :downloadedBytes,
            totalBytes = :totalBytes,
            speedBytesPerSecond = :speedBytesPerSecond,
            etaSeconds = :etaSeconds,
            progress = :progress,
            tempPath = :tempPath,
            error = NULL,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateProgress(
        id: String,
        status: String,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBytesPerSecond: Long,
        etaSeconds: Long,
        progress: Float,
        tempPath: String?,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE downloads
        SET status = :status,
            title = :title,
            outputName = :outputName,
            outputUri = :outputUri,
            downloadedBytes = :downloadedBytes,
            totalBytes = :totalBytes,
            speedBytesPerSecond = 0,
            etaSeconds = 0,
            progress = 1,
            error = NULL,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun markCompleted(
        id: String,
        status: String,
        title: String,
        outputName: String,
        outputUri: String?,
        downloadedBytes: Long,
        totalBytes: Long,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE downloads
        SET status = :status,
            downloadedBytes = 0,
            totalBytes = -1,
            speedBytesPerSecond = 0,
            etaSeconds = -1,
            progress = 0,
            error = NULL,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun resetForRetry(
        id: String,
        status: String = DownloadStatus.QUEUED.name,
        updatedAt: Long = System.currentTimeMillis(),
    )
}
