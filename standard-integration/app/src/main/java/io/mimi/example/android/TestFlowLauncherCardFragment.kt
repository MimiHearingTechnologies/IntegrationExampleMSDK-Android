package io.mimi.example.android

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import io.mimi.example.android.applicators.volumeadjustment.MimiVolumeAdjustmentApplicator
import io.mimi.sdk.common.annotations.MsdkInternalApi
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.controller.tests.HeadphoneApplicatorConfiguration
import io.mimi.sdk.core.model.headphones.MimiHeadphoneIdentifier
import io.mimi.sdk.testflow.activity.TestFlowActivity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Note: You need an authenticated user to launch the [TestFlowActivity].
 *
 * Some applications don't use the Mimi Profile [io.mimi.sdk.profile.MimiProfileFragment], rather they
 * launch their own instance of the [TestFlowActivity]. This is appropriate for app which focus on providing
 * Hearing Tests, rather than Sound Personalization.
 *
 * This example, directly launches the [io.mimi.sdk.testflow.activity.TestFlowActivity] and displays the result data
 * returned from it.
 *
 * The result data is a JSON string, whose format matches the [io.mimi.sdk.testflow.flowfactory.TestFlowResponse] class.
 *
 * You could use the [io.mimi.sdk.core.moshi] object to deserialize it.
 *
 * Note: This is uses the deprecated Android technique for launching an Activity for a result.
 *
 * This Fragment also shows how to inform the MSDK of the currently connected headphone model before
 * launching [TestFlowActivity] to improve PTT Hearing Test accuracy.
 */
class TestFlowLauncherCardFragment : Fragment(R.layout.fragment_test_flow_launcher_card) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            setupLaunchTestFlowUi()
            setupMimiConnectedHeadphoneUi()
        }
    }

    // region ExampleTestFlow launcher for apps not using Mimi Profile

    private fun View.setupLaunchTestFlowUi() {
        findViewById<Button>(R.id.launchTestFlow).setOnClickListener {
            // Launch a standalone TestFlow
            launchTestFlowForResult()
        }
    }

    @OptIn(MsdkInternalApi::class)
    private fun launchTestFlowForResult() {
        val intent = TestFlowActivity.intent(requireActivity())
        startActivityForResult(intent, 10)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val resultJson = data?.getStringExtra(TestFlowActivity.EXTRA_HEARING_TEST_RESULTS)

        val resultsTextView = view?.findViewById<TextView>(R.id.testFlowResults)
        resultsTextView?.apply {
            text = resultJson?.let { prettyPrintJson(it) } ?: "Invalid or no result"
        }
    }

    fun prettyPrintJson(json: String, indent: Int = 2): String {
        return try {
            when {
                json.trim().startsWith("{") -> JSONObject(json).toString(indent)
                json.trim().startsWith("[") -> JSONArray(json).toString(indent)
                else -> json
            }
        } catch (e: Exception) {
            json // Return original if invalid
        }
    }

    // endregion

    // region Mimi Connected Headphones

    /**
     * NOTE: ONLY NECESSARY FOR PARTNERS INTEGRATING THEIR HEADPHONES TO SUPPORT PTT HEARING TESTS.
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
     * In this example, the headphone model identifier is hardcoded. Your app should ensure
     * that it uses the currently connected headphone model.
     *
     * These APIs are suitable when using the Mimi Profile, or when launching the Test Flow directly.
     */

    private fun View.setupMimiConnectedHeadphoneUi() {
        val connectedHeadphoneSwitch = findViewById<SwitchCompat>(R.id.mimiHeadphoneConnected)
        updateConnectedMimiHeadphoneButtons(connectedHeadphoneSwitch)
        connectedHeadphoneSwitch.setOnCheckedChangeListener { _, isChecked ->

            val shouldConnectHeadphones = isChecked

            if (shouldConnectHeadphones) {
                // The headphoneIdentifier is used internally by Mimi to account for the
                // characteristics of the headphone model when processing the hearing test.
                // The value must match that previously agreed with Mimi.
                //
                // If the headphone identifier is recognized, then the Test Flow will skip the
                // user headphone selection step.
                val mimiHeadphoneIdentifier =
                    MimiHeadphoneIdentifier(getConnectedHeadphoneModelIdentifier())

                // Notify the MSDK of the newly connected headphones and supply the
                // applicator configuration to facilitate the PTT automatic volume adjustment
                // sequence. This only needs to be provided for headphones whose
                // firmware supports the Mimi PTT automatic volume adjustment sequence, otherwise
                // it should be left as null.
                MimiCore.testsController.notifyMimiHeadphoneConnected(
                    headphoneIdentifier = mimiHeadphoneIdentifier,
                    applicatorConfiguration = HeadphoneApplicatorConfiguration(
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
        connectedHeadphoneSwitch.isChecked = hasConnectedMimiHeadphone
    }

    // endregion

}