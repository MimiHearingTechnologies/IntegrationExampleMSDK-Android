package io.mimi.example.android.applicators.processing

import android.util.Log
import io.mimi.sdk.processing.MimiApplicatorResult
import io.mimi.sdk.processing.model.Personalization

class MimiProcessingApplicator {

    private val TAG: String = this.javaClass.simpleName

    fun setPreset(preset: Personalization.PersonalizationPreset?) : MimiApplicatorResult {
        return if (preset == null) {
            sendClearMimiCommand()
        } else {
            sendSetMimiPresetCommand(preset)
        }
    }

    fun setIntensity(intensity: Double) : MimiApplicatorResult {
        Log.d(TAG, "TODO() - Send the intensity value to the external device via Bluetooth: $intensity")
        return MimiApplicatorResult.Success
    }

    fun setEnabled(enabled: Boolean) : MimiApplicatorResult {
        Log.d(TAG, "TODO() - Send the Enable/Disable value to the external device via Bluetooth: $enabled")
        return MimiApplicatorResult.Success
    }

    private fun sendClearMimiCommand() : MimiApplicatorResult {
        Log.d(TAG, "TODO() - Send Clear Command to external device via Bluetooth")
        return MimiApplicatorResult.Success
    }

    private fun sendSetMimiPresetCommand(preset: Personalization.PersonalizationPreset?) : MimiApplicatorResult {
        Log.d(TAG, "TODO() - Send the preset to the external device via Bluetooth $preset")
        return MimiApplicatorResult.Success
    }
}
