package io.mimi.example.android.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.mimi.example.android.R
import io.mimi.sdk.profile.MimiProfileFragment
import kotlinx.coroutines.launch

/**
 * An example of how to include the [io.mimi.sdk.profile.MimiProfileFragment] in your application.
 *
 * Provides additional "debug" controls to allow quick authentication and onboarding of a user. In
 * your application.
 *
 * The MimiProfileFragment requires a Theme derived from Theme.Mimi, which is an AppCompat theme,
 * so you're using Compose with a ComponentActivity, then you'll need to launch MimiProfileFragment
 * in its own Activity with its `android:theme` defined appropriately in the `AndroidManifest.xml` (not shown here).
 */
class ProfileLauncherCardFragment : Fragment(R.layout.fragment_profile_launcher_card) {

    private val profileViewModel by viewModels<ProfileLauncherCardViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            setupMimiProfileLauncherUi()
            observeUiState()
            observeMessages()
        }
    }

    private fun View.observeUiState() = lifecycleScope.launch {
        // Re-collecting on each RESUMED keeps the switch up-to-date, for example when the user was
        // deleted from within the Mimi Profile.
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            profileViewModel.uiState.collect { uiState ->
                findViewById<SwitchCompat>(R.id.mimiAuthenticateUserSwitch).isChecked =
                    uiState.isUserAuthenticated
            }
        }
    }

    private fun observeMessages() = lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            profileViewModel.messages.collect { message ->
                Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
     * Opens the Mimi Profile UI.
     */

    private fun View.setupMimiProfileLauncherUi() {

        // Controls to create an anonymous user before launching the Mimi Profile to simulate an
        // existing user.
        val shouldIncludeYearOfBirthCheckBox = findViewById<CheckBox>(R.id.include_yob_checkbox)

        // Note: this is an OnClickListener, and deliberately not an OnCheckedChangeListener.
        //       A CompoundButton only invokes its OnClickListener for real user interaction,
        //       whereas OnCheckedChangeListener also fires when `isChecked` is set programmatically
        //       and when the saved view state is restored - which is not user intent.
        findViewById<SwitchCompat>(R.id.mimiAuthenticateUserSwitch).setOnClickListener { switch ->
            profileViewModel.onAuthenticatedSwitchClicked(
                isChecked = (switch as SwitchCompat).isChecked,
                includeYearOfBirth = shouldIncludeYearOfBirthCheckBox.isChecked
            )
        }

        findViewById<Button>(R.id.launchProfileButton).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<MimiProfileFragment>(R.id.mimiContainerFragment)
                addToBackStack("main")
            }
        }
    }

}
