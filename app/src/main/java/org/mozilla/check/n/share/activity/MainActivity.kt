package org.mozilla.check.n.share.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
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

        adapter.setLongClickListener(View.OnLongClickListener {
            val popupMenu = PopupMenu(this@MainActivity, it)
            popupMenu.menuInflater.inflate(org.mozilla.check.n.share.R.menu.share_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share -> startActivity(Intent(Intent.ACTION_SEND).apply {
                        this.component = ComponentName(this@MainActivity, ShareProxyActivity::class.java)
                    })
                }
                true
            }
            popupMenu.setOnDismissListener {
                // 控件消失时的事件
            }
            popupMenu.show()

            true
        })


    }
}
