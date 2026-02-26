package com.yoki.zarqaproduction.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AppAnalytics {

    private lateinit var analytics: FirebaseAnalytics
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
        isInitialized = true
    }

    fun logScreen(screenName: String, screenClass: String) {
        if (!isInitialized) return
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        })
    }

    fun logLoginAttempt(email: String) {
        if (!isInitialized) return
        val domain = email.substringAfter("@", missingDelimiterValue = "").take(64)
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString("status", "attempt")
            putString("email_domain", domain)
        })
    }

    fun logLoginSuccess(role: String, uid: String) {
        if (!isInitialized) return
        analytics.setUserId(uid.takeIf { it.isNotBlank() })
        analytics.setUserProperty("worker_role", role.ifBlank { "unknown" })
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString("status", "success")
            putString("role", role.ifBlank { "unknown" })
        })
    }

    fun logLoginFailure(errorType: String) {
        if (!isInitialized) return
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString("status", "failed")
            putString("error_type", errorType)
        })
    }

    fun logCustom(name: String, params: Bundle? = null) {
        if (!isInitialized) return
        analytics.logEvent(name, params)
    }
}
