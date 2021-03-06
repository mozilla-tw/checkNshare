package org.mozilla.check.n.share.activity

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Switch
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.entry.ClipService
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.telemetry.TelemetryWrapper
import org.mozilla.check.n.share.widget.ShareAdapter


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var cm: ClipboardManager
    private lateinit var checkInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val check = findViewById<View>(R.id.check)
        checkInput = findViewById(R.id.check_input)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.title = ""
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_hamburger_menu)
        }
        cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val menu = navigationView.menu
        val switch = menu.findItem(R.id.check_copy).actionView.findViewById<Switch>(R.id.switchitem)
        switch.isChecked = MainApplication.clipServiceEnabled(this)
        switch.setOnCheckedChangeListener { _, isChecked ->
            MainApplication.setClipServiceEnabled(this, isChecked)
            TelemetryWrapper.queue(TelemetryWrapper.Category.MENU_TOGGLE_COPY_TEXT, TelemetryWrapper.ExtraKey.CHANGE_TO, isChecked.toString())
            if (isChecked) {
                val intent = Intent()
                intent.component = ComponentName(this, ClipService::class.java)
                startService(intent)
            } else {
                val intent = Intent()
                intent.component = ComponentName(this, ClipService::class.java)
                stopService(intent)
            }
        }

        check.setOnClickListener {
            TelemetryWrapper.queue(TelemetryWrapper.Category.MAIN_PAGE_TAP_CHECK)
            startActivity(IntentBuilder.checkString(this@MainActivity, checkInput.text.toString()))
        }

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
                    ShareEntity.RESPONSE_TRUE -> {
                        TelemetryWrapper.queue(TelemetryWrapper.Category.MAIN_PAGE_TAP_HISTORY_TRUE)
                        startActivity(IntentBuilder.doShare(this@MainActivity, shareEntity.id))
                    }
                    ShareEntity.RESPONSE_FALSE -> {
                        TelemetryWrapper.queue(TelemetryWrapper.Category.MAIN_PAGE_TAP_HISTORY_MISINFO)
                        startActivity(IntentBuilder.askWhy(this@MainActivity, shareEntity.id))
                    }
                    ShareEntity.RESPONSE_NEUTRAL -> {
                        TelemetryWrapper.queue(TelemetryWrapper.Category.MAIN_PAGE_TAP_HISTORY_NEUTRAL)
                        startActivity(IntentBuilder.doShare(this@MainActivity, shareEntity.id))
                    }
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
    }

    override fun onStart() {
        super.onStart()
        checkInput.setText(cm.primaryClip?.getItemAt(0)?.text)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.check_notification -> {
                TelemetryWrapper.queue(TelemetryWrapper.Category.CATCH_NOTI_ITEM)
                startActivity(IntentBuilder.doOnboarding(this@MainActivity))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
