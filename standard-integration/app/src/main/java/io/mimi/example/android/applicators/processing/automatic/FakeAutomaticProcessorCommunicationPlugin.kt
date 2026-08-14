package io.mimi.example.android.applicators.processing.automatic

import android.util.Log
import io.mimi.debug.mock.MockProcessorDevice

/**
 * This is a fake representation of a component which you will need to implement.
 *
 * It is responsible for sending (typically via Bluetooth), the input payload to the connected
 * processor device.
 *
 * Here the connected device is stood in for by [MockProcessorDevice], an in-memory Mimi Processor
 * from the `io.mimi:msdk-debug-mocks` library. The logical equivalent is implemented in your
 * headphone firmware.
 */
class FakeAutomaticProcessorCommunicationPlugin(private val mockProcessorDevice: MockProcessorDevice) {

    private val TAG: String = this.javaClass.simpleName

    @Suppress("RedundantSuspendModifier")
    suspend fun send(input: ByteArray): Result<ByteArray> {
        Log.d(TAG, "send() - input length: ${input.size}")
        Log.d(TAG, "send() - TODO() Send the ByteArray to the device via Bluetooth")

        // Note: You don't need to handle any Exceptions here, you can let the propagate to the MSDK, and it
        // will mark the call as a Failure.

        // Send the ByteArray to your Processing device, then return the ByteArray from the response.
        return mockProcessorDevice.send(input)
    }
}
