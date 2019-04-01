package org.mozilla.check.n.share.telemetry

import android.content.Context

class TelemetryWrapper {

    object Category {
        const val MAIN_PAGE_TAP_CHECK = "main_page_tap_check"
        const val MAIN_PAGE_TAP_HISTORY_MISINFO = "main_page_tap_history_misinfo"
        const val MAIN_PAGE_TAP_HISTORY_TRUE = "main_page_tap_history_true"
        const val MAIN_PAGE_TAP_HISTORY_NEUTRAL = "main_page_tap_history_neutral"
        const val SHOW_TRUE_PROMPT = "show_true_prompt"
        const val TRUE_PROMPT_TAP_SHARE = "true_prompt_tap_share"
        const val SHOW_MISINFO_PROMPT = "show_misinfo_prompt"
        const val MISINFO_PROMPT_TAP_KNOW_MORE = "misinfo_prompt_tap_know_more"
        const val TEXT_SELECTION_PAGE_TAP_GENERATE_PHOTO = "text_selection_page_tap_generate_photo"
        const val TEXT_SELECTION_PAGE_TAP_BACK = "text_selection_page_tap_back"
        const val PHOTO_PREVIEW_TAP_BACK = "photo_preview_tap_back"
        const val PHOTO_PREVIEW_TAP_SHARE_PHOTO = "photo_preview_tap_share_photo"
        const val NO_SELECT_TEXT_ALERT_TAP_AUTO_SELECT = "no_select_text_alert_tap_auto_select"
        const val NO_SELECT_TEXT_ALERT_TAP_MANUAL_SELECT = "no_select_text_alert_tap_manual_select"
        const val SHOW_NO_SELECT_TEXT_ALERT = "show_no_select_text_alert"
        const val CATCH_NOTI_INTRO_PAGE_TAP_ENABLE = "catch_noti_intro_page_tap_enable"
        const val CATCH_NOTI_INTRO_PAGE_TAP_NO_TKS = "catch_noti_intro_page_tap_no_tks"
        const val CATCH_NOTI_ONBOARDING_TAP_GO_SETTINGS = "catch_noti_onboarding_tap_go_settings"
        const val CATCH_NOTI_ONBOARDING_TAP_CANCEL = "catch_noti_onboarding_tap_cancel"
        const val MISINFO_DETAIL_PAGE_TAP_BACK = "misinfo_detail_page_tap_back"
        const val MISINFO_DETAIL_PAGE_TAP_SHARE = "misinfo_detail_page_tap_share"
        const val SHOW_MISINFO_NOTIFICATION = "show_misinfo_notification"
        const val BACKGROUND_CHECK_COPIED_TEXT = "backgroundcheck_copied_text"
        const val TAP_MISINFO_NOTIFICATION = "tap_misinfo_notification"
        const val SHOW_NO_RESULT_PROMPT = "show_no_result_prompt"
        const val NO_RESULT_PROMPT_TAP_SHARE = "no_result_prompt_tap_share"
        const val HANDLE_QUERY = "handle_query"
        const val MENU_TOGGLE_COPY_TEXT = "menu_toggle_copy_text"
        const val CATCH_NOTI_ITEM = "catch_noti_item"
    }

    object ExtraKey {
        const val SEARCH_VALUE = "search_value"
        const val COPY_VALUE = "copy_value"
        const val CHANGE_TO = "change_to"
    }

    companion object {

        private var context: Context? = null

        fun init(context: Context) {
            this.context = context.applicationContext
        }

        fun queue(string: String) {
            EventBuilder(string).queue()
        }

        fun queue(string: String, extraKey: String, extraValue: String) {
            EventBuilder(string).extra(extraKey, extraValue).queue()
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
