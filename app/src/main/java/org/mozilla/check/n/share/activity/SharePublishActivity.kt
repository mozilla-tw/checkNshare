package org.mozilla.check.n.share.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_publish.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SharePublishActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_publish)

        val id = intent.getLongExtra(ShareEntity.KEY_ID, -1)
        if (id <= 0) {
            finish()
        }

        btn_share.setOnClickListener {
            val bitmap = publish_container.drawToBitmap()
            try {

                val cachePath = File(cacheDir, "images")
                cachePath.mkdirs() // don't forget to make the directory
                val stream = FileOutputStream("$cachePath/image.png") // overwrites this image every time
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }

            val imagePath = File(cacheDir, "images")
            val newFile = File(imagePath, "image.png")
            val contentUri =
                FileProvider.getUriForFile(this@SharePublishActivity, "$packageName.fileprovider", newFile)

            if (contentUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                    setDataAndType(contentUri, contentResolver.getType(contentUri))
                    putExtra(Intent.EXTRA_STREAM, contentUri)

                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(Intent.createChooser(shareIntent, "Choose an app"))
            }
        }

        val highlight = intent.getStringExtra(ShareEntity.KEY_HIGHLIGHT)
        //  TODO: update highlight text to db -> db schema upgrade first

        val liveShareEntity = (application as MainApplication).database.shareDao().getShare(id)
        liveShareEntity.observe(this, Observer { shareEntity ->
            publish_content_textview.text = highlight
        })
    }
}