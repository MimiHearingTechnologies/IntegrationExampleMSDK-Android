package io.mimi.example.android.applicators.processing.basic

import io.mimi.sdk.processing.MimiApplicatorResult

internal class IsEnabledApplicator(private val applicator: ExampleBasicProcessingApplicator) {

    fun apply(enabled: Boolean) : MimiApplicatorResult {
        return applicator.setEnabled(enabled)
    }

}
