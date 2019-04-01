package org.mozilla.check.n.share.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_publish.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.telemetry.TelemetryWrapper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SharePublishActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()

        val id = intent.getLongExtra(ShareEntity.KEY_ID, -1)
        if (id <= 0) {
            finish()
        }

        val highlight = intent.getStringExtra(ShareEntity.KEY_HIGHLIGHT)
        //  TODO: update highlight text to db -> db schema upgrade first

        val liveShareEntity = (application as MainApplication).database.shareDao().getShare(id)
        liveShareEntity.observe(this, Observer { shareEntity ->
            publish_content_textview.text = highlight
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val handled = when (item?.itemId) {
            android.R.id.home -> {
                TelemetryWrapper.queue(TelemetryWrapper.Category.PHOTO_PREVIEW_TAP_BACK)
                finish()
                true
            }
            R.id.appbar_btn_publish -> {
                TelemetryWrapper.queue(TelemetryWrapper.Category.PHOTO_PREVIEW_TAP_SHARE_PHOTO)
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
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return handled
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_publish, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupView() {
        setContentView(R.layout.activity_publish)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = resources.getString(R.string.appbar_title_publish)
        }
    }

    override fun onBackPressed() {
        TelemetryWrapper.queue(TelemetryWrapper.Category.PHOTO_PREVIEW_TAP_BACK)
        super.onBackPressed()
    }

}