package org.mozilla.check.n.share.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.check.n.share.R

class OnboardingActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_notification)
        val button = findViewById<Button>(R.id.button)
        findViewById<TextView>(R.id.textView2).text = "為了讓「訊息自動查證功能」正常運作，請開啟「享先查」的通知存取權限。"
        findViewById<ImageView>(R.id.imageView2).setImageResource(R.drawable.illustration2)
        findViewById<Button>(R.id.button).text = "前往設定"
        findViewById<TextView>(R.id.textView3).text = "取消"

        button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            finish()
        }
        val text = findViewById<TextView>(R.id.button2)
        text.setOnClickListener {
            finish()
        }
    }
}