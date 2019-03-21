package org.mozilla.check.n.share.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.clearSpans
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_edit.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.widget.HighlightTextView.OnSelectionChangeListener


class ShareEditorActivity : AppCompatActivity() {

    private var selectedText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()

        val id = intent.getLongExtra(ShareEntity.KEY_ID, -1)
        if (id <= 0) {
            finish()
        }

        edit_content_textview.onSelectionChangeListener = object : OnSelectionChangeListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                val length = edit_content_textview.text.length
                if (selStart < 0 || selEnd < 0 || selStart > length || selEnd > length || selStart == selEnd) {
                    selectedText = null
                } else if (selStart > selEnd) {
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
                if (selectedText.isNullOrEmpty()) {
                    showGuidingDialog()
                    return@setOnClickListener
                }


                startActivity(Intent().apply {
                    component = ComponentName(applicationContext, SharePublishActivity::class.java)
                    putExtra(ShareEntity.KEY_ID, shareEntity.id)
                    putExtra(ShareEntity.KEY_HIGHLIGHT, selectedText)
                })
            }


        })


    }

    private fun selectParagraphAutomatically() {
        val paragraph = edit_content_textview.text.split('\n')
        val selectedSpan: Spannable = edit_content_textview.text as Spannable
        Selection.removeSelection(selectedSpan)
        if (paragraph.isEmpty()) {
            Selection.selectAll(selectedSpan)
        } else {
            Selection.setSelection(selectedSpan, 0, paragraph[0].length)
        }

        //  FIXME: There is a bug that first time selection will not be highlighted.
        //  invalidate does not help. Need to find a work-around solution.
        edit_content_textview.invalidate()
    }

    private fun showGuidingDialog() {
        val builder = AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_title_edit_guiding)
            setView(R.layout.dialog_content)
            setPositiveButton("系統挑選") { dialog, which ->
                selectParagraphAutomatically()
            }
            setNegativeButton("自行選取", null)
        }
        builder.create().show()
    }

    private fun showOnBoardingDialog() {
        val builder = AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_title_edit_guiding)
            setView(R.layout.dialog_content)
            setPositiveButton(R.string.dialog_positive_acknoledged, null)
        }
        builder.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val handled = when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return handled
    }

    private fun setupView() {
        setContentView(org.mozilla.check.n.share.R.layout.activity_edit)


        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.appbar_title_editor)
        }
    }

}