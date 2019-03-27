package org.mozilla.check.n.share.widget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.andext.findViewOften
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity

class WhyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var result: ShareEntity

    private lateinit var explanations: JSONArray

    override fun getItemCount(): Int {
        return if (::explanations.isInitialized) {
            explanations.length()
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val title = holder.findViewOften<TextView>(R.id.why_title)
        val details = holder.findViewOften<TextView>(R.id.why_details)
        // Not possible if position exist
        val item: JSONObject = explanations.getJSONObject(position)
        title.setBackgroundColor(item.getColor())
        title.text = item.getTitle()
        details.text = item.optString("TEXT")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.whyitem, parent, false)
        val shareItemHolder = ShareItemHolder(itemView)
        return shareItemHolder
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