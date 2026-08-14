package io.mimi.example.android.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.mimi.sdk.common.observable.asFlow
import io.mimi.sdk.core.MimiCore
import io.mimi.sdk.core.model.MimiAuthRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the state and the user management actions for [ProfileLauncherCardFragment].
 *
 * The authenticated state is owned here and derived from the MSDK, so that the "User authenticated"
 * switch is only ever a *reflection* of [MimiCore]'s user state. The Fragment reports deliberate
 * user interaction to this ViewModel, and never the other way around.
 */
class ProfileLauncherCardViewModel : ViewModel() {

    private val TAG = this::class.simpleName

    private val _uiState = MutableStateFlow(
        UiState(isUserAuthenticated = MimiCore.userController.mimiUser.state.value != null)
    )
    val uiState: StateFlow<UiState> = _uiState

    // One-off messages for the UI to surface, e.g. as a Toast. Not part of UiState, as they should
    // not be re-shown when the UI is recreated.
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages

    init {
        viewModelScope.launch {
            MimiCore.userController.mimiUser.asFlow().collect { mimiUser ->
                Log.d(TAG, "MimiUser changed: $mimiUser")
                _uiState.update { it.copy(isUserAuthenticated = mimiUser.value != null) }
            }
        }
    }

    /**
     * Handles a *deliberate* toggle of the "User authenticated" switch.
     *
     * The state is re-checked against the MSDK here, so that a switch position which already agrees
     * with the MSDK does nothing.
     */
    fun onAuthenticatedSwitchClicked(isChecked: Boolean, includeYearOfBirth: Boolean) {
        val currentUser = MimiCore.userController.mimiUser.state.value
        when {
            isChecked && currentUser == null -> authenticateUser(includeYearOfBirth)
            !isChecked && currentUser != null -> deleteUser()
            else -> Log.d(TAG, "Switch already agrees with the MimiUser state - ignoring")
        }
    }

    private fun authenticateUser(includeYearOfBirth: Boolean) {
        Log.d(TAG, "Authenticating user")
        viewModelScope.launch {
            try {
                val user = if (includeYearOfBirth) {
                    MimiCore.userController.authenticate(MimiAuthRoute.Anonymously)
                    MimiCore.userController.submitYearOfBirth(EXAMPLE_YEAR_OF_BIRTH)
                } else {
                    MimiCore.userController.authenticate(MimiAuthRoute.Anonymously)
                }
                _messages.tryEmit("Created user: ${user.id}")
            } catch (e: Exception) {
                _messages.tryEmit("Failed to create user: $e")
            }
        }
    }

    private fun deleteUser() {
        Log.d(TAG, "Deleting current user")
        viewModelScope.launch { MimiCore.userController.logout() }
    }

    data class UiState(
        val isUserAuthenticated: Boolean = false,
    )

    private companion object {
        // If the "YoB" is defined, then the user is considered "onboarded" and the Profile skips
        // the introduction and onboarding screens.
        //
        // Note: Users are also onboarded if they have a Hearing Test submission.
        const val EXAMPLE_YEAR_OF_BIRTH = 1984
    }
}
