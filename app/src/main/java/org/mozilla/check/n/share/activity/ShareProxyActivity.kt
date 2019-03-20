package org.mozilla.check.n.share.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.checker.CofactsChecker
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.service.CheckService

class ShareProxyActivity : AppCompatActivity() {

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        setupView()
        root_content.setOnTouchListener { view, event ->
            finish()
            true
        }

        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    val queryText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (queryText?.isNotEmpty() == true) {
                        handleTextCheck(queryText) // Handle text being sent
                    } else {
                        finish()
                    }
                }
            }
            else -> {
                //  nothing to handle, finish self
                finish()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun setupView() {
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    private fun handleTextCheck(inputText: String) {
        GlobalScope.launch {
            val id = addShare(ShareEntity(inputText))
            val liveShareEntity = if (id <= 0L) {
                (application as MainApplication).database.shareDao().getShare(inputText)
            } else {
                (application as MainApplication).database.shareDao().getShare(id)
            }

            GlobalScope.launch(Dispatchers.Main) {
                liveShareEntity.observe(this@ShareProxyActivity, object : Observer<ShareEntity> {
                    var checked = false

                    override fun onChanged(shareEntity: ShareEntity) {
                        if (checked) {
                            val resultView = addCheckResultView(shareEntity.cofactsResponse)
                            resultView.findViewById<Button>(R.id.btn_share)
                                .setOnClickListener(object : View.OnClickListener {
                                    override fun onClick(view: View) {
                                        val intent = Intent()
                                        intent.component =
                                            ComponentName(applicationContext, ShareEditorActivity::class.java)
                                        intent.putExtra(ShareEntity.KEY_ID, shareEntity.id)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        finish()
                                    }
                                })
                        } else {
                            checked = true
                            checkRumor(shareEntity)
                        }
                    }
                })
            }
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

    private fun addCheckResultView(response: Int): View {
        val viewId = when (response) {
            ShareEntity.RESPONSE_FALSE -> R.layout.check_result_false
//            0 -> R.layout.check_result_neutral
            else -> R.layout.check_result_true
        }

        return layoutInflater.inflate(viewId, root_content)
    }

    private fun handleProxySend(intent: Intent) {
        //  insert into database

        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            val queryText = intent.getStringExtra(Intent.EXTRA_TEXT)

            GlobalScope.launch {
                addShare(ShareEntity(queryText))
            }


            val queryIntent = Intent(this, CheckService::class.java)
            queryIntent.putExtra(Intent.EXTRA_TEXT, queryText)
            startService(queryIntent)

            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.title_share_chooser))
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                it
            )
            startActivityForResult(Intent.createChooser(sendIntent, getString(R.string.title_share_chooser)), 1)
        }


    }

    fun addShare(share: ShareEntity): Long {
        return (application as MainApplication).database.shareDao().addShare(share)
    }

}