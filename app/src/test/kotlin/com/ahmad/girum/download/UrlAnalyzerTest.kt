package com.ahmad.girum.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlAnalyzerTest {
    private val analyzer = UrlAnalyzer()

    @Test
    fun identifiesDirectDownload() {
        val result = analyzer.analyze("https://example.com/files/movie.mp4")

        assertEquals(LinkKind.DIRECT, result.kind)
        assertEquals("movie.mp4", result.suggestedName)
    }

    @Test
    fun identifiesYoutubeVideo() {
        val result = analyzer.analyze("https://youtu.be/abc123")

        assertEquals(LinkKind.YOUTUBE_VIDEO, result.kind)
    }

    @Test
    fun identifiesYoutubeShortsAsVideo() {
        val result = analyzer.analyze("https://www.youtube.com/shorts/abc123?si=shared")

        assertEquals(LinkKind.YOUTUBE_VIDEO, result.kind)
    }

    @Test
    fun identifiesYoutubePlaylist() {
        val result = analyzer.analyze("https://www.youtube.com/watch?v=abc123&list=PL123")

        assertEquals(LinkKind.YOUTUBE_PLAYLIST, result.kind)
    }

    @Test
    fun identifiesExplicitPlaylistUrl() {
        val result = analyzer.analyze("https://youtube.com/playlist?list=PL123")

        assertEquals(LinkKind.YOUTUBE_PLAYLIST, result.kind)
    }

    @Test
    fun extractsUrlFromSharedText() {
        val result = analyzer.extractUrl("Watch this: https://example.com/file.zip.")

        assertEquals("https://example.com/file.zip", result)
    }

    @Test
    fun acceptsBareDomainsAndNormalizesScheme() {
        val result = analyzer.analyze("youtube.com/watch?v=abc123")

        assertEquals(LinkKind.YOUTUBE_VIDEO, result.kind)
        assertEquals("https://youtube.com/watch?v=abc123", result.url)
    }

    @Test
    fun ignoresUnsupportedSchemes() {
        val result = analyzer.analyze("ftp://example.com/file.zip")

        assertEquals(LinkKind.UNSUPPORTED, result.kind)
    }

    @Test
    fun returnsNullForTextWithoutUrl() {
        assertNull(analyzer.extractUrl("hello"))
    }
}
