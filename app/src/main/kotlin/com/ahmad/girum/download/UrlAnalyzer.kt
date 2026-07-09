package com.ahmad.girum.download

import java.net.URI

enum class LinkKind {
    DIRECT,
    YOUTUBE_VIDEO,
    YOUTUBE_PLAYLIST,
    UNSUPPORTED,
}

data class LinkAnalysis(
    val kind: LinkKind,
    val url: String,
    val suggestedName: String,
)

class UrlAnalyzer {
    private val urlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    fun analyze(input: String): LinkAnalysis {
        val url = extractUrl(input) ?: return LinkAnalysis(
            kind = LinkKind.UNSUPPORTED,
            url = input.trim(),
            suggestedName = "download",
        )
        val uri = runCatching { URI(url) }.getOrNull() ?: return LinkAnalysis(
            kind = LinkKind.UNSUPPORTED,
            url = url,
            suggestedName = "download",
        )
        val host = uri.host.orEmpty().lowercase()
        val path = uri.path.orEmpty()
        val isYoutube = host == "youtu.be" || host.endsWith(".youtube.com") || host == "youtube.com" || host == "m.youtube.com"
        val hasPlaylist = uri.rawQuery
            .orEmpty()
            .split('&')
            .any { part -> part.substringBefore('=') == "list" && part.substringAfter('=', "").isNotBlank() }
        val kind = when {
            isYoutube && hasPlaylist -> LinkKind.YOUTUBE_PLAYLIST
            isYoutube -> LinkKind.YOUTUBE_VIDEO
            uri.scheme == "http" || uri.scheme == "https" -> LinkKind.DIRECT
            else -> LinkKind.UNSUPPORTED
        }
        val suggestedName = FileNamePolicy.sanitize(
            rawName = path.substringAfterLast('/').ifBlank { host.ifBlank { "download" } },
            fallback = "download",
        )
        return LinkAnalysis(kind = kind, url = url, suggestedName = suggestedName)
    }

    fun extractUrl(input: String): String? {
        return urlRegex.find(input)?.value?.trimEnd('.', ',', ')', ']')
    }
}
