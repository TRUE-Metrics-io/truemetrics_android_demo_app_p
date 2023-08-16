package io.truemetrics.demo

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationsHelper(private val context: Context) {

    companion object {
        const val FOREGROUND_SERVICE_CHANNEL = "FOREGROUND_SERVICE"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    fun createNotificationChannels(){
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(FOREGROUND_SERVICE_CHANNEL, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.notification_channel_foreground_service))
                .setShowBadge(false)
                .build()
        )
    }

    fun createForegroundServiceNotification() : Notification {
        val builder = NotificationCompat.Builder(context, FOREGROUND_SERVICE_CHANNEL)
            .setContentTitle(context.getString(R.string.notification_foreground_service))
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setOngoing(true)

        return builder.build()
    }
}