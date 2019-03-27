package org.mozilla.check.n.share.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.widget.ShareAdapter


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ShareAdapter()
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(this)

        val liveShareEntities = (application as MainApplication).database.shareDao().getShares()
        liveShareEntities.observe(this, Observer { shares ->
            adapter.setShares(shares)
            adapter.notifyDataSetChanged()
        })

        adapter.setClickListener(object : ShareAdapter.OnClickListener {
            override fun onClick(itemView: View, shareEntity: ShareEntity) {
                when (shareEntity.cofactsResponse) {
                    ShareEntity.RESPONSE_TRUE -> startActivity(IntentBuilder.doShare(this@MainActivity, shareEntity.id))
                    else -> startActivity(IntentBuilder.askWhy(this@MainActivity, shareEntity.id))
                }
            }
        })

        adapter.setLongClickListener(listener = object : ShareAdapter.OnLongClickListener {
            override fun onLongClick(itemView: View, shareEntity: ShareEntity): Boolean {
                val popupMenu = PopupMenu(this@MainActivity, itemView)
                popupMenu.menuInflater.inflate(R.menu.share_item_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        //  TODO: support remove item
                    }
                    true
                }
                popupMenu.setOnDismissListener {
                    // 控件消失时的事件
                }
                popupMenu.show()

                return true
            }
        })
        startActivity(IntentBuilder.doOnboarding(this@MainActivity))


    }
}
