package com.void200.runku.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

/**
 * Foreground service that keeps GPS tracking alive while the app is backgrounded
 * during a run. The actual sample stream is observed by the ViewModel; this
 * service only holds the foreground notification + process priority.
 */
class TrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "runku_tracking"
        const val NOTIF_ID = 4001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notif = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, notif)
        }
        // Hold a partial wake lock so the CPU stays awake while tracking,
        // preventing the service from being killed under memory pressure.
        val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "runku:tracking").apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    override fun onDestroy() {
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun buildNotification(): Notification {
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, CHANNEL_ID) else @Suppress("DEPRECATION") Notification.Builder(this)
        // Make notification persistent and non-dismissible so Android doesn't
        // kill the foreground service under memory pressure.
        return b.setContentTitle("runku · merekam")
            .setContentText("Pelacakan lari aktif — jangan tutup notifikasi ini")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(Notification.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Pelacakan", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(ch)
        }
    }
}
