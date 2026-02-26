package com.yoki.zarqaproduction.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.yoki.zarqaproduction.data.model.UserProfile
import com.yoki.zarqaproduction.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInProfile: UserProfile? = null
)

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        val error = validate(state.email, state.password)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.login(state.email.trim(), state.password)
                .onSuccess { profile ->
                    Timber.i("Login sukses: ${profile.name} (${profile.role})")
                    _uiState.update { it.copy(isLoading = false, loggedInProfile = profile) }
                }
                .onFailure { e ->
                    Timber.e(e, "Login gagal")
                    _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
                }
        }
    }

    fun clearNavigated() {
        _uiState.update { it.copy(loggedInProfile = null) }
    }

    private fun validate(email: String, password: String): String? = when {
        email.isBlank()       -> "Email tidak boleh kosong"
        !email.contains("@") -> "Format email tidak valid"
        password.length < 6  -> "Password minimal 6 karakter"
        else                  -> null
    }

    private fun mapError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Email atau password salah"
        is FirebaseAuthInvalidUserException        -> "Email atau password salah"
        else -> if (e.message?.contains("network", ignoreCase = true) == true)
            "Periksa koneksi internet"
        else
            "Terjadi kesalahan. Coba lagi."
    }
}
