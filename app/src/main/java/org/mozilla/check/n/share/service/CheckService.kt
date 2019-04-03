package org.mozilla.check.n.share.service

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.checker.CofactsChecker
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.telemetry.TelemetryWrapper
import android.graphics.Bitmap
import org.mozilla.check.n.share.activity.WhyActivity


const val SHOW_NOTIFICATION = "show_notification"

class CheckService : IntentService(CheckService::class.java.simpleName) {

    private val handler = Handler()
    private val notificationId = "copy_warning"

    override fun onHandleIntent(intent: Intent?) {
        val queryText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: return
        TelemetryWrapper.queue(TelemetryWrapper.Category.HANDLE_QUERY, TelemetryWrapper.ExtraKey.SEARCH_VALUE, queryText)
        val showNotification: Boolean = intent.getBooleanExtra(SHOW_NOTIFICATION, false)
        handleTextCheck(queryText, showNotification)
    }


    private fun handleTextCheck(inputText: String, showNotification: Boolean) {
        val id = addShare(ShareEntity(inputText))
        val liveShareEntity = if (id <= 0L) {
            (application as MainApplication).database.shareDao().getShare(inputText)
        } else {
            (application as MainApplication).database.shareDao().getShare(id)
        }

        GlobalScope.launch(Dispatchers.Main) {
            liveShareEntity.observeForever(object : Observer<ShareEntity> {
                var checked = false

                override fun onChanged(shareEntity: ShareEntity) {
                    if (checked) {
                        liveShareEntity.removeObserver(this)
                        stopSelf()
                        if (showNotification) {
                            if (shareEntity.cofactsResponse != ShareEntity.RESPONSE_FALSE) {
                                return
                            }
                            val intent = IntentBuilder.askWhy(this@CheckService, shareEntity.id)
                            intent.putExtra(WhyActivity.FROM_NOTIFICATION, true)
                            TelemetryWrapper.queue(TelemetryWrapper.Category.SHOW_MISINFO_NOTIFICATION)
                            val builder = NotificationCompat.Builder(this@CheckService, notificationId)
                            builder.setContentTitle("糟糕您正在複製的文字「" + shareEntity.contentText.substring(0, 5) + "⋯」含有爭議！")
                            builder.setSmallIcon(R.drawable.ic_notification)
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_misinfo)
                            val paint = Paint()
                            paint.colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                            val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmapResult)
                            canvas.drawBitmap(bitmap, 0f, 0f, paint)
                            builder.setLargeIcon(bitmapResult)
                            builder.setContentText("點這裡看查證結果。")
                            builder.setVibrate(longArrayOf(0))
                            builder.setContentIntent(PendingIntent.getActivity(this@CheckService, 5566, intent, PendingIntent.FLAG_ONE_SHOT))
                            if (Build.VERSION.SDK_INT >= 24) {
                                builder.priority = NotificationManager.IMPORTANCE_HIGH
                            }
                            val mNotificationManager =
                                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (Build.VERSION.SDK_INT >= 26) {
                                val mChannel = NotificationChannel(notificationId, "WHATEVE", NotificationManager.IMPORTANCE_HIGH)
                                mChannel.enableVibration(true)
                                mChannel.vibrationPattern = longArrayOf(0)

                                // Configure the notification channel.
                                mChannel.description = "CHANNEL"
                                mNotificationManager.createNotificationChannel(mChannel)
                            }
                            mNotificationManager.notify(5566, builder.build())

                        } else {
                            val intent = IntentBuilder.showResultFromService(this@CheckService, shareEntity.id)
                            startActivity(intent)
                        }
                    } else {
                        checked = true
                        checkRumor(shareEntity)
                    }
                }
            })
        }
    }

    private fun checkRumor(shareEntity: ShareEntity) {

        val checker = CofactsChecker((application as MainApplication).apolloClient)
        checker.check(shareEntity, object : CofactsChecker.CheckCallback {
            override fun provideHandler(): Handler = handler

            override fun onCheckResult(shareEntity: ShareEntity) {
                GlobalScope.launch {
                    (application as MainApplication).database.shareDao().updateShare(shareEntity)
                }
            }
        })
    }

    fun addShare(share: ShareEntity): Long {
        return (application as MainApplication).database.shareDao().addShare(share)
    }
}