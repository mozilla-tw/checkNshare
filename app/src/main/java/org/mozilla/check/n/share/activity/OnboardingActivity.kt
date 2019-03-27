package org.mozilla.check.n.share.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.check.n.share.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_notification)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { finish() }
    }
}