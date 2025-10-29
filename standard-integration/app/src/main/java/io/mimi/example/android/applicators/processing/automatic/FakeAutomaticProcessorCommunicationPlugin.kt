package io.mimi.example.android.applicators.processing.automatic

import android.util.Log

/**
 * This is a fake representation of a component which you will need to implement.
 *
 * It is responsible for sending (typically via Bluetooth), the input payload to the connected
 * processor device.
 *
 * Note: Without a compatible device, you won't be able to successfully activate the session,
 *       as this it used to query for device capabilities, and also to change the device state.
 */
class FakeAutomaticProcessorCommunicationPlugin(private val fakeAutomaticProcessorDevice: FakeAutomaticProcessorDevice) {

    private val TAG: String = this.javaClass.simpleName

    @Suppress("RedundantSuspendModifier")
    suspend fun send(input : ByteArray) : Result<ByteArray> {
        Log.d(TAG, "send() - input length: ${input.size}")
        Log.d(TAG, "send() - TODO() Send the ByteArray to the device via Bluetooth")

        // Note: You don't need to handle any Exceptions here, you can let the propagate to the MSDK, and it
        // will mark the call as a Failure.

        // Send the ByteArray to your Processing device, then return the ByteArray from the response.
        return fakeAutomaticProcessorDevice.process(input)
    }
}
