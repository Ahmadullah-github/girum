package com.ahmad.girum.download

import android.content.Context
import androidx.core.content.ContextCompat
import com.ahmad.girum.data.DownloadCategory
import com.ahmad.girum.data.DownloadDao
import com.ahmad.girum.data.DownloadItemEntity
import com.ahmad.girum.data.DownloadPlatform
import com.ahmad.girum.data.DownloadStatus
import com.ahmad.girum.data.DownloadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DownloadRepository(
    private val context: Context,
    private val dao: DownloadDao,
    private val analyzer: UrlAnalyzer,
) {
    val downloads: Flow<List<DownloadItemEntity>> = dao.observeAll()

    suspend fun enqueueFromInput(input: String): EnqueueResult = withContext(Dispatchers.IO) {
        val analysis = analyzer.analyze(input)
        when (analysis.kind) {
            LinkKind.DIRECT -> enqueueDirect(analysis)
            LinkKind.YOUTUBE_VIDEO -> enqueueYoutubeVideo(analysis)
            LinkKind.YOUTUBE_PLAYLIST -> enqueueYoutubePlaylist(analysis)
            LinkKind.UNSUPPORTED -> EnqueueResult.Error("Unsupported or invalid link")
        }
    }

    suspend fun pause(id: String) {
        dao.updateStatus(id, DownloadStatus.PAUSED.name)
        context.startService(DownloadForegroundService.pauseIntent(context, id))
    }

    suspend fun resume(id: String) {
        dao.updateStatus(id, DownloadStatus.QUEUED.name)
        startWorker()
    }

    suspend fun retry(id: String) {
        dao.resetForRetry(id)
        startWorker()
    }

    suspend fun cancel(id: String) {
        dao.updateStatus(id, DownloadStatus.CANCELED.name)
        context.startService(DownloadForegroundService.cancelIntent(context, id))
    }

    fun startWorker() {
        ContextCompat.startForegroundService(context, DownloadForegroundService.startIntent(context))
    }

    private suspend fun enqueueDirect(analysis: LinkAnalysis): EnqueueResult {
        val name = FileNamePolicy.sanitize(analysis.suggestedName, "download")
        val item = DownloadItemEntity(
            sourceUrl = analysis.url,
            title = name,
            platform = DownloadPlatform.DIRECT.name,
            type = DownloadType.DIRECT_FILE.name,
            status = DownloadStatus.QUEUED.name,
            category = FileNamePolicy.categoryForFileName(name).name,
            outputName = name,
        )
        dao.upsert(item)
        startWorker()
        return EnqueueResult.Success(1, "Added direct download")
    }

    private suspend fun enqueueYoutubeVideo(analysis: LinkAnalysis): EnqueueResult {
        val title = "Preparing YouTube video..."
        val item = DownloadItemEntity(
            sourceUrl = analysis.url,
            title = title,
            platform = DownloadPlatform.YOUTUBE.name,
            type = DownloadType.YOUTUBE_VIDEO.name,
            status = DownloadStatus.QUEUED.name,
            category = DownloadCategory.VIDEOS.name,
            outputName = FileNamePolicy.extensionOrDefault(title, "mp4"),
        )
        dao.upsert(item)
        startWorker()
        return EnqueueResult.Success(1, "Added YouTube video")
    }

    private suspend fun enqueueYoutubePlaylist(analysis: LinkAnalysis): EnqueueResult {
        val title = "Preparing playlist..."
        val item = DownloadItemEntity(
            sourceUrl = analysis.url,
            title = title,
            platform = DownloadPlatform.YOUTUBE.name,
            type = DownloadType.YOUTUBE_PLAYLIST.name,
            status = DownloadStatus.QUEUED.name,
            category = DownloadCategory.PLAYLISTS.name,
            outputName = FileNamePolicy.extensionOrDefault(title, "mp4"),
            playlistTitle = analysis.suggestedName,
        )
        dao.upsert(item)
        startWorker()
        return EnqueueResult.Success(1, "Added playlist for background analysis")
    }
}

sealed interface EnqueueResult {
    data class Success(val count: Int, val message: String) : EnqueueResult
    data class Error(val message: String) : EnqueueResult
}
