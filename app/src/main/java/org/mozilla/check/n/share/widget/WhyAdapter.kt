package org.mozilla.check.n.share.widget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.andext.findViewOften
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.persistence.ShareEntity

private const val VIEW_TYPE_FOOTER = 0
private const val VIEW_TYPE_NORMAL = 1

class WhyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var result: ShareEntity

    private lateinit var explanations: JSONArray

    override fun getItemCount(): Int {
        return if (::explanations.isInitialized) {
            explanations.length() + 1
        } else {
            0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == explanations.length()) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_NORMAL) {
            val title = holder.findViewOften<TextView>(R.id.why_title)
            val details = holder.findViewOften<TextView>(R.id.why_details)
            // Not possible if position exist
            val item: JSONObject = explanations.getJSONObject(position)
            title.setBackgroundColor(item.getColor())
            title.text = item.getTitle()
            details.text = item.optString("TEXT")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_NORMAL) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.whyitem, parent, false)
            val shareItemHolder = ShareItemHolder(itemView)
            return shareItemHolder
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.whyitem_footer, parent, false)
            itemView.setOnClickListener { parent.context.startActivity(IntentBuilder.doShare(parent.context, result.id)) }
            return ShareItemHolder(itemView)
        }
    }

    fun initResult(shareEntity: ShareEntity) {
        result = shareEntity
        explanations = JSONArray(result.cofactsExplanation)
        notifyDataSetChanged()
    }

    private fun JSONObject.getColor(): Int {
        return when (this.getType()) {
            RESPONSE_NOT_RUMOR -> {
                Color.parseColor("#37966F")
            }
            RESPONSE_RUMOR -> {
                Color.parseColor("#F1344B")
            }
            else -> {
                Color.parseColor("#414141")
            }
        }
    }

    private fun JSONObject.getTitle(): String {
        return when (this.getType()) {
            RESPONSE_NOT_RUMOR -> {
                "認定為屬實之說明"
            }
            RESPONSE_RUMOR -> {
                "認定為含有不實內容之說明"
            }
            else -> {
                "其他說明"
            }
        }
    }

    private fun JSONObject.getType(): Int {
        return when(this.getString("TYPE")) {
            "RUMOR" -> RESPONSE_RUMOR
            "NOT_RUMOR" -> RESPONSE_NOT_RUMOR
            "NOT_ARTICLE" -> RESPONSE_OTHER
            else -> RESPONSE_OTHER
        }
    }

    companion object {
        const val RESPONSE_RUMOR = 2
        const val RESPONSE_NOT_RUMOR = 1
        const val RESPONSE_OTHER = 0
    }
}