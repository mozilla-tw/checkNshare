/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.check.n.share.telemetry

import android.content.Context
import android.os.Bundle
import androidx.annotation.CheckResult
import com.google.firebase.analytics.FirebaseAnalytics
import org.mozilla.check.n.share.BuildConfig
import java.util.Objects

internal class FirebaseEvent private constructor(private val eventName: String) {

    private var eventParam: Bundle? = null

    init {
        if (this.eventName.length > MAX_LENGTH_EVENT_NAME) {
            throw IllegalArgumentException("Event[" + this.eventName + "] exceeds Firebase event name limit " + this.eventName.length + " of " + MAX_LENGTH_EVENT_NAME)
        }
    }

    fun param(name: String, value: String): FirebaseEvent {
        if (this.eventParam == null) {
            this.eventParam = Bundle()
        }
        // validate the size
        if (this.eventParam!!.size() >= MAX_PARAM_SIZE) {
            if (BuildConfig.DEBUG) {
                throw IllegalArgumentException("Firebase event[$eventName] has too many parameters")
            }
        }

        this.eventParam!!.putString(
            safeParamLength(name, MAX_LENGTH_PARAM_NAME),
            safeParamLength(value, MAX_LENGTH_PARAM_VALUE)
        )

        return this
    }

    /*** Queue the events and let Firebase Analytics to decide when to upload to server
     *
     * @param context used for FirebaseAnalytics.getInstance() call.
     */
    fun event(context: Context?) {
        if (context == null) {
            return
        }

        FirebaseAnalytics.getInstance(context).logEvent(this.eventName, this.eventParam)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other !is FirebaseEvent) {
            return false
        }
        val event = other as FirebaseEvent?
        return this.eventName == event?.eventName && equalBundles(this.eventParam, event.eventParam)
    }

    private fun equalBundles(a: Bundle?, b: Bundle?): Boolean {
        if (a == b) {
            return true
        }
        if (a == null) {
            return false
        }
        if (a.size() != b!!.size()) {
            return false
        }

        if (!a.keySet().containsAll(b.keySet())) {
            return false
        }

        for (key in a.keySet()) {
            val valueOne = a.get(key) as String
            val valueTwo = b.get(key) as String
            if (valueOne != valueTwo) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(eventName, eventParam)
    }

    companion object {

        // limitation for event:
        // Event names can be up to 40 characters long, may only contain alphanumeric characters and
        // underscores ("_"), and must start with an alphabetic character. The "firebase_", "google_"
        // and "ga_" prefixes are reserved and should not be used.
        // see: https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event
        private const val MAX_LENGTH_EVENT_NAME = 40

        // limitation to param
        // You can associate up to 25 unique Params with each Event type. Param names can be up to 40
        // characters long, may only contain alphanumeric characters and underscores ("_"),
        // and must start with an alphabetic character. Param values can be up to 100 characters long.
        // The "firebase_", "google_" and "ga_" prefixes are reserved and should not be used.
        // see: https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Param
        private const val MAX_PARAM_SIZE = 25
        private const val MAX_LENGTH_PARAM_NAME = 40
        private const val MAX_LENGTH_PARAM_VALUE = 100

        @CheckResult
        fun create(category: String): FirebaseEvent {
            return FirebaseEvent(category)
        }

        // TODO: check param name should start with[a-zA-Z] , contains only [a-zA-A0-9_], and shouldn't
        // TODO: start with ^(?!(firebase_|google_|ga_)).*
        //  FIXME: ^(?!ga_|google_|firebase_)[A-Za-z][a-zA-Z0-9_]*$
        private fun safeParamLength(str: String, end: Int): String {
            // validate the length
            if (str.length > end) {
                //Logger.throwOrWarn(TAG, "Exceeding limit of param content length:" + str.length() + " of " + end);
            }
            // fix the value if we just want to warn
            return str.substring(0, Math.min(end, str.length))
        }
    }
}