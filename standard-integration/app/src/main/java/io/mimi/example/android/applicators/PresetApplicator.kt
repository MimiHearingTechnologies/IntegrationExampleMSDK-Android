package io.mimi.example.android.applicators

import io.mimi.example.android.MimiProcessingApplicator
import io.mimi.sdk.core.controller.processing.MimiApplicatorResult
import io.mimi.sdk.core.model.personalization.Personalization

internal class PresetApplicator(private val applicator: MimiProcessingApplicator) {

    fun apply(preset: Personalization.PersonalizationPreset?) : MimiApplicatorResult {
        return applicator.setPreset(preset)
    }
}