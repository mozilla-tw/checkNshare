package org.mozilla.check.n.share.widget

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.andext.findViewOften
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity

class ShareAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var shareList: List<ShareEntity> = emptyList()
    private var onLongClickListener: View.OnLongClickListener? = null
    private var onClickListener: View.OnClickListener? = null

    interface OnLongClickListener {
        fun onLongClick(itemView: View, shareEntity: ShareEntity): Boolean
    }

    interface OnClickListener {
        fun onClick(itemView: View, shareEntity: ShareEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.share_item, parent, false)
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

        onClickListener?.let{
            holder.setClickListener(it)
        }
        holder.findViewOften<View>(R.id.indicator).setBackgroundColor(shareEntity.getColor())
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as ShareItemHolder).unBind()
        super.onViewRecycled(holder)
    }

    fun setShares(list: List<ShareEntity>) {
        shareList = list.reversed()
    }

    fun setLongClickListener(listener: OnLongClickListener) {
        onLongClickListener = View.OnLongClickListener {
            listener.onLongClick(it, it.getTag(R.id.key_holder_item) as ShareEntity)
        }
    }

    fun setClickListener(listener: OnClickListener) {
        onClickListener = View.OnClickListener {
            listener.onClick(it, it.getTag(R.id.key_holder_item) as ShareEntity)
        }
    }
}

class ShareItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun setClickListener(listener: View.OnClickListener) {
        itemView.setOnClickListener(listener)
    }

    fun setLongClickListener(listener: View.OnLongClickListener) {
        itemView.setOnLongClickListener(listener)
    }

    fun unBind() {
        itemView.setTag(R.id.key_holder_item, null)
    }

    fun bind(shareItem: ShareEntity) {
        val contentText: TextView = findViewOften(R.id.share_item_text1)
        contentText.text = shareItem.contentText

        itemView.setTag(R.id.key_holder_item, shareItem)
    }
}
