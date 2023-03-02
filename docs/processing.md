# Processing

## Component Info

Read about the various components [here](https://mimihearingtechnologies.github.io/SDK-Android/latest/processing/)

## Initialization and Activation

- Once you have already initialized `MimiCore`, you can access the Processing APIs from the `ProcessingController` and activate a `ProcessingSession` to add `Applicator`s and modify `ProcessingParameter` values.

- When activating a `ProcessingSession`, you need to provide a `Fitting` value.

    > `Fitting` model provides data about the current processing environment and in turn how presets should be generated.

**Example:**
> Setup processing controller and session in your MainActivity.kt file

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    // Your code
    
    /*
     * ----- Mimi code -----
     * Executing this line would
     * 1. Deactivate session
     * 2. Activate session on configuration change
     *
     * This means if there are references to the old ProcessingSession
     * which have been held at a different lifecycle scope, then they
     * become invalid.
     * 
     * This should be called at the right point in your code flow.
     * This is an example to show it being called in onCreate(). but can
     * be called later in lifecycle as per business use-case requirement
     */
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            activateProcessingSession()
        }
    }
}

private suspend fun activateProcessingSession() {
    val processingController = MimiCore.processingController
    val upDownDataSourceConfig = MimiPresetParameterDataSourceConfiguration.UpDown(fitting = getTechLevelFromFirmware())
        val upDownPresetDataSource = MimiCore.personalizationController.createPresetParameterDataSource(upDownDataSourceConfig)
        processingController.activateSession(upDownPresetDataSource)
}

private fun getTechLevelFromFirmware(): Fitting {
    // Usually requested via Bluetooth connection
    // This is hardcoded as an example
    return Fitting(techLevel = 4)
}
```

## `ProcessingParameter` Operations

### Reading the `ProcessingParameter` state

- You need access to activeSession to read value from `ProcessingParameter`
  
```kotlin
// Acquire the active ProcessSession (assumes already activated!)
private val activeSession: ProcessingSession by lazy {
    requireNotNull(MimiCore.processingController.activeSession.state)
}
```

#### Example for accessing isEnabled parameter

> The same style applies to other parameters i.e `preset` and `intensity`.

**Get the `value`:**

```kotlin
// To force getting a value from a Parameter,
// Usage: val isEnabledValue = getIsEnabledParam()
fun getIsEnabledParam() = activeSession.isEnabled.value
```

**`observe` the changes in value:**

[Refer to Advanced Processing](advanced-processing.md)

### Setting the `ProcessingParameter` state

### Defining your own Applicator

When creating an [`Applicator`](https://mimihearingtechnologies.github.io/SDK-Android/latest/processing/#adding-an-applicator-to-a-processingparameter), we recommend delegating your `canApply` and `apply` functions to a class containing your custom processing logic. This custom processing logic depends entirely on your processing system. Generally, this approach helps make your code more modular and testable.

> Note: This is simplified sample code to demonstrate the general sequence and may not reflect the best structure for your particular usecase.

#### Setup MimiProcessingApplicator

This class contains custom logic related to how MSDK will transfer the changes to Processing unit i.e bluetooth headset as bluetooth packets.

```kotlin
import android.util.Log
import io.mimi.sdk.core.model.personalization.Personalization

class MimiProcessingApplicator {

    private val TAG: String = this.javaClass.simpleName

    fun setPreset(preset: Personalization.PersonalizationPreset?) {
        if (preset == null) {
            clearMimiCommand()
        } else {
            Log.d(TAG, "Send the values to the external device via bluetooth $preset")
        }
    }

    fun setIntensity(intensity: Double) {
        Log.d(TAG, "Send the values to the external device via bluetooth $intensity")
    }

    fun setEnabled(enabled: Boolean) {
        Log.d(TAG, "Send the values to the external device via bluetooth $enabled")
    }

    private fun clearMimiCommand() {
        Log.d(TAG, "Send Clear Command to external device via bluetooth")
    }
}
```

#### Example for creating an isEnabled parameter applicator

```kotlin
class IsEnabledApplicator(private val applicator: MimiProcessingApplicator) {
    fun canApply() = true

    fun apply(enabled: Boolean) = applicator.setEnabled(enabled)
}
```

### Wireup applicator in MainActivity.kt

```kotlin
// Declare an instance; depending on your usecase, you may want this to be a singleton.
private val mimiProcessingApplicator = MimiProcessingApplicator()
private val isEnabledApplicator = IsEnabledApplicator(mimiProcessingApplicator)

// TODO - You should use an appropriate value for your integration.
private val APPLY_TIMEOUT: Long = 10_000L

suspend fun addIsEnabledApplicator(
    isEnabledParam: MimiProcessingParameter<Boolean>,
): MimiParameterApplicator {
    // Add the Applicator to the param, delegating the calls to your
    // custom applicator logic
    val applicator = isEnabledParam.addApplicator(
        { isEnabledApplicator.canApply() },
        isEnabledApplicator::apply,
        APPLY_TIMEOUT
    )

    // Causes the isEnabled ProcessingParameter to push its current
    // value to the newly added Applicator
    isEnabledParam.synchronize()
    return applicator
}
```
Next, add the applicator to active session inside `activateProcessingSession()`:

```kotlin
var isEnabledApplicatorRef: MimiParameterApplicator? = null

private suspend fun activateProcessingSession() {
    val processingController = MimiCore.processingController
    processingController.activateSession(getTechLevelFromFirmware())

    // Wire up the applicator to the activeSession
    isEnabledApplicatorRef = addIsEnabledApplicator(activeSession.isEnabled)

    // add more applicators here for intensity and preset
}
```

Retain the `applicator` reference so that you can later remove it from the `ProcessingParameter`. 

Once an `Applicator` has been removed, it will no longer receive updates from the `ProcessingParameter`.

**Example: Removing Applicator**

```kotlin
// Removes the Applicator from its ProcessingParameter so it won't receive 
// further updates.
private fun removeIsEnabledApplicator() {
    isEnabledApplicatorRef?.remove()
}
```

### Setting a value

```kotlin
// To force set a value on a Parameter
// Usage: setIsEnabledParam(true)
fun setIsEnabledParam(flag: Boolean) {
    isEnabledApplicator.apply(flag)
}
```
