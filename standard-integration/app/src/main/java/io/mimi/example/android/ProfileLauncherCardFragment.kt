package io.mimi.example.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.model.MimiAuthRoute
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

    private val TAG = this::class.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            setupMimiProfileLauncherUi()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        updateUserSwitchUi() // make sure up-to-date, if deleted from Profile.
    }

    private fun updateUserSwitchUi() {
        val authenticateUserSwitch =
            view?.findViewById<SwitchCompat>(R.id.mimiAuthenticateUserSwitch)
        authenticateUserSwitch?.apply {
            val isUserAuthenticated = MimiCore.userController.mimiUser.state.value != null
            isChecked = isUserAuthenticated
        }
    }

    /*
     * Opens the Mimi Profile UI.
     */

    @SuppressLint("SetTextI18n")
    private fun View.setupMimiProfileLauncherUi() {

        Log.d(TAG, "setupMimiProfileLauncherUi()")
        // Controls to create an anonymous user before launching the Mimi Profile to simulate an existing user.
        //
        // If the "YoB" is defined, then the user is considered "onboarded" and the Profile skips
        // the introduction and onboarding screens.
        //
        // Note: Users are also onboarded if they have a Hearing Test submission.
        val shouldIncludeYearOfBirthCheckBox = findViewById<CheckBox>(R.id.include_yob_checkbox)
        val exampleYoBForDemo = 1984

        val authenticateUserSwitch = findViewById<SwitchCompat>(R.id.mimiAuthenticateUserSwitch)
        authenticateUserSwitch.apply {

            updateUserSwitchUi()

            setOnCheckedChangeListener { _, isChecked ->
                val shouldDelete =
                    !isChecked && MimiCore.userController.mimiUser.state.value != null
                val shouldAuthenticate =
                    isChecked && MimiCore.userController.mimiUser.state.value == null
                lifecycleScope.launch {
                    if (shouldAuthenticate) {
                        Log.d(TAG, "Authenticating user")
                        try {
                            val user = if (shouldIncludeYearOfBirthCheckBox.isChecked) {
                                MimiCore.userController.authenticate(MimiAuthRoute.Anonymously)
                                MimiCore.userController.submitYearOfBirth(exampleYoBForDemo)
                            } else {
                                MimiCore.userController.authenticate(MimiAuthRoute.Anonymously)
                            }
                            Toast.makeText(
                                requireActivity(),
                                "Created user: ${user.id}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireActivity(),
                                "Failed to create user: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (shouldDelete) {
                        // Delete the user
                        Log.d(TAG, "Deleting current user")
                        MimiCore.userController.logout()
                    }
                }
            }
        }

        findViewById<Button>(R.id.launchProfileButton).apply {
            setOnClickListener {
                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<MimiProfileFragment>(R.id.mimiContainerFragment)
                    addToBackStack("main")
                }
            }
        }

    }

}


