package io.mimi.example.android.applicators.processing.basic

import io.mimi.sdk.processing.MimiApplicatorResult
import io.mimi.sdk.processing.model.Personalization

internal class PresetApplicator(private val applicator: ExampleBasicProcessingApplicator) {

    fun apply(preset: Personalization.PersonalizationPreset?) : MimiApplicatorResult {
        return applicator.setPreset(preset)
    }
}