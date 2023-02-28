package io.mimi.example.android.applicators

import io.mimi.example.android.MimiProcessingApplicator
import io.mimi.sdk.core.model.personalization.Personalization

internal class PresetApplicator(private val applicator: MimiProcessingApplicator) {

    fun canApply(preset: Personalization.PersonalizationPreset?): Boolean {
        // If preset is null, then go ahead and apply.
        return preset?.isValid ?: true
    }

    fun apply(preset: Personalization.PersonalizationPreset?) {
        applicator.setPreset(preset = preset)
    }
}