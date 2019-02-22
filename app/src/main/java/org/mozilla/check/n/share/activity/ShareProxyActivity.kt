package org.mozilla.check.n.share.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareDatabase
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.service.CheckService

class ShareProxyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)


        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("mmmmmmmm", "finish onActivityResult")
        finish()
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.d("mmmmmmmm", "finish onPostResume")
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("mmmmmmmm", "finish onNewIntent")
        finish()
    }

    private fun handleSendText(intent: Intent) {
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

    suspend fun addShare(share: ShareEntity) {
        (application as MainApplication).database.shareDao().addShare(share)
    }

}