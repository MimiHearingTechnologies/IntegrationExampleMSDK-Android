package io.mimi.example.android.applicators.processing

import io.mimi.sdk.core.controller.processing.MimiApplicatorResult

internal class IsEnabledApplicator(private val applicator: MimiProcessingApplicator) {

    fun apply(enabled: Boolean) : MimiApplicatorResult {
        return applicator.setEnabled(enabled)
    }

}
