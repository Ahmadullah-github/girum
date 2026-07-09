package com.ahmad.girum.download

import com.ahmad.girum.data.DownloadCategory
import java.net.URLDecoder
import java.util.Locale

object FileNamePolicy {
    private val unsafeCharacters = Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]")
    private val whitespace = Regex("\\s+")

    fun sanitize(rawName: String, fallback: String = "download"): String {
        val trimmed = rawName
            .substringAfterLast('/')
            .substringBefore('?')
            .substringBefore('#')
            .let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrDefault(it) }
            .replace(unsafeCharacters, "_")
            .replace(whitespace, " ")
            .trim()
            .trim('.')
        return trimmed
            .takeIf { it.isNotBlank() && it.any { character -> character.isLetterOrDigit() } }
            ?.take(180)
            ?: fallback
    }

    fun categoryForFileName(fileName: String): DownloadCategory {
        return when (fileName.substringAfterLast('.', "").lowercase(Locale.US)) {
            "mp4", "mkv", "webm", "mov", "avi", "m4v" -> DownloadCategory.VIDEOS
            "mp3", "m4a", "aac", "flac", "ogg", "wav" -> DownloadCategory.MUSIC
            "jpg", "jpeg", "png", "gif", "webp", "zip", "rar", "7z", "pdf", "doc", "docx" -> DownloadCategory.FILES
            else -> DownloadCategory.FILES
        }
    }

    fun extensionOrDefault(fileName: String, defaultExtension: String): String {
        return if (fileName.contains('.') && fileName.substringAfterLast('.').isNotBlank()) {
            fileName
        } else {
            "$fileName.$defaultExtension"
        }
    }
}
