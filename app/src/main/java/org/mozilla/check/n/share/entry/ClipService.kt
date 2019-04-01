package org.mozilla.check.n.share.entry

import android.os.IBinder
import android.util.Log
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.Service
import android.content.*
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.service.CheckService
import org.mozilla.check.n.share.telemetry.TelemetryWrapper

class ClipService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") // We never bind
    }

    private val channelname = "relocation_service"
    private val id = 987
    private var destroyed = false
    private lateinit var mCM: ClipboardManager
    private lateinit var callback: () -> Unit


    // Configure the notification channel if needed
    private fun configForegroundChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // NotificationChannel API is only available for Android O and above, so we need to add the check here so IDE won't complain
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "CNS"
            val notificationChannel = NotificationChannel(channelname, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun startForeground() {
        val notificationChannelId: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configForegroundChannel(this)
            notificationChannelId = channelname
        } else {
            notificationChannelId = "not_used_notification_id"
        }
        val builder = NotificationCompat.Builder(applicationContext, notificationChannelId)
        builder.setContentTitle("享先查正在背景查證您的複製")
        val notification = builder.setSmallIcon(R.drawable.ic_notification).build()
        startForeground(id, notification)
    }

    var last: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        mCM = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        callback = fun() {
            // An emergency hotfix for callback is still triggered after stopService()
            // Could be a memory leak or some other reason but I didn't have time to fix this thoroughly now.
            if (destroyed) {
                return
            }
            val text = mCM.primaryClip?.getItemAt(0)?.text?.toString()
            TelemetryWrapper.queue(TelemetryWrapper.Category.BACKGROUND_CHECK_COPIED_TEXT, TelemetryWrapper.ExtraKey.COPY_VALUE, text.toString())
            if (text?.length?.compareTo(10) ?: -1 < 0) {
                return
            }
            // For some unknown reason call back nearly always fire twice, dedup.
            when {
                text == last -> Log.d("Clipboard", "dup!")
                TextUtils.isEmpty(text) -> Log.d("Clipboard", "empty!")
                else -> {
                    last = text
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_TEXT, text)
                    intent.putExtra(org.mozilla.check.n.share.service.SHOW_NOTIFICATION, true)
                    intent.type = "text/plain"
                    intent.component = ComponentName(this@ClipService, CheckService::class.java)
                    intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK
                    startService(intent)
                }
            }
        }
        mCM.addPrimaryClipChangedListener(callback)
        return START_STICKY
    }

    override fun onDestroy() {
        mCM.removePrimaryClipChangedListener(callback)
        destroyed = true
        super.onDestroy()
    }
}