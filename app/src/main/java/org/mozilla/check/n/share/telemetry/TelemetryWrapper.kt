package org.mozilla.check.n.share.telemetry

import android.content.Context

class TelemetryWrapper {

    private object Category {
        const val SHARE_FACT = "Share_fact"
    }

    companion object {

        private var context: Context? = null

        fun init(context: Context) {
            this.context = context.applicationContext
        }

        fun shareFact() {
            EventBuilder(Category.SHARE_FACT).queue()
        }
    }

    internal class EventBuilder constructor(name: String) {
        var firebaseEvent: FirebaseEvent = FirebaseEvent.create(name)


        fun extra(key: String, value: String): EventBuilder {
            firebaseEvent.param(key, value)
            return this
        }

        fun queue() {
            firebaseEvent.event(context)
        }
    }

}
