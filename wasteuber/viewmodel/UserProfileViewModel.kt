package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.data.repository.UserRepository

class UserProfileViewModel : ViewModel() {

    private val userRepository =
        UserRepository()

    fun getUserById(
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        userRepository.getUserById(
            userId = userId,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}