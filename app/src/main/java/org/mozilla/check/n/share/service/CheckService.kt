package org.mozilla.check.n.share.service

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.activity.ShowResultActivity
import org.mozilla.check.n.share.checker.CofactsChecker
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.persistence.ShareEntity


const val SHOW_NOTIFICATION = "show_notification"

class CheckService : IntentService(CheckService::class.java.simpleName) {

    private val handler = Handler()
    private val notificationId = "copy_warning"

    override fun onHandleIntent(intent: Intent?) {
        val queryText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: return
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
                        val intent = IntentBuilder.showResultFromService(this@CheckService, shareEntity.id)
                        if (showNotification) {
                            if (shareEntity.cofactsResponse != ShareEntity.RESPONSE_FALSE) {
                                return
                            }
                            val builder = NotificationCompat.Builder(this@CheckService, notificationId)
                            builder.setContentText("糟糕您正在複製的內容含有爭議！")
                            builder.setSubText("點這裡看查證結果。")
                            builder.setVibrate(longArrayOf(0))
                            builder.setContentIntent(PendingIntent.getActivity(this@CheckService, 5566, intent, PendingIntent.FLAG_ONE_SHOT))
                            builder.setSmallIcon(org.mozilla.check.n.share.R.drawable.notification_bg)
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