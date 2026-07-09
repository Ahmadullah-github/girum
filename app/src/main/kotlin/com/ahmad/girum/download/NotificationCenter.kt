package com.ahmad.girum.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ahmad.girum.MainActivity
import com.ahmad.girum.R

class NotificationCenter(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            DOWNLOAD_CHANNEL_ID,
            "Girum downloads",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Active Girum download progress"
        }
        manager.createNotificationChannel(channel)
    }

    fun activeNotification(activeCount: Int, queuedCount: Int): Notification {
        ensureChannel()
        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = when {
            activeCount > 0 && queuedCount > 0 -> "$activeCount active, $queuedCount queued"
            activeCount > 0 -> "$activeCount active download${if (activeCount == 1) "" else "s"}"
            queuedCount > 0 -> "$queuedCount queued download${if (queuedCount == 1) "" else "s"}"
            else -> "Preparing downloads"
        }
        return NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, activeCount > 0)
            .build()
    }

    companion object {
        const val DOWNLOAD_CHANNEL_ID = "girum_downloads"
        const val DOWNLOAD_NOTIFICATION_ID = 41
    }
}
