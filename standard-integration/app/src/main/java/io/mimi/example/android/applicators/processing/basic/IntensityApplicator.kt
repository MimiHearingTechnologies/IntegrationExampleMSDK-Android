package io.mimi.example.android.applicators.processing.basic

import io.mimi.sdk.processing.MimiApplicatorResult

internal class IntensityApplicator(private val applicator: ExampleBasicProcessingApplicator) {

    /**
     * If you previosly performed validation the values in the now removed `canApply`, you should
     * now do it here.
     */
    fun apply(intensity: Double): MimiApplicatorResult {
        return if (intensity !in 0.0..1.0) {
            MimiApplicatorResult.Failure("Intensity must be between 0.0 and 1.0")
        } else {
            applicator.setIntensity(intensity)
        }
    }
}