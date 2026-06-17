package com.sap.codelab

import android.app.Application
import android.content.Context
import com.sap.codelab.notification.Notifier
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import javax.inject.Inject

@HiltAndroidApp
internal class App : Application() {

    @Inject
    lateinit var notifier: Notifier

    override fun onCreate() {
        super.onCreate()
        notifier.createChannel()
        Configuration.getInstance().apply {
            userAgentValue = packageName
            load(this@App, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        }
    }
}
