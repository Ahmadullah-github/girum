package com.ahmad.girum.data

import org.junit.Assert.assertEquals
import org.junit.Test

class QueueStatsCalculatorTest {
    @Test
    fun countsQueueBuckets() {
        val items = listOf(
            item(DownloadStatus.RUNNING),
            item(DownloadStatus.QUEUED),
            item(DownloadStatus.PAUSED),
            item(DownloadStatus.COMPLETED),
            item(DownloadStatus.FAILED),
            item(DownloadStatus.EXPANDED),
        )

        val stats = QueueStatsCalculator.from(items)

        assertEquals(1, stats.active)
        assertEquals(2, stats.queued)
        assertEquals(1, stats.completed)
        assertEquals(1, stats.failed)
    }

    private fun item(status: DownloadStatus): DownloadItemEntity {
        return DownloadItemEntity(
            sourceUrl = "https://example.com/file",
            title = "file",
            platform = DownloadPlatform.DIRECT.name,
            type = DownloadType.DIRECT_FILE.name,
            status = status.name,
            category = DownloadCategory.FILES.name,
            outputName = "file",
        )
    }
}
