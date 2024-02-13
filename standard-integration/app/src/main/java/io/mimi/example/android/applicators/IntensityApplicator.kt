package io.mimi.example.android.applicators

import io.mimi.example.android.MimiProcessingApplicator
import io.mimi.sdk.core.controller.processing.MimiApplicatorResult

internal class IntensityApplicator(private val applicator: MimiProcessingApplicator) {

    /**
     * If you performed validation the values in the now removed `canApply`, you can now do it here.
     */
    fun apply(intensity: Double): MimiApplicatorResult {
        return if (intensity !in 0.0..1.0) {
            MimiApplicatorResult.Failure("Intensity must be between 0.0 and 1.0")
        } else {
            applicator.setIntensity(intensity)
        }
    }
}