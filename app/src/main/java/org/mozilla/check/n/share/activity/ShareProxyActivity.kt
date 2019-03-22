package org.mozilla.check.n.share.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.check.n.share.service.CheckService

class ShareProxyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    val queryText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (queryText?.isNotEmpty() == true) {
                        startService(Intent().apply {
                            component = ComponentName(applicationContext, CheckService::class.java)
                            putExtra(Intent.EXTRA_TEXT, queryText)
                        })

                    }
                    finish()
                }
            }
            else -> {
                //  nothing to handle, finish self
                finish()
            }
        }
    }


}