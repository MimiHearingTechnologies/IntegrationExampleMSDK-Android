package io.mimi.example.android.audiogram

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.mimi.example.android.R
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.model.MimiAuthRoute
import io.mimi.sdk.core.model.tests.MimiTestAudiogram
import io.mimi.sdk.core.model.tests.TestAudiogramMetadata
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Example of how to onboard a user via external audiogram data.
 *
 * This makes a remote call to the Mimi backend, and creates a [io.mimi.sdk.core.model.tests.MimiTestResult].
 *
 * The [io.mimi.sdk.core.MimiCore.testsController.latestTestResults] is automatically updated.
 *
 * This functionality can be used to onboard users with existing audiograms from external sources,
 * rather than through the Mimi SDK's hearing test or hearing estimation features.
 */
class SubmitAudiogramCardFragment : Fragment(R.layout.fragment_submit_audiogram_card) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setupSubmitAudiogramUi()
    }

    // region Audiogram

    private fun View.setupSubmitAudiogramUi() {
        findViewById<Button>(R.id.submitAudiogram).setOnClickListener {
            // Send the custom audiogram to the Mimi SDK
            submitAudiogram()
        }
    }

    private fun submitAudiogram() = lifecycleScope.launch {
        // Check if there is a user logged in
        if (MimiCore.userController.mimiUser.state.value == null) {
            MimiCore.userController.authenticate(MimiAuthRoute.Anonymously)
        }

        // Submit the custom external audiogram (example data).
        try {
            val response = MimiCore.testsController.submitAudiogram(

                leftEar = MimiTestAudiogram(
                    listOf(
                        MimiTestAudiogram.DataPoint(frequency = 250, threshold = 17.8),
                        MimiTestAudiogram.DataPoint(frequency = 500, threshold = 18.8),
                        MimiTestAudiogram.DataPoint(frequency = 1000, threshold = 22.7),
                        MimiTestAudiogram.DataPoint(frequency = 2000, threshold = 27.6),
                        MimiTestAudiogram.DataPoint(frequency = 4000, threshold = 29.4),
                        MimiTestAudiogram.DataPoint(frequency = 8000, threshold = 16.0)
                    )
                ),
                rightEar = MimiTestAudiogram(
                    listOf(
                        MimiTestAudiogram.DataPoint(frequency = 250, threshold = 13.3),
                        MimiTestAudiogram.DataPoint(frequency = 500, threshold = 17.3),
                        MimiTestAudiogram.DataPoint(frequency = 1000, threshold = 21.9),
                        MimiTestAudiogram.DataPoint(frequency = 2000, threshold = 28.9),
                        MimiTestAudiogram.DataPoint(frequency = 4000, threshold = 28.8),
                        MimiTestAudiogram.DataPoint(frequency = 8000, threshold = 17.2)
                    )
                ),
                metadata = TestAudiogramMetadata(timestamp = Date(System.currentTimeMillis()))
            )
            Toast.makeText(
                requireActivity(),
                "Submitted audiogram. Test Result id: ${response.id}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireActivity(),
                "Submit audiogram failed: $e",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // endregion

}