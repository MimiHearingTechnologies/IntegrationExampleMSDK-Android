package io.mimi.example.android

import android.app.Application
import android.content.Context
import io.mimi.sdk.core.MimiConfiguration
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.MimiProfilePersonalizationConfiguration
import io.mimi.sdk.core.UiControlDebounceBehavior
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeMimiCore(this)
    }

    private fun initializeMimiCore(context: Context) {
        enableMimiSDKLogs()
        // Profile config to debounce Mimi Personalization controls for 500 ms
        val configuration = MimiConfiguration(
            mimiProfilePersonalizationConfiguration =
            MimiProfilePersonalizationConfiguration(
                uiControlDebounceBehavior =
                UiControlDebounceBehavior.Debounce(500.toDuration(DurationUnit.MILLISECONDS))
            )
        )
        MimiCore.start(
            context = context,
            configuration = configuration,
            clientId = BuildConfig.MY_CLIENT_ID,
            clientSecret = BuildConfig.MY_CLIENT_SECRET
        )
    }

    private fun enableMimiSDKLogs() {
        io.mimi.sdk.core.util.Log.isEnabled = BuildConfig.DEBUG
    }
}