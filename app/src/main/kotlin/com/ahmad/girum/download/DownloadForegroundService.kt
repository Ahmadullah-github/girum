package com.ahmad.girum.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.ahmad.girum.GirumApplication
import com.ahmad.girum.data.DownloadItemEntity
import com.ahmad.girum.data.DownloadPlatform
import com.ahmad.girum.data.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class DownloadForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private var pumpJob: Job? = null

    private val container by lazy { (application as GirumApplication).appContainer }
    private val dao by lazy { container.database.downloadDao() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        when (intent?.action ?: ACTION_START) {
            ACTION_PAUSE -> intent?.getStringExtra(EXTRA_ID)?.let { pause(it) }
            ACTION_CANCEL -> intent?.getStringExtra(EXTRA_ID)?.let { cancel(it) }
            ACTION_START -> ensurePump()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        activeJobs.values.forEach { it.cancel() }
        pumpJob?.cancel()
        super.onDestroy()
    }

    private fun startAsForeground() {
        val notification = container.notificationCenter.activeNotification(
            activeCount = activeJobs.size,
            queuedCount = 0,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationCenter.DOWNLOAD_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NotificationCenter.DOWNLOAD_NOTIFICATION_ID, notification)
        }
    }

    private fun pause(id: String) {
        activeJobs.remove(id)?.cancel()
        container.mediaEngine.cancel(id)
    }

    private fun cancel(id: String) {
        activeJobs.remove(id)?.cancel()
        container.mediaEngine.cancel(id)
    }

    private fun ensurePump() {
        if (pumpJob?.isActive == true) return
        pumpJob = scope.launch {
            while (isActive) {
                val openSlots = (MAX_CONCURRENT_DOWNLOADS - activeJobs.size).coerceAtLeast(0)
                if (openSlots > 0) {
                    dao.getByStatus(DownloadStatus.QUEUED.name, openSlots).forEach { item ->
                        launchDownload(item)
                    }
                }
                val queuedCount = dao.countByStatus(DownloadStatus.QUEUED.name)
                refreshNotification(queuedCount)
                if (queuedCount == 0 && activeJobs.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun launchDownload(item: DownloadItemEntity) {
        if (activeJobs.containsKey(item.id)) return
        val job = scope.launch {
            try {
                when (item.platform) {
                    DownloadPlatform.DIRECT.name -> container.directDownloader.download(item)
                    DownloadPlatform.YOUTUBE.name -> container.mediaEngine.download(item)
                    else -> throw IllegalStateException("Unsupported platform ${item.platform}")
                }
            } catch (cancelled: CancellationException) {
                val current = dao.getById(item.id)
                if (current?.status == DownloadStatus.RUNNING.name) {
                    dao.updateStatus(item.id, DownloadStatus.PAUSED.name)
                }
            } catch (error: Exception) {
                val current = dao.getById(item.id)
                if (current?.status != DownloadStatus.CANCELED.name && current?.status != DownloadStatus.PAUSED.name) {
                    dao.updateStatus(
                        id = item.id,
                        status = DownloadStatus.FAILED.name,
                        error = error.message ?: error::class.java.simpleName,
                    )
                }
            } finally {
                activeJobs.remove(item.id)
            }
        }
        activeJobs[item.id] = job
    }

    private suspend fun refreshNotification(queuedCount: Int) {
        val notification = container.notificationCenter.activeNotification(
            activeCount = activeJobs.size,
            queuedCount = queuedCount,
        )
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.notify(NotificationCenter.DOWNLOAD_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val MAX_CONCURRENT_DOWNLOADS = 2
        private const val ACTION_START = "com.ahmad.girum.action.START_DOWNLOADS"
        private const val ACTION_PAUSE = "com.ahmad.girum.action.PAUSE_DOWNLOAD"
        private const val ACTION_CANCEL = "com.ahmad.girum.action.CANCEL_DOWNLOAD"
        private const val EXTRA_ID = "download_id"

        fun startIntent(context: Context): Intent {
            return Intent(context, DownloadForegroundService::class.java).setAction(ACTION_START)
        }

        fun pauseIntent(context: Context, id: String): Intent {
            return Intent(context, DownloadForegroundService::class.java)
                .setAction(ACTION_PAUSE)
                .putExtra(EXTRA_ID, id)
        }

        fun cancelIntent(context: Context, id: String): Intent {
            return Intent(context, DownloadForegroundService::class.java)
                .setAction(ACTION_CANCEL)
                .putExtra(EXTRA_ID, id)
        }
    }
}
