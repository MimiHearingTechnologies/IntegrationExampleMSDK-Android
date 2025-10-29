package io.mimi.example.android.applicators.processing.automatic

import android.util.Log

/**
 * This is a fake, in-memory representation of a Mimi Processor, such as headphones.
 *
 * The logical equivalent is implemented in your headphone firmware.
 */
class FakeAutomaticProcessorDevice {

    private val TAG: String = this.javaClass.simpleName

     fun process(input : ByteArray) : Result<ByteArray> {
       // A
       Log.d(TAG, "Received - input: ${input.size}")
       return Result.success(ByteArray(0))
    }
}
