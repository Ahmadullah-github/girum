package com.ahmad.girum.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sourceUrl: String,
    val title: String,
    val platform: String,
    val type: String,
    val status: String,
    val category: String,
    val outputName: String,
    val outputUri: String? = null,
    val tempPath: String? = null,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val speedBytesPerSecond: Long = 0L,
    val etaSeconds: Long = -1L,
    val progress: Float = 0f,
    val playlistTitle: String? = null,
    val playlistIndex: Int? = null,
    val playlistTotal: Int? = null,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
