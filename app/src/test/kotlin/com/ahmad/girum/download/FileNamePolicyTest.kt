package com.ahmad.girum.download

import com.ahmad.girum.data.DownloadCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class FileNamePolicyTest {
    @Test
    fun sanitizesUnsafeCharacters() {
        assertEquals("bad_name.mp4", FileNamePolicy.sanitize("bad:name.mp4"))
        assertEquals("download", FileNamePolicy.sanitize("???", fallback = "download"))
    }

    @Test
    fun categorizesCommonExtensions() {
        assertEquals(DownloadCategory.VIDEOS, FileNamePolicy.categoryForFileName("clip.mp4"))
        assertEquals(DownloadCategory.MUSIC, FileNamePolicy.categoryForFileName("mix.mp3"))
        assertEquals(DownloadCategory.FILES, FileNamePolicy.categoryForFileName("archive.zip"))
    }

    @Test
    fun addsDefaultExtensionWhenMissing() {
        assertEquals("video.mp4", FileNamePolicy.extensionOrDefault("video", "mp4"))
        assertEquals("video.webm", FileNamePolicy.extensionOrDefault("video.webm", "mp4"))
    }
}
