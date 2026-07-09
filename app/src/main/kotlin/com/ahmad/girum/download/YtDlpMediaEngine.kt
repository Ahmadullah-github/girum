package com.ahmad.girum.download

import android.content.Context
import com.ahmad.girum.data.DownloadCategory
import com.ahmad.girum.data.DownloadDao
import com.ahmad.girum.data.DownloadItemEntity
import com.ahmad.girum.data.DownloadStatus
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class MediaEntry(
    val url: String,
    val title: String,
)

class YtDlpMediaEngine(
    private val context: Context,
    private val dao: DownloadDao,
    private val storageWriter: StorageWriter,
) {
    @Volatile private var initialized = false

    @Synchronized
    fun initialize() {
        if (initialized) return
        YoutubeDL.init(context)
        FFmpeg.init(context)
        initialized = true
    }

    suspend fun videoTitle(url: String): String = withContext(Dispatchers.IO) {
        initialize()
        val info = YoutubeDL.getInfo(url)
        FileNamePolicy.sanitize(info.title ?: info.fulltitle ?: "YouTube video", "YouTube video")
    }

    suspend fun playlistEntries(
        url: String,
        processId: String = "playlist-${UUID.randomUUID()}",
    ): List<MediaEntry> = withContext(Dispatchers.IO) {
        initialize()
        val request = YoutubeDLRequest(url)
            .addOption("--flat-playlist")
            .addOption("--dump-single-json")
            .addOption("--ignore-errors")
        val response = YoutubeDL.execute(
            request = request,
            processId = processId,
            redirectErrorStream = true,
            callback = null,
        )
        val json = JSONObject(response.out)
        val entries = json.optJSONArray("entries") ?: return@withContext emptyList()
        buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index) ?: continue
                val rawUrl = entry.optString("webpage_url")
                    .ifBlank { entry.optString("url") }
                    .ifBlank { entry.optString("id") }
                if (rawUrl.isBlank()) continue
                val normalizedUrl = if (rawUrl.startsWith("http")) rawUrl else "https://www.youtube.com/watch?v=$rawUrl"
                add(
                    MediaEntry(
                        url = normalizedUrl,
                        title = FileNamePolicy.sanitize(
                            rawName = entry.optString("title").ifBlank { "Playlist video ${index + 1}" },
                            fallback = "Playlist video ${index + 1}",
                        ),
                    ),
                )
            }
        }
    }

    suspend fun download(item: DownloadItemEntity) = withContext(Dispatchers.IO) {
        initialize()
        val preparedItem = prepareMetadata(item)
        val workDir = storageWriter.mediaWorkDir(item.id)
        workDir.listFiles()?.forEach { it.deleteRecursively() }
        val request = YoutubeDLRequest(preparedItem.sourceUrl)
            .addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
            .addOption("--merge-output-format", "mp4")
            .addOption("--newline")
            .addOption("--no-mtime")
            .addOption("-o", File(workDir, "%(title).160B.%(ext)s").absolutePath)
        dao.updateProgress(
            id = preparedItem.id,
            status = DownloadStatus.RUNNING.name,
            downloadedBytes = preparedItem.downloadedBytes,
            totalBytes = preparedItem.totalBytes,
            speedBytesPerSecond = 0L,
            etaSeconds = -1L,
            progress = preparedItem.progress,
            tempPath = workDir.absolutePath,
        )
        var lastProgressUpdateAt = 0L
        var lastDownloadedBytes = preparedItem.downloadedBytes
        var lastTotalBytes = preparedItem.totalBytes
        var lastSpeedBytesPerSecond = 0L
        var lastEtaSeconds = -1L
        YoutubeDL.execute(
            request = request,
            processId = preparedItem.id,
            redirectErrorStream = true,
        ) { progress, eta, line ->
            val now = System.currentTimeMillis()
            if (now - lastProgressUpdateAt < PROGRESS_UPDATE_INTERVAL_MS && progress < 100f) {
                return@execute
            }
            lastProgressUpdateAt = now
            val update = YtDlpProgressParser.parse(progress, eta, line)
            lastTotalBytes = update.totalBytes ?: lastTotalBytes
            lastDownloadedBytes = update.downloadedBytes ?: lastDownloadedBytes
            lastSpeedBytesPerSecond = update.speedBytesPerSecond ?: lastSpeedBytesPerSecond
            lastEtaSeconds = update.etaSeconds ?: lastEtaSeconds
            kotlinx.coroutines.runBlocking {
                dao.updateProgress(
                    id = preparedItem.id,
                    status = DownloadStatus.RUNNING.name,
                    downloadedBytes = lastDownloadedBytes,
                    totalBytes = lastTotalBytes,
                    speedBytesPerSecond = lastSpeedBytesPerSecond,
                    etaSeconds = lastEtaSeconds,
                    progress = update.progress,
                    tempPath = workDir.absolutePath,
                )
            }
        }
        val output = newestMediaFile(workDir)
            ?: throw IllegalStateException("yt-dlp finished but no output file was created")
        val safeName = FileNamePolicy.sanitize(output.name, preparedItem.outputName)
        val uri = storageWriter.promote(
            tempFile = output,
            outputName = safeName,
            categoryName = if (preparedItem.type.contains("PLAYLIST")) DownloadCategory.PLAYLISTS.name else preparedItem.category,
        )
        dao.markCompleted(
            id = preparedItem.id,
            status = DownloadStatus.COMPLETED.name,
            title = safeName,
            outputName = safeName,
            outputUri = uri?.toString(),
            downloadedBytes = output.length(),
            totalBytes = output.length(),
        )
        workDir.deleteRecursively()
    }

    private suspend fun prepareMetadata(item: DownloadItemEntity): DownloadItemEntity {
        if (!item.title.startsWith("Preparing")) return item
        val title = runCatching { videoTitle(item.sourceUrl) }.getOrDefault(item.title)
        val outputName = FileNamePolicy.extensionOrDefault(title, "mp4")
        dao.updateTitle(
            id = item.id,
            title = title,
            outputName = outputName,
        )
        return item.copy(title = title, outputName = outputName)
    }

    fun cancel(processId: String) {
        YoutubeDL.destroyProcessById(processId)
    }

    private fun newestMediaFile(workDir: File): File? {
        return workDir.walkTopDown()
            .filter { it.isFile && !it.name.endsWith(".part") && !it.name.endsWith(".ytdl") }
            .maxByOrNull { it.lastModified() }
    }

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }
}
