package org.mozilla.check.n.share.widget

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.containsKey
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.andext.findViewOften
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity

class ShareAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var shareList: List<ShareEntity> = emptyList()
    private var onLongClickListener: View.OnLongClickListener? = null

    interface OnLongClickListener {
        fun onLongClick(itemView: View, shareEntity: ShareEntity): Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.share_item, null)
        val shareItemHolder = ShareItemHolder(itemView)
        return shareItemHolder
    }

    override fun getItemCount(): Int {
        return shareList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val shareEntity = shareList[position]
        (holder as ShareItemHolder).bind(shareEntity)
        onLongClickListener?.let {
            holder.setLongClickListener(it)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as ShareItemHolder).unBind()
        super.onViewRecycled(holder)
    }

    fun setShares(list: List<ShareEntity>) {
        shareList = list
    }

    fun setLongClickListener(listener: OnLongClickListener) {
        onLongClickListener = View.OnLongClickListener {
            listener.onLongClick(it, it.getTag(R.id.key_holder_item) as ShareEntity)
        }
    }


}

class ShareItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    fun setLongClickListener(listener: View.OnLongClickListener) {
        itemView.setOnLongClickListener(listener)
    }

    fun unBind() {
        itemView.setTag(R.id.key_holder_item, null)
    }

    fun bind(shareItem: ShareEntity) {
        val resources = itemView.resources

        val color = when (shareItem.cofactsResponse) {
            ShareEntity.RESPONSE_TRUE -> {
                ResourcesCompat.getColor(resources, android.R.color.holo_green_dark, null)
            }
            ShareEntity.RESPONSE_FALSE -> {
                ResourcesCompat.getColor(resources, android.R.color.holo_red_dark, null)
            }
            else -> {
                ResourcesCompat.getColor(resources, android.R.color.transparent, null)
            }
        }
        itemView.setBackgroundColor(color)

        val contentText: TextView = findViewOften(R.id.share_item_text)
        contentText.text = shareItem.contentText
        if (color == ResourcesCompat.getColor(resources, android.R.color.transparent, null)) {
            contentText.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
        } else {
            contentText.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
        }

        itemView.setTag(R.id.key_holder_item, shareItem)
    }
}
