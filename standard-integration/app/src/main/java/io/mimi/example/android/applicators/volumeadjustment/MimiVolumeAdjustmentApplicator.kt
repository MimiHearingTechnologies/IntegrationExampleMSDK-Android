package io.mimi.example.android.applicators.volumeadjustment

import android.util.Log
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.controller.TestsController
import io.mimi.sdk.core.controller.tests.HeadphoneApplicatorConfiguration
import io.mimi.sdk.core.controller.tests.IsAbsoluteVolumeSupportedResponse
import io.mimi.sdk.core.controller.tests.SendHearingTestEndCommandResponse
import io.mimi.sdk.core.controller.tests.SendHearingTestStartCommandResponse

/**
 * This class is an example to the functions required for the [HeadphoneApplicatorConfiguration].
 *
 * They are responsible for applying the volume adjustment commands to the connected headphones.
 * They also need to notify the MSDK when the headphones send an HT_PAUSE notification event.
 */
class MimiVolumeAdjustmentApplicator(private val testsController: TestsController) {

    private val TAG: String = javaClass.simpleName

    companion object {
        // We're using a singleton for simplicity in this example. You should use a DI framework to inject your dependencies.
        val instance : MimiVolumeAdjustmentApplicator by lazy {
            MimiVolumeAdjustmentApplicator(MimiCore.testsController)
        }
    }

    suspend fun isAbsoluteVolumeSupported() : IsAbsoluteVolumeSupportedResponse {
        Log.d(TAG, "TODO() - Using Bluetooth query whether the headphones supports Bluetooth Absolute Volume and return the result")
        // TODO Read the result from the response payload and return it.
        val fakeIsAbsoluteVolumeSupported = true
        return IsAbsoluteVolumeSupportedResponse(isAbsoluteVolumeSupported = fakeIsAbsoluteVolumeSupported)
    }
    suspend fun sendHearingTestStartCommand() : SendHearingTestStartCommandResponse {
        Log.d(TAG, "TODO() - Using Bluetooth, send the hearing test start command.")
        // TODO Read the current headphone volume from the response payload and return it.
        val fakeResponseVolume = 63
        return SendHearingTestStartCommandResponse(fakeResponseVolume)
    }
    suspend fun sendHearingTestEndCommand() : SendHearingTestEndCommandResponse {
        Log.d(TAG, "TODO() - Using Bluetooth, send the hearing test end command.")
        // TODO Read the current headphone volume from the response payload and return it.
        val fakeResponseVolume = 13
        return SendHearingTestEndCommandResponse(fakeResponseVolume)
    }

    // TODO Your implementation needs to notify the MSDK when the headphones send an HT_PAUSE event.
    private fun onNotifyMimiHearingTestPausedByHeadphones() {
        testsController.connectedMimiHeadphone?.notificationReceiver?.notifyHearingTestPaused()
    }

}