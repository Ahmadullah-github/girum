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
    private val urlRegex = Regex(
        pattern = """(?i)\b((?:https?://)?(?:[a-z0-9-]+\.)+[a-z]{2,}(?::\d+)?(?:/[^\s"'<>]*)?)""",
    )

    fun analyze(input: String): LinkAnalysis {
        val rawUrl = extractUrl(input) ?: return LinkAnalysis(
            kind = LinkKind.UNSUPPORTED,
            url = input.trim(),
            suggestedName = "download",
        )
        val url = normalizeScheme(rawUrl)
        val uri = runCatching { URI(url) }.getOrNull() ?: return LinkAnalysis(
            kind = LinkKind.UNSUPPORTED,
            url = url,
            suggestedName = "download",
        )
        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()
        val path = uri.path.orEmpty()
        val isYoutube = isYoutubeHost(host)
        val hasPlaylist = hasQueryParam(uri, "list")
        val hasVideoId = hasQueryParam(uri, "v") ||
            host.endsWith("youtu.be") ||
            path.startsWith("/shorts/") ||
            path.startsWith("/live/") ||
            path.startsWith("/embed/")
        val kind = when {
            scheme != "http" && scheme != "https" -> LinkKind.UNSUPPORTED
            isYoutube && path.startsWith("/playlist") && hasPlaylist -> LinkKind.YOUTUBE_PLAYLIST
            isYoutube && hasPlaylist && !hasVideoId -> LinkKind.YOUTUBE_PLAYLIST
            isYoutube && hasPlaylist -> LinkKind.YOUTUBE_PLAYLIST
            isYoutube -> LinkKind.YOUTUBE_VIDEO
            else -> LinkKind.DIRECT
        }
        val suggestedName = FileNamePolicy.sanitize(
            rawName = path.substringAfterLast('/').ifBlank { host.ifBlank { "download" } },
            fallback = "download",
        )
        return LinkAnalysis(kind = kind, url = url, suggestedName = suggestedName)
    }

    fun extractUrl(input: String): String? {
        val match = urlRegex.find(input) ?: return null
        if (match.range.first >= 3 && input.substring(0, match.range.first).endsWith("://")) {
            return null
        }
        return match.value.trimEnd('.', ',', ';', ':', ')', ']', '}', '؟', '،')
    }

    private fun normalizeScheme(url: String): String {
        return if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
            url
        } else {
            "https://$url"
        }
    }

    private fun isYoutubeHost(host: String): Boolean {
        return host == "youtube.com" ||
            host.endsWith(".youtube.com") ||
            host == "youtu.be" ||
            host.endsWith(".youtu.be") ||
            host == "youtube-nocookie.com" ||
            host.endsWith(".youtube-nocookie.com")
    }

    private fun hasQueryParam(uri: URI, key: String): Boolean {
        return uri.rawQuery
            .orEmpty()
            .split('&')
            .any { part ->
                part.substringBefore('=').equals(key, ignoreCase = true) &&
                    part.substringAfter('=', "").isNotBlank()
            }
    }
}
