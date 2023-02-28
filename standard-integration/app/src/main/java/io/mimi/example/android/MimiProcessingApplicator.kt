package io.mimi.example.android

import android.util.Log
import io.mimi.sdk.core.model.personalization.Personalization

class MimiProcessingApplicator {

    private val TAG: String = this.javaClass.simpleName

    fun setPreset(preset: Personalization.PersonalizationPreset?) {
        if (preset == null) {
            clearMimiCommand()
        } else {
            Log.d(TAG, "Send the values to the external device via bluetooth $preset")
        }
    }

    fun setIntensity(intensity: Double) {
        Log.d(TAG, "Send the values to the external device via bluetooth $intensity")
    }

    fun setEnabled(enabled: Boolean) {
        Log.d(TAG, "Send the values to the external device via bluetooth $enabled")
    }

    private fun clearMimiCommand() {
        Log.d(TAG, "Send Clear Command to external device via bluetooth")
    }
}