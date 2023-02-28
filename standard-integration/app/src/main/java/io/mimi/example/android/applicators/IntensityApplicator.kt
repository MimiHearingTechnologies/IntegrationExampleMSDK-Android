package io.mimi.example.android.applicators

import io.mimi.example.android.MimiProcessingApplicator

internal class IntensityApplicator(private val applicator: MimiProcessingApplicator) {
    fun canApply(intensity: Double) = intensity in 0.0..1.0

    fun apply(intensity: Double) {
        applicator.setIntensity(intensity)
    }
}