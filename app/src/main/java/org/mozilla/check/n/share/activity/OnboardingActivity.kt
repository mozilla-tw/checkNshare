package org.mozilla.check.n.share.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.check.n.share.R
import org.mozilla.check.n.share.navigation.IntentBuilder
import org.mozilla.check.n.share.telemetry.TelemetryWrapper

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_notification)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            TelemetryWrapper.queue(TelemetryWrapper.Category.CATCH_NOTI_INTRO_PAGE_TAP_ENABLE)
            startActivity(IntentBuilder.doOnboarding2(this))
            finish()
        }
        val text = findViewById<TextView>(R.id.button2)
        text.setOnClickListener {
            TelemetryWrapper.queue(TelemetryWrapper.Category.CATCH_NOTI_INTRO_PAGE_TAP_NO_TKS)
            finish()
        }
    }
}