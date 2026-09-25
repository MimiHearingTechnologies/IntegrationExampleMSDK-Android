package io.mimi.example.android.hearingtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import io.mimi.example.android.R
import io.mimi.example.android.applicators.volumeadjustment.MimiVolumeAdjustmentApplicator
import io.mimi.sdk.common.annotations.MsdkInternalApi
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.controller.tests.HeadphoneApplicatorConfiguration
import io.mimi.sdk.core.model.headphones.MimiHeadphoneIdentifier
import io.mimi.sdk.core.moshi
import io.mimi.sdk.testflow.activity.TestFlowActivity
import io.mimi.sdk.testflow.flowfactory.TestFlowResponse

/**
 * Note: You need an authenticated user to launch the [io.mimi.sdk.testflow.activity.TestFlowActivity].
 *
 * Some applications don't use the Mimi Profile [io.mimi.sdk.profile.MimiProfileFragment], rather they
 * launch their own instance of the [io.mimi.sdk.testflow.activity.TestFlowActivity]. This is appropriate for app which focus on providing
 * Hearing Tests, rather than Sound Personalization.
 *
 * This example, directly launches the [io.mimi.sdk.testflow.activity.TestFlowActivity] and displays the result data
 * returned from it.
 *
 * The result data is a JSON string, whose format matches the [io.mimi.sdk.testflow.flowfactory.TestFlowResponse] class.
 * [TestFlowResultContract] uses the [io.mimi.sdk.core.moshi] object to deserialize it.
 *
 * This Fragment also shows how to inform the MSDK of the currently connected headphone model before
 * launching [io.mimi.sdk.testflow.activity.TestFlowActivity] to improve PTT Hearing Test accuracy.
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

    private val testFlowLauncher =
        registerForActivityResult(TestFlowResultContract()) { response ->
            view?.findViewById<TextView>(R.id.testFlowResults)?.text =
                response?.let { testFlowResponseAdapter.indent("  ").toJson(it) }
                    ?: "Invalid or no result"
        }

    @OptIn(MsdkInternalApi::class)
    private fun launchTestFlowForResult() {
        testFlowLauncher.launch(TestFlowActivity.intent(requireActivity()))
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
     * When the TestFlow is launched, it will read from [io.mimi.sdk.core.MimiCore.testsController.connectedMimiHeadphone]
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
                        MimiVolumeAdjustmentApplicator.Companion.instance::isAbsoluteVolumeSupported,
                        MimiVolumeAdjustmentApplicator.Companion.instance::sendHearingTestStartCommand,
                        MimiVolumeAdjustmentApplicator.Companion.instance::sendHearingTestEndCommand
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

private val testFlowResponseAdapter = moshi.adapter(TestFlowResponse::class.java)

/**
 * Launches the [TestFlowActivity] from the supplied [Intent] and parses the result as a [TestFlowResponse].
 */
private class TestFlowResultContract : ActivityResultContract<Intent, TestFlowResponse?>() {

    override fun createIntent(context: Context, input: Intent): Intent = input

    override fun parseResult(resultCode: Int, intent: Intent?): TestFlowResponse? =
        // This deserializes the JSON response provided by the TestFlowActivity into a TestFlowResponse
        // instance. This example uses the MSDK Moshi instance, which is already configured to parse
        // the complex JSON structure. Depending on your needs, you could deserialize with different techniques.
        intent?.getStringExtra(TestFlowActivity.EXTRA_HEARING_TEST_RESULTS)
            ?.let { testFlowResponseAdapter.fromJson(it) }
}
