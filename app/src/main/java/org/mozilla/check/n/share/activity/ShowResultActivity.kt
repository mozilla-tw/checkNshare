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
            if (it.cofactsResponse == ShareEntity.RESPONSE_FALSE) {
                resultView.findViewById<Button>(R.id.action)
                    .setOnClickListener {
                        startActivity(IntentBuilder.askWhy(this@ShowResultActivity, entity.id))
                        finish()
                    }
            } else {
                resultView.findViewById<Button>(R.id.action)
                    .setOnClickListener {
                        startActivity(IntentBuilder.doShare(this@ShowResultActivity, entity.id))
                        finish()
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