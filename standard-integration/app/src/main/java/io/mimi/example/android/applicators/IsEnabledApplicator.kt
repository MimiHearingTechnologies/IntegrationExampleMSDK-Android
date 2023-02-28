package io.mimi.example.android.applicators

import io.mimi.example.android.MimiProcessingApplicator

internal class IsEnabledApplicator(private val applicator: MimiProcessingApplicator) {
    fun canApply() = true

    fun apply(enabled: Boolean) = applicator.setEnabled(enabled)
}
