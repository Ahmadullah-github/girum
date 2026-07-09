package com.ahmad.girum.download

import java.util.Locale

data class YtDlpProgressUpdate(
    val progress: Float,
    val etaSeconds: Long?,
    val downloadedBytes: Long?,
    val totalBytes: Long?,
    val speedBytesPerSecond: Long?,
)

object YtDlpProgressParser {
    private val sizePattern = Regex(
        pattern = """of\s+~?\s*([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?i?B|[KMGT]?B)""",
        option = RegexOption.IGNORE_CASE,
    )
    private val speedPattern = Regex(
        pattern = """at\s+([0-9]+(?:\.[0-9]+)?)\s*([KMGT]?i?B|[KMGT]?B)/s""",
        option = RegexOption.IGNORE_CASE,
    )
    private val etaPattern = Regex("""ETA\s+([0-9:]+)""", RegexOption.IGNORE_CASE)

    fun parse(progressPercent: Float, etaSeconds: Long, line: String?): YtDlpProgressUpdate {
        val normalizedProgress = if (progressPercent.isFinite()) {
            (progressPercent / 100f).coerceIn(0f, 1f)
        } else {
            0f
        }
        val totalBytes = parseSize(sizePattern.find(line.orEmpty()))
        val downloadedBytes = totalBytes?.let { total ->
            if (normalizedProgress > 0f) (total * normalizedProgress).toLong() else 0L
        }
        return YtDlpProgressUpdate(
            progress = normalizedProgress,
            etaSeconds = etaSeconds.takeIf { it >= 0L } ?: parseEta(line),
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            speedBytesPerSecond = parseSize(speedPattern.find(line.orEmpty())),
        )
    }

    private fun parseSize(match: MatchResult?): Long? {
        if (match == null) return null
        val amount = match.groupValues.getOrNull(1)?.toDoubleOrNull() ?: return null
        val unit = match.groupValues.getOrNull(2).orEmpty().uppercase(Locale.US)
        val multiplier = when (unit) {
            "B" -> 1L
            "KB", "KIB" -> 1024L
            "MB", "MIB" -> 1024L * 1024L
            "GB", "GIB" -> 1024L * 1024L * 1024L
            "TB", "TIB" -> 1024L * 1024L * 1024L * 1024L
            else -> return null
        }
        return (amount * multiplier).toLong()
    }

    private fun parseEta(line: String?): Long? {
        val raw = etaPattern.find(line.orEmpty())?.groupValues?.getOrNull(1) ?: return null
        val parts = raw.split(':').mapNotNull { it.toLongOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60L + parts[1]
            3 -> parts[0] * 3600L + parts[1] * 60L + parts[2]
            else -> null
        }
    }
}
