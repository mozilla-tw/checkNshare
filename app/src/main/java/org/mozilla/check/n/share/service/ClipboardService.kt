package org.mozilla.check.n.share.service

import android.annotation.TargetApi
import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.mozilla.check.n.share.R

class ClipboardService : Service() {

    companion object {
        private const val ID_FOREGROUND = 9487
        private const val ACTION_CAPTURE_SCREEN = "action_capture"

        private fun getForegroundNotificationId(): Int {
            return ID_FOREGROUND
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createForegroundChannel(context: Context): String {
            val channelId = "foreground_channel"
            val channelName = "ScreenshotPlus Service"
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_NONE
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            return channelId
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createMessageChannel(context: Context): String {
            val channelId = "message_channel"
            val channelName = "ScreenshotPlus Message"
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            return channelId
        }

        private fun getForegroundNotification(context: Context): Notification? {
            val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createForegroundChannel(context)
            } else {
                ""
            }

            val tapIntent = Intent(ACTION_CAPTURE_SCREEN)
            tapIntent.setClass(context, ClipboardService::class.java)
            val tapPendingIntent = PendingIntent.getService(context, 0, tapIntent, 0)

            val style = NotificationCompat.BigTextStyle()
            style.bigText(context.getString(R.string.notification_channel))
            return NotificationCompat.Builder(context, channelId)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.app_share_name))
                .setContentText(context.getString(R.string.notification_channel))
                .setContentIntent(tapPendingIntent)
                .setStyle(style)
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(getForegroundNotificationId(), getForegroundNotification(applicationContext))
        val clipboardManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            val primaryClip = clipboardManager.primaryClip
            if (primaryClip == null || primaryClip.itemCount <= 0) {
                return@addPrimaryClipChangedListener
            }
            for (i in 0..primaryClip.itemCount) {
                Log.d("mmmmmmmm", primaryClip.getItemAt(i).toString())
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}