package io.mimi.example.android

import android.app.Application
import android.content.Context
import io.mimi.sdk.core.MimiConfiguration
import io.mimi.sdk.core.MimiCore

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeMimiCore(this)
    }

    private fun enableMimiSDKLogs() {
        io.mimi.sdk.core.util.Log::class.java.getDeclaredField("enabled")
                .apply {
                    isAccessible = true
                    setBoolean(io.mimi.sdk.core.util.Log(), true)
                }
    }

    private fun initializeMimiCore(context: Context) {
        // Uncomment to enable debug logs for MSDK
        // enableMimiSDKLogs()
        MimiCore.start(
                context = context,
                configuration = MimiConfiguration(),
                clientId = BuildConfig.MY_CLIENT_ID,
                clientSecret = BuildConfig.MY_CLIENT_SECRET
        )
    }
}