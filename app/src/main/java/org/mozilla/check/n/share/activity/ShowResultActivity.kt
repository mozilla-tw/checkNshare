package org.mozilla.check.n.share.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_share.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.telemetry.TelemetryWrapper

class ShowResultActivity : AppCompatActivity() {

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        root_content.setOnTouchListener { _, _ ->
            finish()
            true
        }
        val id = intent?.extras?.getLong(ShareEntity.KEY_ID) ?: return
        val shareEntityLiveData = (application as MainApplication).database.shareDao().getShare(id)
        shareEntityLiveData.observe(this@ShowResultActivity, Observer<ShareEntity> {
            val entity = it
            if (it == null) {
                return@Observer
            }
            val resultView = addCheckResultView(entity.cofactsResponse)
            when (entity.cofactsResponse) {
                ShareEntity.RESPONSE_NEUTRAL -> {
                    TelemetryWrapper.queue(TelemetryWrapper.Category.SHOW_NO_RESULT_PROMPT)
                }
                ShareEntity.RESPONSE_FALSE -> {
                    TelemetryWrapper.queue(TelemetryWrapper.Category.SHOW_MISINFO_PROMPT)
                }
                ShareEntity.RESPONSE_TRUE -> {
                    TelemetryWrapper.queue(TelemetryWrapper.Category.SHOW_TRUE_PROMPT)
                }
            }
            resultView.findViewById<Button>(R.id.action)
                .setOnClickListener {
                    when (entity.cofactsResponse) {
                        ShareEntity.RESPONSE_NEUTRAL -> {
                            startActivity(IntentBuilder.doShare(this@ShowResultActivity, entity.id))
                            finish()
                            TelemetryWrapper.queue(TelemetryWrapper.Category.NO_RESULT_PROMPT_TAP_SHARE)
                        }
                        ShareEntity.RESPONSE_FALSE -> {
                            startActivity(IntentBuilder.askWhy(this@ShowResultActivity, entity.id))
                            finish()
                            TelemetryWrapper.queue(TelemetryWrapper.Category.MISINFO_PROMPT_TAP_KNOW_MORE)
                        }
                        ShareEntity.RESPONSE_TRUE -> {
                            startActivity(IntentBuilder.doShare(this@ShowResultActivity, entity.id))
                            finish()
                            TelemetryWrapper.queue(TelemetryWrapper.Category.TRUE_PROMPT_TAP_SHARE)
                        }
                    }
                }
        })
    }


    private fun addCheckResultView(response: Int): View {
        val viewId = when (response) {
            ShareEntity.RESPONSE_FALSE -> R.layout.check_result_false
            ShareEntity.RESPONSE_TRUE -> R.layout.check_result_true
            else -> R.layout.check_result_neutral
        }

        return layoutInflater.inflate(viewId, root_content)
    }
}
