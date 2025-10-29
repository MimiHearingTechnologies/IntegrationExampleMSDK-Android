package io.mimi.example.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.mimi.example.android.applicators.processing.automatic.FakeAutomaticProcessorCommunicationPlugin
import io.mimi.example.android.applicators.processing.automatic.FakeAutomaticProcessorDevice
import io.mimi.example.android.applicators.processing.basic.ExampleBasicProcessingApplicator
import io.mimi.example.android.applicators.processing.basic.IntensityApplicator
import io.mimi.example.android.applicators.processing.basic.IsEnabledApplicator
import io.mimi.example.android.applicators.processing.basic.PresetApplicator
import io.mimi.sdk.common.annotations.MsdkExperimentalApi
import io.mimi.sdk.common.observable.asFlow
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.common.LoadingState
import io.mimi.sdk.core.controller.processing.config.MimiProcessingConfiguration
import io.mimi.sdk.core.controller.processing.config.dsl.automatic.dsl.applicator
import io.mimi.sdk.core.controller.processing.config.dsl.automatic.dsl.processor
import io.mimi.sdk.core.controller.processing.config.dsl.basic.dsl.applicators
import io.mimi.sdk.core.controller.processing.config.dsl.basic.dsl.fineTuning
import io.mimi.sdk.core.controller.processing.config.dsl.basic.dsl.intensity
import io.mimi.sdk.core.controller.processing.config.dsl.basic.dsl.isEnabled
import io.mimi.sdk.core.controller.processing.config.dsl.basic.dsl.preset
import io.mimi.sdk.core.controller.processing.config.dsl.basic.dsl.soundPersonalization
import io.mimi.sdk.core.controller.processing.config.dsl.mimiAutomaticProcessingConfiguration
import io.mimi.sdk.core.controller.processing.config.dsl.mimiBasicProcessingConfiguration
import io.mimi.sdk.core.controller.processing.config.model.automatic.MimiAutomaticProcessorConfiguration
import io.mimi.sdk.core.controller.processing.config.model.automatic.MimiProcessorApplicatorConfiguration
import io.mimi.sdk.core.controller.processing.config.model.basic.PersonalizationModeConfiguration
import io.mimi.sdk.core.controller.processing.config.model.basic.ProcessingParameterConfiguration
import io.mimi.sdk.core.controller.processing.config.model.basic.SoundPersonalizationFeatureConfiguration
import io.mimi.sdk.core.controller.processing.config.model.basic.SoundPersonalizationParametersConfiguration
import io.mimi.sdk.processing.model.Fitting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * This shows a combined example of how to configure and activate both varieties of MimiProcessing:
 * - Basic (only suitable for older devices, which only support simple Mimi Sound Personalization)
 * - Automatic (only suitable for our new firmware, which supports a wider range of optional features)
 *
 * The available features their modules are defined in the [io.mimi.sdk.processing.ProcessingSession].
 *
 * In headphone integration, it will become likely to support both types of configurations, once
 * your headphones adopt new firmware.
 */
class ProcessingCardFragmentViewModel(
    private val basicApplicator: ExampleBasicProcessingApplicator = ExampleBasicProcessingApplicator(),
    private val automaticProcessorPlugin: FakeAutomaticProcessorCommunicationPlugin =
        FakeAutomaticProcessorCommunicationPlugin(FakeAutomaticProcessorDevice())
) :
    ViewModel() {

    private val TAG = this::class.simpleName

    // You should define a timeout which best suits your integration
    // This example is 10 seconds and we've chosen it to be the same for all parameters.
    //
    // Note: Due to the larger payloads used, it may be necessary to define a larger timeout for
    // "Automatic" integrations, than for the Basic configuration.
    private val APPLICATOR_TIMEOUT: Duration = 10.toDuration(DurationUnit.SECONDS)

    private val _uiState = MutableStateFlow<UiState>(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            MimiCore.processingController.activeSession.asFlow()
                .collect { session ->
                    Log.d(TAG, "Active Session changed: $session")
                    _uiState.update {
                        it.copy(
                            hasActiveProcessingSession = session.value != null,
                            loadingState = session.loadingState
                        )
                    }
                }
        }
    }

    //region Basic Mimi Processing

    /**
     * A Basic Processor only has media SoundPersonalization and consequently, only three
     * MimiProcessingParameters. But you need to manually configure the ProcessingSession and
     * define an MimiApplicator function for each.
     */

    private val isEnabledApplicator = IsEnabledApplicator(basicApplicator)
    private val intensityApplicator = IntensityApplicator(basicApplicator)
    private val presetApplicator = PresetApplicator(basicApplicator)

    fun activateBasicProcessingSession() {
        Log.d(TAG, "activateBasicProcessingSession()")
        viewModelScope.launch {
            val basicConfig = defineMimiBasicProcessingConfiguration()
            MimiCore.processingController.activateSession(basicConfig)
                .fold(
                    onSuccess = {
                        // In this example we don't handle the result of the synchronization
                        // as any failure will reported in the Mimi Profile UI.
                        with(it.soundPersonalization?.media) {
                            // synchronizeApplicator ensures that the Processing device receives the latest
                            // value from the MSDK cache.
                            this?.isEnabled?.synchronizeApplicator()
                            this?.intensity?.synchronizeApplicator()
                            this?.preset?.synchronizeApplicator()
                        }
                    },
                    onFailure = { Log.e(TAG, "Failed to activate a Basic Processing Session", it) }
                )
        }
    }

    private fun defineMimiBasicProcessingConfiguration(): MimiProcessingConfiguration.Basic {
        return MimiProcessingConfiguration.Basic(
            soundPersonalization = SoundPersonalizationFeatureConfiguration(
                mode = PersonalizationModeConfiguration.FineTuning(fitting = getTechLevelFromFirmware()),
                parameterConfiguration = SoundPersonalizationParametersConfiguration(
                    isEnabled = ProcessingParameterConfiguration(
                        APPLICATOR_TIMEOUT,
                        isEnabledApplicator::apply
                    ),
                    intensity = ProcessingParameterConfiguration(
                        APPLICATOR_TIMEOUT,
                        intensityApplicator::apply
                    ),
                    preset = ProcessingParameterConfiguration(
                        APPLICATOR_TIMEOUT,
                        presetApplicator::apply
                    ),
                )
            )
        )
    }

    // An alternative syntax for Basic - still experimental, so opt-in is required!
    @OptIn(MsdkExperimentalApi::class)
    private fun defineBasicMimiProcessingConfigurationUsingDsl(): MimiProcessingConfiguration.Basic {
        return mimiBasicProcessingConfiguration {
            soundPersonalization {
                fineTuning {
                    fitting = getTechLevelFromFirmware()
                }
                applicators {
                    isEnabled(APPLICATOR_TIMEOUT) {
                        isEnabledApplicator.apply(it)
                    }
                    intensity(APPLICATOR_TIMEOUT) {
                        intensityApplicator.apply(it)
                    }
                    preset(APPLICATOR_TIMEOUT) {
                        presetApplicator.apply(it)
                    }
                }
            }
        }
    }

    private fun getTechLevelFromFirmware(): Fitting {
        // In Basic Configuration - is usually requested from the headphones via Bluetooth connection
        // TODO - This is hardcoded as an example!
        return Fitting(techLevel = 4)
    }

    // endregion


    // region Automatic Mimi Processing

    /**
     * For Mimi Automatic Processing Configuration, there is only a single "Applicator" function.
     *
     * The MSDK uses it for all related communication with the Processing device.
     *
     * During ProcessingSession activation, the MSDK will query the device for its capabilities
     * and current state.
     *
     * When the value of a MimiProcessingParameter is changed, the MSDK sends the appropriate
     * state update request to the device.
     *
     * On the mobile device, your integration simply needs to transmit these payloads, and does not
     * need to understand or deserialize the contents of the messages.
     */

    fun activateAutomaticProcessingSession() {
        Log.d(TAG, "activateAutomaticProcessingSession()")
        viewModelScope.launch {
            val automaticConfig = defineMimiAutomaticProcessingConfiguration()
            MimiCore.processingController.activateSession(automaticConfig)
        }
    }

    private fun defineMimiAutomaticProcessingConfiguration(): MimiProcessingConfiguration.Automatic {
        return MimiProcessingConfiguration.Automatic(
            processor = MimiAutomaticProcessorConfiguration(
                applicator =
                    MimiProcessorApplicatorConfiguration.Protobuf(timeout = APPLICATOR_TIMEOUT) { requestAsByteArray ->
                        automaticProcessorPlugin.send(requestAsByteArray)
                    }
            )
        )
    }

    // An alternative syntax for Automatic - still experimental, so opt-in is required!
    @OptIn(MsdkExperimentalApi::class)
    private fun defineMimiAutomaticProcessingConfigurationUsingDsl(): MimiProcessingConfiguration.Automatic {
        return mimiAutomaticProcessingConfiguration {
            processor {
                applicator(timeout = APPLICATOR_TIMEOUT) { requestAsByteArray ->
                    automaticProcessorPlugin.send(requestAsByteArray)
                }
            }
        }
    }

    // endregion

    fun deactivateSession() {
        // Same API for both Basic and Automatic configuration modes.
        Log.d(TAG, "deactivateSession()")
        viewModelScope.launch { MimiCore.processingController.deactivateSession() }
    }

    //endregion

    data class UiState(
        val hasActiveProcessingSession: Boolean = false,
        val loadingState: LoadingState = LoadingState.Done,
    )

}