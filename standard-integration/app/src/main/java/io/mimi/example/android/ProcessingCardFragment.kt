package io.mimi.example.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.mimi.sdk.core.common.LoadingState
import kotlinx.coroutines.launch

/**
 * A demonstration of the different [io.mimi.sdk.processing.ProcessingSession] configuration modes.
 *
 * In your application, you wouldn't usually have a switch to activate/deactivate the
 * Mimi ProcessingSession, rather you would associate the ProcessingSession lifecycle
 * with the lifecycle of your Processor (headphones etc).
 *
 * Usually you would call [io.mimi.sdk.core.MimiCore.processingController.activateSession]
 * when the headphones are connected.
 *
 * Then [io.mimi.sdk.core.MimiCore.processingController.deactivateSession] when the headphone
 * are disconnected.
 *
 * Note: In this example app, the Automatic Configuration mode will fail to activate, as
 *       the MSDK requires a specific response from the Processing device, which is faked
 *       in this case.
 */
class ProcessingCardFragment : Fragment(R.layout.fragment_processing_card) {

    private val TAG = this::class.simpleName

    private val processingViewModel by activityViewModels<ProcessingCardFragmentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            setupMimiProcessingConfigurationUi()
            observeUiState()
        }
    }

    private fun View.observeUiState() = lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            processingViewModel.uiState.collect { uiState ->
                // Update state on changes
                Log.d(TAG, "UI received state change: $uiState")
                findViewById<SwitchCompat>(R.id.mimiActivateProcessingSwitch)?.apply {
                    isChecked = uiState.hasActiveProcessingSession
                }
                findViewById<ProgressBar>(R.id.sessionProgressBar)?.apply {
                    val isVisible = uiState.loadingState == LoadingState.InProgress
                    visibility = if(isVisible) View.VISIBLE else View.GONE
                }
                findViewById<TextView>(R.id.sessionStatusTxt)?.apply {
                    val isVisible = uiState.loadingState is LoadingState.Failure
                    visibility = if(isVisible) View.VISIBLE else View.GONE
                }
            }
        }
    }

    /*
     * Opens the Mimi Profile UI.
     */

    @SuppressLint("SetTextI18n")
    private fun View.setupMimiProcessingConfigurationUi() {

        val radioGroup = findViewById<RadioGroup>(R.id.processingRadioGroup)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.basicProcessingRadioBtn -> {
                    processingViewModel.deactivateSession()
                }
                R.id.autoProcessingRadioBtn -> {

                    processingViewModel.deactivateSession()
                }
            }
        }

        findViewById<SwitchCompat>(R.id.mimiActivateProcessingSwitch).apply {
            setOnCheckedChangeListener { _, isChecked ->
                val shouldActivate = isChecked
                if (shouldActivate) {
                    val shouldActivateBasic =
                        radioGroup.checkedRadioButtonId == R.id.basicProcessingRadioBtn
                    val shouldActivateAuto =
                        radioGroup.checkedRadioButtonId == R.id.autoProcessingRadioBtn

                    if (shouldActivateBasic) {
                        processingViewModel.activateBasicProcessingSession()
                    } else if (shouldActivateAuto) {
                        processingViewModel.activateAutomaticProcessingSession()
                    }
                } else {
                    processingViewModel.deactivateSession()
                }
            }
        }
    }
}
