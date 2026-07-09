package com.ahmad.girum.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.ahmad.girum.data.DownloadCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLConnection

class StorageWriter(private val context: Context) {
    private val appContext = context.applicationContext

    fun tempFile(id: String, outputName: String): File {
        val dir = File(appContext.externalCacheDir ?: appContext.cacheDir, "downloads/$id")
        dir.mkdirs()
        return File(dir, FileNamePolicy.sanitize(outputName))
    }

    fun mediaWorkDir(id: String): File {
        val dir = File(appContext.getExternalFilesDir("media-work") ?: appContext.filesDir, id)
        dir.mkdirs()
        return dir
    }

    suspend fun promote(tempFile: File, outputName: String, categoryName: String): Uri? = withContext(Dispatchers.IO) {
        if (!tempFile.exists()) return@withContext null
        val category = runCatching { DownloadCategory.valueOf(categoryName) }.getOrDefault(DownloadCategory.FILES)
        val safeName = FileNamePolicy.sanitize(outputName, tempFile.name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeWithMediaStore(tempFile, safeName, category)
        } else {
            writeLegacy(tempFile, safeName, category)
        }
    }

    private fun writeWithMediaStore(tempFile: File, outputName: String, category: DownloadCategory): Uri? {
        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, outputName)
            put(MediaStore.MediaColumns.MIME_TYPE, guessMime(outputName))
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Girum/${category.folderName}")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return null
        return try {
            resolver.openOutputStream(uri)?.use { output ->
                tempFile.inputStream().use { input -> input.copyTo(output) }
            } ?: return null
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            tempFile.delete()
            uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun writeLegacy(tempFile: File, outputName: String, category: DownloadCategory): Uri {
        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Girum/${category.folderName}",
        )
        outputDir.mkdirs()
        val outputFile = uniqueFile(outputDir, outputName)
        tempFile.copyTo(outputFile, overwrite = true)
        tempFile.delete()
        return Uri.fromFile(outputFile)
    }

    private fun uniqueFile(dir: File, outputName: String): File {
        val base = outputName.substringBeforeLast('.', outputName)
        val ext = outputName.substringAfterLast('.', "")
        var candidate = File(dir, outputName)
        var index = 1
        while (candidate.exists()) {
            val suffix = if (ext.isBlank()) " ($index)" else " ($index).$ext"
            candidate = File(dir, "$base$suffix")
            index++
        }
        return candidate
    }

    private fun guessMime(outputName: String): String {
        return URLConnection.guessContentTypeFromName(outputName) ?: "application/octet-stream"
    }
}
