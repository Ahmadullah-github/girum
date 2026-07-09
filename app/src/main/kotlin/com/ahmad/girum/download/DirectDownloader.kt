package com.ahmad.girum.download

import com.ahmad.girum.data.DownloadDao
import com.ahmad.girum.data.DownloadItemEntity
import com.ahmad.girum.data.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.coroutineContext

class DirectDownloader(
    private val client: OkHttpClient,
    private val dao: DownloadDao,
    private val storageWriter: StorageWriter,
) {
    suspend fun download(item: DownloadItemEntity) = withContext(Dispatchers.IO) {
        val tempFile = storageWriter.tempFile(item.id, item.outputName)
        val existingBytes = tempFile.takeIf { it.exists() }?.length() ?: 0L
        val requestBuilder = Request.Builder().url(item.sourceUrl)
        if (existingBytes > 0L) {
            requestBuilder.header("Range", "bytes=$existingBytes-")
        }
        val request = requestBuilder.build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 206) {
                throw IllegalStateException("Download failed with HTTP ${response.code}")
            }
            val canAppend = existingBytes > 0L && response.code == 206
            val startBytes = if (canAppend) existingBytes else 0L
            if (!canAppend && tempFile.exists()) tempFile.delete()
            val responseName = contentDispositionName(response.header("Content-Disposition"))
            val finalName = FileNamePolicy.sanitize(responseName ?: item.outputName)
            val totalBytes = response.body?.contentLength()
                ?.takeIf { it >= 0L }
                ?.plus(startBytes)
                ?: item.totalBytes
            dao.updateProgress(
                id = item.id,
                status = DownloadStatus.RUNNING.name,
                downloadedBytes = startBytes,
                totalBytes = totalBytes,
                speedBytesPerSecond = 0L,
                etaSeconds = -1L,
                progress = progressFor(startBytes, totalBytes),
                tempPath = tempFile.absolutePath,
            )
            val body = response.body ?: throw IllegalStateException("Empty response body")
            var downloadedBytes = startBytes
            var lastBytes = downloadedBytes
            var lastUpdate = System.currentTimeMillis()
            body.byteStream().use { input ->
                FileOutputStream(tempFile, canAppend).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate >= 500L) {
                            val seconds = (now - lastUpdate).coerceAtLeast(1L) / 1000.0
                            val speed = ((downloadedBytes - lastBytes) / seconds).toLong()
                            val eta = if (speed > 0L && totalBytes > downloadedBytes) {
                                (totalBytes - downloadedBytes) / speed
                            } else {
                                -1L
                            }
                            dao.updateProgress(
                                id = item.id,
                                status = DownloadStatus.RUNNING.name,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                                speedBytesPerSecond = speed,
                                etaSeconds = eta,
                                progress = progressFor(downloadedBytes, totalBytes),
                                tempPath = tempFile.absolutePath,
                            )
                            lastBytes = downloadedBytes
                            lastUpdate = now
                        }
                    }
                }
            }
            val uri = storageWriter.promote(tempFile, finalName, item.category)
            dao.markCompleted(
                id = item.id,
                status = DownloadStatus.COMPLETED.name,
                title = finalName,
                outputName = finalName,
                outputUri = uri?.toString(),
                downloadedBytes = downloadedBytes,
                totalBytes = if (totalBytes > 0L) totalBytes else downloadedBytes,
            )
        }
    }

    private fun progressFor(downloadedBytes: Long, totalBytes: Long): Float {
        return if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
    }

    private fun contentDispositionName(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val fileNameStar = Regex("""filename\*=UTF-8''([^;]+)""", RegexOption.IGNORE_CASE).find(value)?.groupValues?.getOrNull(1)
        if (!fileNameStar.isNullOrBlank()) return java.net.URLDecoder.decode(fileNameStar, "UTF-8")
        return Regex("""filename="?([^";]+)"?""", RegexOption.IGNORE_CASE).find(value)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 128 * 1024
    }
}
