package io.mimi.example.android

class HeadphoneIdentifierStore {

    companion object {
        val instance : HeadphoneIdentifierStore by lazy {
            HeadphoneIdentifierStore()
        }
    }
    // This value should be set/unset when the headphone is connected/disconnected
    val headphoneModelId : String? = null
}
