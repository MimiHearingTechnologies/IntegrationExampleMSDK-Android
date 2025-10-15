package io.mimi.example.android

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.mimi.sdk.common.annotations.MsdkInternalApi
import io.mimi.sdk.testflow.activity.TestFlowActivity
import io.mimi.sdk.testflow.internal.debug.DebugOptions
import org.json.JSONArray
import org.json.JSONObject

/**
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
 */
class TestFlowLauncherCardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_flow_launcher_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setupLaunchTestFlowUi()
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
        val intent =
            TestFlowActivity.intent(requireActivity(), DebugOptions(isMockedHeadphones = true))
        startActivityForResult(intent, 10)
    }

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

}