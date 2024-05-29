package io.mimi.example.android.applicators.volumeadjustment

import android.util.Log
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.controller.TestsController
import io.mimi.sdk.core.controller.tests.IsAbsoluteVolumeSupportedResponse
import io.mimi.sdk.core.controller.tests.SendHearingTestEndCommandResponse
import io.mimi.sdk.core.controller.tests.SendHearingTestStartCommandResponse

class MimiVolumeAdjustmentApplicator(private val testsController: TestsController) {

    companion object {
        val instance : MimiVolumeAdjustmentApplicator by lazy {
            MimiVolumeAdjustmentApplicator(MimiCore.testsController)
        }
    }

    private val TAG: String = this.javaClass.simpleName

    public suspend fun isAbsoluteVolumeSupported() : IsAbsoluteVolumeSupportedResponse {
        Log.d(TAG, "TODO() - Using Bluetooth query whether the headphones supports Bluetooth Absolute Volume and return the result")
        // TODO Read the result from the response payload and return it.
        val fakeIsAbsoluteVolumeSupported = true
        return IsAbsoluteVolumeSupportedResponse(isAbsoluteVolumeSupported = fakeIsAbsoluteVolumeSupported)
    }
    public suspend fun onSendHearingTestStartCommand() : SendHearingTestStartCommandResponse {
        Log.d(TAG, "TODO() - Using Bluetooth, send the hearing test start command.")
        // TODO Read the current headphone volume from the response payload and return it.
        val fakeResponseVolume = 63
        return SendHearingTestStartCommandResponse(fakeResponseVolume)
    }
    public suspend fun onSendHearingTestEndCommand() : SendHearingTestEndCommandResponse {
        Log.d(TAG, "TODO() - Using Bluetooth, send the hearing test end command.")
        // TODO Read the current headphone volume from the response payload and return it.
        val fakeResponseVolume = 13
        return SendHearingTestEndCommandResponse(fakeResponseVolume)
    }

    // Your implementation needs to notify the MSDK when the headphones send an HT_PAUSE event.
    private fun onNotifyMimiHearingTestPausedByHeadphones() {
        testsController.connectedMimiHeadphone?.notificationReceiver?.notifyHearingTestPaused()
    }

}