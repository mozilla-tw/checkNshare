package org.mozilla.check.n.share.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_edit.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.widget.HighlightTextView.OnSelectionChangeListener


class ShareEditorActivity : AppCompatActivity() {

    private var selectedText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(org.mozilla.check.n.share.R.layout.activity_edit)
        val id = intent.getLongExtra(ShareEntity.KEY_ID, -1)
        if (id <= 0) {
            finish()
        }

        edit_content_textview.onSelectionChangeListener = object : OnSelectionChangeListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                val length = edit_content_textview.text.length
                if (selStart < 0 || selEnd < 0 || selStart > length || selEnd > length) {
                    Log.d("mmmmmmmm", "s:${selStart}\te:${selEnd}")
                    selectedText = null
                } else if(selStart > selEnd){
                    selectedText = edit_content_textview.text.substring(selEnd, selStart)
                } else {
                    selectedText = edit_content_textview.text.substring(selStart, selEnd)
                }
            }

        }
        edit_content_textview.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Can now use the mode whenever (if it's not null)
                // e.g. call mActionMode.finish()
                return true // true = create the ActionMode
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.clear()
                mode.menu.clear()
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
            }
        }


        val liveShareEntity = (application as MainApplication).database.shareDao().getShare(id)
        liveShareEntity.observe(this, Observer { shareEntity ->
            edit_content_textview.text = shareEntity.contentText


            btn_share.setOnClickListener {
                startActivity(Intent().apply {
                    component = ComponentName(applicationContext, SharePublishActivity::class.java)
                    putExtra(ShareEntity.KEY_ID, shareEntity.id)
                    putExtra(ShareEntity.KEY_HIGHLIGHT, selectedText)
                })
            }


        })


    }
}