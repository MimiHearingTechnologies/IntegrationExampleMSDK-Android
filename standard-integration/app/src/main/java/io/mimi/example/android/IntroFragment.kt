package io.mimi.example.android

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.mimi.sdk.BuildConfig

class IntroFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            setupMimiInfoPanel()
        }
    }

    // region Mimi Profile

    /*
     * Opens the Mimi Profile UI.
     */

    @SuppressLint("SetTextI18n")
    private fun View.setupMimiInfoPanel() {
        findViewById<TextView>(R.id.versionTextView).text =
            "MSDK Version: ${io.mimi.sdk.BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
    }
}