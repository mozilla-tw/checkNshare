package org.mozilla.check.n.share.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.check.n.share.R

class WhyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_why)
        setTitle("為什麼含有爭議內容？")
        setSupportActionBar(findViewById(R.id.activity_my_toolbar))
    }
}