package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.data.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.register(
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            password = password,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.login(
            email = email,
            password = password,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getCurrentUser(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.getCurrentUser(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun logout() {
        authRepository.logout()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }
}