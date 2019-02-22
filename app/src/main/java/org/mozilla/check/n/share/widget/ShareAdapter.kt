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

    fun setShares(list: List<ShareEntity>) {
        shareList = list
    }

    fun setLongClickListener(listener: View.OnLongClickListener) {
        onLongClickListener = listener
    }


}

class ShareItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun setLongClickListener(listener: View.OnLongClickListener) {
        itemView.setOnLongClickListener(listener)
    }

    fun bind(shareItem: ShareEntity) {
        val resources = itemView.resources

        val color = if (shareItem.cofactsResponse >= 100) {
            ResourcesCompat.getColor(resources, android.R.color.holo_red_light, null)
        } else if (shareItem.cofactsResponse > 0) {
            ResourcesCompat.getColor(resources, android.R.color.holo_orange_light, null)
        } else {
            ResourcesCompat.getColor(resources, android.R.color.transparent, null)
        }
        itemView.setBackgroundColor(color)

        val contentText: TextView = findViewOften(R.id.share_item_text)
        contentText.text = shareItem.contentText
        if (color == ResourcesCompat.getColor(resources, android.R.color.transparent, null)) {
            contentText.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
        } else {
            contentText.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
        }


    }

}
