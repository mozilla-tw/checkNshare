package org.mozilla.check.n.share.navigation

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.mozilla.check.n.share.activity.*
import org.mozilla.check.n.share.persistence.ShareEntity

class IntentBuilder {

    companion object {
        fun askWhy(context: Context, id: Long): Intent {
            val intent = Intent()
            intent.component =
                ComponentName(context, WhyActivity::class.java)
            intent.putExtra(ShareEntity.KEY_ID, id)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return intent
        }

        fun doShare(context: Context, id: Long): Intent {
            val intent = Intent()
            intent.component =
                ComponentName(context, ShareEditorActivity::class.java)
            intent.putExtra(ShareEntity.KEY_ID, id)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return intent
        }

        fun showResultFromService(service: Service, id: Long): Intent {
            val intent = Intent()
            intent.component = ComponentName(service, ShowResultActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(ShareEntity.KEY_ID, id)
            return intent
        }

        fun checkString(context: Context, string: String): Intent {
            return Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, string)
                setType("text/plain")
                this.component = ComponentName(context, ShareProxyActivity::class.java)
            }
        }

        fun createShare(context: Context, id: Long, highlight: String?): Intent {
            return Intent().apply {
                component = ComponentName(context, SharePublishActivity::class.java)
                putExtra(ShareEntity.KEY_ID, id)
                putExtra(ShareEntity.KEY_HIGHLIGHT, highlight)
            }
        }

        fun doOnboarding(context: Context): Intent {
            return Intent().apply {
                component = ComponentName(context, OnboardingActivity::class.java)
            }
        }

        fun doOnboarding2(context: Context): Intent {
            return Intent().apply {
                component = ComponentName(context, OnboardingActivity2::class.java)
            }
        }
    }
}