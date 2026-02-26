package com.yoki.zarqaproduction

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.yoki.zarqaproduction.BuildConfig
import timber.log.Timber

class ZarqaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            ZarqaMessagingService.CHANNEL_BATCH,
            "Batch Produksi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi batch produksi masuk"
            enableVibration(true)
        }

        manager.createNotificationChannel(channel)
    }
}
