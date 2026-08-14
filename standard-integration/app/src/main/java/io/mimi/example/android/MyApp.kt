package io.mimi.example.android

import android.app.Application
import android.content.Context
import io.mimi.debug.mock.MockProcessorDevice
import io.mimi.sdk.common.logging.MimiLog
import io.mimi.sdk.core.MimiConfiguration
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.MimiProfilePersonalizationConfiguration
import io.mimi.sdk.core.UiControlDebounceBehavior
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MyApp : Application() {

    /**
     * An in-memory stand-in for the connected automatic Mimi Processor, in place of real headphones.
     *
     * It is held in Application scope because it is stateful and represents a device which
     * outlives any single screen - in your integration, the equivalent would be your Bluetooth
     * connection to the headphones.
     */
    val mockAutomaticProcessorDevice = MockProcessorDevice()

    override fun onCreate() {
        super.onCreate()
        initializeMimiCore(this)
    }

    private fun initializeMimiCore(context: Context) {
        enableMimiSDKLogs()
        // Profile UI configuration to debounce Mimi Personalization controls for 500 ms
        val configuration = MimiConfiguration(
            mimiProfilePersonalizationConfiguration =
                MimiProfilePersonalizationConfiguration(
                    uiControlDebounceBehavior =
                        UiControlDebounceBehavior.Debounce(500.toDuration(DurationUnit.MILLISECONDS))
                )
        )
        // Initialize MimiCore so it's ready for use.
        MimiCore.start(
            context = context,
            configuration = configuration,
            clientId = BuildConfig.MY_CLIENT_ID,
            clientSecret = BuildConfig.MY_CLIENT_SECRET
        )
    }

    private fun enableMimiSDKLogs() {
        // Enable logging inside the MSDK (make sure you use your app's `BuildConfig` class!)
        MimiLog.isEnabled = BuildConfig.DEBUG
    }
}