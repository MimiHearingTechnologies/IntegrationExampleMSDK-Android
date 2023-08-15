package io.mimi.example.android

import android.app.Application
import android.content.Context
import io.mimi.sdk.core.MimiConfiguration
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.model.headphones.MimiConnectedHeadphoneProvider
import io.mimi.sdk.core.model.headphones.MimiHeadphoneIdentifier

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeMimiCore(this)
    }

    private fun initializeMimiCore(context: Context) {
        enableMimiSDKLogs()
        MimiCore.start(
                context = context,
                configuration = MimiConfiguration(),
                clientId = BuildConfig.MY_CLIENT_ID,
                clientSecret = BuildConfig.MY_CLIENT_SECRET
        )
        // This is an optional step which is only applicable if you are using the Mimi SDK to
        // provide PTT hearing test functionality for known headphone models.
        setUpHeadphoneIdentifierProvider()
    }

    private fun setUpHeadphoneIdentifierProvider() {
        MimiCore.testsController.connectedHeadphoneProvider = MimiConnectedHeadphoneProvider {
            HeadphoneIdentifierStore.instance.headphoneModelId?.let { id ->
                MimiHeadphoneIdentifier(id)
            }
        }
    }

    private fun enableMimiSDKLogs() {
        io.mimi.sdk.core.util.Log.isEnabled = BuildConfig.DEBUG
    }
}