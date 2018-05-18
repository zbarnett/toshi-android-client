package com.toshi.util

import com.amplitude.api.Amplitude
import com.toshi.util.logging.LogUtil
import org.json.JSONException
import org.json.JSONObject

class Analytics {
    companion object {
        const val SIGN_IN_TAPPED = "sign_in_tapped"

        fun trackEvent(event: String, properties: HashMap<String, String>? = null) {
            if (properties == null) {
                Amplitude.getInstance().logEvent(event)
                return
            }

            val eventProperties = JSONObject()
            try {
                for ((key, value) in properties) {
                    eventProperties.put(key, value)
                }
            } catch (ex: JSONException) {
                LogUtil.exception("Error while accessing event properties", ex)
            }

            Amplitude.getInstance().logEvent(event, eventProperties)
        }
    }
}