package io.mimi.example.android.applicators.processing

import io.mimi.sdk.processing.MimiApplicatorResult
import io.mimi.sdk.processing.model.Personalization

internal class PresetApplicator(private val applicator: MimiProcessingApplicator) {

    fun apply(preset: Personalization.PersonalizationPreset?) : MimiApplicatorResult {
        return applicator.setPreset(preset)
    }
}