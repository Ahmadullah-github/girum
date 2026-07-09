package com.ahmad.girum.core

import android.content.Context
import androidx.room.Room
import com.ahmad.girum.data.GirumDatabase
import com.ahmad.girum.data.SettingsRepository
import com.ahmad.girum.download.DirectDownloader
import com.ahmad.girum.download.DownloadRepository
import com.ahmad.girum.download.NotificationCenter
import com.ahmad.girum.download.StorageWriter
import com.ahmad.girum.download.UrlAnalyzer
import com.ahmad.girum.download.YtDlpMediaEngine
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: GirumDatabase = Room.databaseBuilder(
        appContext,
        GirumDatabase::class.java,
        "girum.db",
    ).build()

    val settingsRepository = SettingsRepository(appContext)
    val urlAnalyzer = UrlAnalyzer()
    val storageWriter = StorageWriter(appContext)
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    val notificationCenter = NotificationCenter(appContext)
    val directDownloader = DirectDownloader(
        client = okHttpClient,
        dao = database.downloadDao(),
        storageWriter = storageWriter,
    )
    val mediaEngine = YtDlpMediaEngine(
        context = appContext,
        dao = database.downloadDao(),
        storageWriter = storageWriter,
    )
    val downloadRepository = DownloadRepository(
        context = appContext,
        dao = database.downloadDao(),
        analyzer = urlAnalyzer,
    )
}
