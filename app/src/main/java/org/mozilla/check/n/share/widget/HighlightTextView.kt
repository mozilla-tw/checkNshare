package org.mozilla.check.n.share.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class HighlightTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var onSelectionChangeListener : OnSelectionChangeListener? = null

    interface OnSelectionChangeListener {
        fun onSelectionChanged(selStart: Int, selEnd: Int)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)

        onSelectionChangeListener?.onSelectionChanged(selStart, selEnd)
    }
}