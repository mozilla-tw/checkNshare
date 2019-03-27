package org.mozilla.check.n.share.activity

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.widget.WhyAdapter

class WhyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_why)
        setTitle("為什麼含有爭議內容？")
        setSupportActionBar(findViewById(R.id.activity_my_toolbar))
        val adapter = WhyAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        val liveShareEntities = (application as MainApplication).database.shareDao().getShare(intent.extras.getLong(
            ShareEntity.KEY_ID))
        liveShareEntities.observe(this, Observer { result ->
            if (result != null) {
                adapter.initResult(result)
            }
        })
    }
}