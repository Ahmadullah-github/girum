package com.ahmad.girum.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YtDlpProgressParserTest {
    @Test
    fun parsesSpeedSizeAndEta() {
        val update = YtDlpProgressParser.parse(
            progressPercent = 50f,
            etaSeconds = -1L,
            line = "[download] 50.0% of 100.00MiB at 2.50MiB/s ETA 00:15",
        )

        assertEquals(0.5f, update.progress)
        assertEquals(15L, update.etaSeconds)
        assertEquals(100L * 1024L * 1024L, update.totalBytes)
        assertEquals(50L * 1024L * 1024L, update.downloadedBytes)
        assertEquals((2.5 * 1024 * 1024).toLong(), update.speedBytesPerSecond)
    }

    @Test
    fun prefersCallbackEta() {
        val update = YtDlpProgressParser.parse(
            progressPercent = 12.5f,
            etaSeconds = 91L,
            line = "[download] 12.5% of 1.00GiB at 512.00KiB/s ETA 10:00",
        )

        assertEquals(91L, update.etaSeconds)
    }

    @Test
    fun handlesLinesWithoutSizeOrSpeed() {
        val update = YtDlpProgressParser.parse(
            progressPercent = 5f,
            etaSeconds = -1L,
            line = "[download] Destination: video.mp4",
        )

        assertEquals(0.05f, update.progress)
        assertNull(update.totalBytes)
        assertNull(update.speedBytesPerSecond)
    }
}
