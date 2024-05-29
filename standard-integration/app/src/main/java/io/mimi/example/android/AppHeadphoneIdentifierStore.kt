package io.mimi.example.android

class AppHeadphoneIdentifierStore {

    companion object {
        val instance : AppHeadphoneIdentifierStore by lazy {
            AppHeadphoneIdentifierStore()
        }
    }
    // This value should be set/unset when the headphone is connected/disconnected
    val headphoneModelId : String? = null
}
