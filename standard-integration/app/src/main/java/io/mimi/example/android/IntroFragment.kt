package io.mimi.example.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import io.mimi.example.android.applicators.volumeadjustment.MimiVolumeAdjustmentApplicator
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.controller.tests.HeadphoneApplicatorConfiguration
import io.mimi.sdk.core.model.MimiAuthRoute
import io.mimi.sdk.core.model.headphones.MimiHeadphoneIdentifier
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
            setupMimiProfileLauncherUi()
            setupMimiConnectedHeadphoneUi()
            setupSubmitAudiogramUi()
        }
    }

    // region Mimi Profile

    /*
     * Opens the Mimi Profile UI.
     */

    private fun View.setupMimiProfileLauncherUi() {
        findViewById<Button>(R.id.launchButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<MimiProfileFragment>(R.id.mimiContainerFragment)
                addToBackStack("main")
            }
        }
    }

    // endregion

    // region Mimi Connected Headphones

    /*
     * NOTE: ONLY NECESSARY FOR PARTNERS INTEGRATING THEIR HEADPHONES TO SUPPORT PTT HEARING TESTS.
     *
     * Volume Adjustment Sequence
     *
     * The [connectedHeadphoneSwitch] simulates the actions that your app should take when receiving
     * changes in Bluetooth headphone connectivity.
     *
     * When headphones are connected or disconnected, your app should notify the MSDK.
     *
     * When the TestFlow is launched, it will read from [MimiCore.testsController.connectedMimiHeadphone]
     * to determine:
     *  - Which test types (paradigms) are available and,
     *  - How to interact with the headphones to ensure a consistent volume during the hearing test.
     *
     * In this example, the headphone model identifier is hardcoded, however you app should ensure
     * that it reflects the currently connected headphone model.
     */

    private fun View.setupMimiConnectedHeadphoneUi() {
        val connectedHeadphoneSwitch = findViewById<SwitchCompat>(R.id.mimiHeadphoneConnected)
        updateConnectedMimiHeadphoneButtons(connectedHeadphoneSwitch)
        connectedHeadphoneSwitch.setOnClickListener {

            val headphoneAreConnected = it.isSelected

            if (headphoneAreConnected) {
                val mimiHeadphoneIdentifier =
                    MimiHeadphoneIdentifier(getConnectedHeadphoneModelIdentifier())
                // Notify the MSDK of the newly connected headphones and supply the
                // applicator configuration to facilitate the PTT automatic volume adjustment
                // sequence.
                MimiCore.testsController.notifyMimiHeadphoneConnected(
                    mimiHeadphoneIdentifier,
                    HeadphoneApplicatorConfiguration(
                        MimiVolumeAdjustmentApplicator.instance::isAbsoluteVolumeSupported,
                        MimiVolumeAdjustmentApplicator.instance::sendHearingTestStartCommand,
                        MimiVolumeAdjustmentApplicator.instance::sendHearingTestEndCommand
                    )
                )
            } else {
                // Notify the MSDK that the headphones have been disconnected.
                MimiCore.testsController.notifyMimiHeadphoneDisconnected()
            }
        }
    }

    /*
     * This is a simulated function: your application should return the identifier associated
     * with your currently connected headphone device.
     */
    private fun getConnectedHeadphoneModelIdentifier() = "mimi:showcase_headphone"

    private fun updateConnectedMimiHeadphoneButtons(connectedHeadphoneSwitch: SwitchCompat) {
        val hasConnectedMimiHeadphone = MimiCore.testsController.connectedMimiHeadphone != null
        connectedHeadphoneSwitch.isSelected = !hasConnectedMimiHeadphone
    }

    // endregion

    // region Audiogram

    /*
     * NOTE: ONLY NECESSARY FOR PARTNERS USING EXTERNAL AUDIOGRAM DATA.
     *
     * This functionality cam be used to onboard users with existing audiograms, rather than
     * through the Mimi SDK's hearing test or hearing estimation features.
     */

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

    // endregion
}