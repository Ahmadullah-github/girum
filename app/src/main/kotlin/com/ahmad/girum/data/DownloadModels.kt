package com.ahmad.girum.data

enum class DownloadStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELED,
    EXPANDED,
}

enum class DownloadPlatform {
    DIRECT,
    YOUTUBE,
}

enum class DownloadType {
    DIRECT_FILE,
    YOUTUBE_VIDEO,
    YOUTUBE_PLAYLIST,
    YOUTUBE_PLAYLIST_ITEM,
}

enum class DownloadCategory(val folderName: String) {
    VIDEOS("Videos"),
    PLAYLISTS("Playlists"),
    MUSIC("Music"),
    SHORTS("Shorts"),
    FILES("Files"),
}

data class QueueStats(
    val active: Int = 0,
    val queued: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0,
)

object QueueStatsCalculator {
    fun from(items: List<DownloadItemEntity>): QueueStats {
        return QueueStats(
            active = items.count { it.status == DownloadStatus.RUNNING.name },
            queued = items.count { it.status == DownloadStatus.QUEUED.name || it.status == DownloadStatus.PAUSED.name },
            completed = items.count { it.status == DownloadStatus.COMPLETED.name },
            failed = items.count { it.status == DownloadStatus.FAILED.name },
        )
    }
}
