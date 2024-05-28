package io.mimi.example.android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.mimi.example.android.applicators.IntensityApplicator
import io.mimi.example.android.applicators.IsEnabledApplicator
import io.mimi.example.android.applicators.PresetApplicator
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.controller.processing.Fitting
import io.mimi.sdk.core.controller.processing.MimiApplicatorResult
import io.mimi.sdk.core.controller.processing.MimiParameterApplicator
import io.mimi.sdk.core.controller.processing.MimiProcessingParameter
import io.mimi.sdk.core.controller.processing.ProcessingParameterResult
import io.mimi.sdk.core.controller.processing.ProcessingSession
import io.mimi.sdk.core.controller.processing.config.MimiProcessingConfiguration
import io.mimi.sdk.core.controller.processing.config.PersonalizationConfiguration
import io.mimi.sdk.core.controller.processing.config.PersonalizationModeConfiguration
import io.mimi.sdk.core.controller.processing.config.dsl.fineTuning
import io.mimi.sdk.core.controller.processing.config.dsl.mimiProcessingConfiguration
import io.mimi.sdk.core.controller.processing.config.dsl.personalization
import io.mimi.sdk.core.internal.MsdkExperimentalApi
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainActivity : AppCompatActivity() {

    private val tag: String = this::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerLocationServiceStateReceiver()

        /*
         * Executing this line would
         * 1. Deactivate session
         * 2. Activate session on configuration change
         *
         * This means if there are references to the old ProcessingSession
         * which have been held at a different lifecycle scope, then they
         * become invalid.
         */
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                activateProcessingSession()
            }
        }
    }

    //region Permission Handling
    companion object {
        private const val ACCESS_LOCATION_REQUEST = 1
    }

    private fun registerLocationServiceStateReceiver() {
        registerReceiver(
            locationServiceStateReceiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        )
    }

    private val enableBluetoothRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // Bluetooth has been enabled
                checkPermissions()
            } else {
                // Bluetooth has not been enabled, try again
                askToEnableBluetooth()
            }
        }

    private val bluetoothManager by lazy {
        applicationContext
            .getSystemService(BLUETOOTH_SERVICE)
                as BluetoothManager
    }

    private val isBluetoothEnabled: Boolean
        get() {
            val bluetoothAdapter = bluetoothManager.adapter ?: return false
            return bluetoothAdapter.isEnabled
        }

    private fun askToEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothRequest.launch(enableBtIntent)
    }

    private fun checkPermissions() {
        val missingPermissions = getMissingPermissions(requiredPermissions)
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions, ACCESS_LOCATION_REQUEST)
        } else {
            permissionsGranted()
        }
    }

    private fun getMissingPermissions(requiredPermissions: Array<String>): Array<String> {
        val missingPermissions: MutableList<String> = ArrayList()
        for (requiredPermission in requiredPermissions) {
            val permissionIsGranted = ContextCompat.checkSelfPermission(
                this,
                requiredPermission
            ) != PackageManager.PERMISSION_GRANTED
            if (permissionIsGranted) {
                missingPermissions.add(requiredPermission)
            }
        }
        return missingPermissions.toTypedArray()
    }

    private fun permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        val targetSdkVersion = applicationInfo.targetSdkVersion
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                initializeBluetoothHandler()
            }
        } else {
            initializeBluetoothHandler()
        }
    }

    private fun initializeBluetoothHandler() {
        // Initialize your Bluetooth Handler here
        // This would include setting/unsetting the HeadphoneIdentifierStore.instance.headphoneModelId
        // when the headphones are connected/disconnected
    }

    private fun checkLocationServices(): Boolean {
        return if (!areLocationServicesEnabled()) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Location services are not enabled")
                .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                .setPositiveButton("Enable") { dialogInterface, _ ->
                    dialogInterface.cancel()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel()
                }
                .create()
                .show()
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if all permission were granted
        var allGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }
        if (allGranted) {
            permissionsGranted()
        } else {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Location permission is required for scanning Bluetooth peripherals")
                .setMessage("Please grant permissions")
                .setPositiveButton("Retry") { dialogInterface, _ ->
                    dialogInterface.cancel()
                    checkPermissions()
                }
                .create()
                .show()
        }
    }


    private fun areLocationServicesEnabled(): Boolean {
        val locationManager =
            applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            isGpsEnabled || isNetworkEnabled
        }
    }

    private val requiredPermissions: Array<String>
        get() {
            val targetSdkVersion = applicationInfo.targetSdkVersion
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            } else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

    private val locationServiceStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == LocationManager.MODE_CHANGED_ACTION) {
                val isEnabled = areLocationServicesEnabled()
                val logMessage = if (isEnabled) "on" else "off"
                Log.i(tag, "Location service state changed to: $logMessage")
                checkPermissions()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationServiceStateReceiver)
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothManager.adapter != null) {
            if (!isBluetoothEnabled) {
                askToEnableBluetooth()
            } else {
                checkPermissions()
            }
        } else {
            Log.e(tag, "This device has no Bluetooth hardware")
        }
    }
    //endregion

    //region Mimi
    // Acquire the active ProcessSession (assumes already activated!)
    private val activeSession: ProcessingSession by lazy { requireNotNull(MimiCore.processingController.activeSession.state) }

    private val applicator = MimiProcessingApplicator()
    private val isEnabledApplicator = IsEnabledApplicator(applicator)
    private var isEnabledApplicatorRef: MimiParameterApplicator? = null
    private val intensityApplicator = IntensityApplicator(applicator)
    private var intensityApplicatorRef: MimiParameterApplicator? = null
    private val presetApplicator = PresetApplicator(applicator)
    private var presetApplicatorRef: MimiParameterApplicator? = null

    // You should define a timeout which best suits your integration
    // This example is 10 seconds and we've chosen it to be the same for all parameters.
    private val APPLY_TIMEOUT: Duration = 10.toDuration(DurationUnit.SECONDS)

    /*
     This will do a deactivation, then activation on a configuration change.
     */
    private suspend fun activateProcessingSession() {

        val config = defineMimiProcessingConfiguration()
        MimiCore.processingController.activateSession(config)

        // Wire up the applicators to the activeSession
        // In this example we don't want to handle the result of the synchronization
        // Any failure will reported in the Mimi Profile UI.
        with(activeSession) {
            val (newIsEnabledApplicatorRef, _) =
                isEnabled.addApplicatorAndSynchronize(
                    APPLY_TIMEOUT,
                    isEnabledApplicator::apply
                )
            val (newIntensityApplicatorRef, _) =
                intensity.addApplicatorAndSynchronize(
                    APPLY_TIMEOUT,
                    intensityApplicator::apply
                )
            val (newPresetApplicatorRef, _) =
                activeSession.preset.addApplicatorAndSynchronize(
                    APPLY_TIMEOUT,
                    presetApplicator::apply
                )
            // Assign so we can dispose later.
            with(this@MainActivity) {
                isEnabledApplicatorRef = newIsEnabledApplicatorRef
                intensityApplicatorRef = newIntensityApplicatorRef
                presetApplicatorRef = newPresetApplicatorRef
            }
        }

    }

    private fun defineMimiProcessingConfiguration(): MimiProcessingConfiguration {
        return MimiProcessingConfiguration(
            personalization = PersonalizationConfiguration(
                mode = PersonalizationModeConfiguration.FineTuning(fitting = getTechLevelFromFirmware())
            )
        )
    }

    // An alternative syntax - still experimental, so opt-in is required!
    @OptIn(MsdkExperimentalApi::class)
    private fun defineMimiProcessingConfigurationUsingDsl(): MimiProcessingConfiguration {
        return mimiProcessingConfiguration {
            personalization {
                fineTuning {
                    fitting = getTechLevelFromFirmware()
                }
            }
        }
    }

    // Removes the Applicator from its ProcessingParameter so it won't receive
    // further updates.
    private fun removeApplicators() {
        isEnabledApplicatorRef?.remove()
        intensityApplicatorRef?.remove()
        presetApplicatorRef?.remove()
    }

    private fun getTechLevelFromFirmware(): Fitting {
        // Usually requested via Bluetooth connection
        // TODO - This is hardcoded as an example!
        return Fitting(techLevel = 4)
    }

    /**
     * This is a helper function to add an Applicator to a Processing Parameter
     * and then synchronize the newly added Applicator with the Processing Parameter's current value.
     */
    private suspend fun <T> MimiProcessingParameter<T>.addApplicatorAndSynchronize(
        applyTimeout: Duration,
        applyFn: (T) -> MimiApplicatorResult
    ): Pair<MimiParameterApplicator, ProcessingParameterResult> {
        // Add the Applicator to the Processing Parameter,
        // delegating the calls to your custom applicator logic.
        val newApplicator = addApplicator(applyTimeout, applyFn)

        // Causes the Processing Parameter to push its current value
        // to the newly added Applicator.
        // This may fail, so we may want to handle the result.
        val result = synchronizeApplicators()

        return newApplicator to result
    }
    //endregion
}