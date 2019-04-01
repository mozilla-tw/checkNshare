package org.mozilla.check.n.share.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.navigation.IntentBuilder

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_notification)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            startActivity(IntentBuilder.doOnboarding2(this))
            finish()
        }
        val text = findViewById<TextView>(R.id.button2)
        text.setOnClickListener {
            finish()
        }
    }
}