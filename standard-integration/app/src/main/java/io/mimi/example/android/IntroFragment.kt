package io.mimi.example.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.model.MimiAuthRoute
import io.mimi.sdk.core.model.tests.MimiTestAudiogram
import io.mimi.sdk.core.model.tests.TestAudiogramMetadata
import io.mimi.sdk.profile.MimiProfileFragment
import kotlinx.coroutines.launch
import java.util.Date

class IntroFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            findViewById<Button>(R.id.launchButton).setOnClickListener {
                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<MimiProfileFragment>(R.id.mimiContainerFragment)
                    addToBackStack("main")
                }
            }

            findViewById<Button>(R.id.submitAudiogram).setOnClickListener {
                // Send the custom audiogram to the Mimi SDK
                submitAudiogram()
            }
        }
    }

    private fun submitAudiogram() = lifecycleScope.launch {
        // Check if there is a user logged in
        if (MimiCore.userController.mimiUser.state.value == null) {
            MimiCore.userController.authenticate(MimiAuthRoute.Anonymously)
        }

        // Submit the custom audiogram
        MimiCore.testsController.submitAudiogram(
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
    }


}