package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.data.repository.AdminModerationRepository

class AdminModerationViewModel : ViewModel() {

    private val adminModerationRepository =
        AdminModerationRepository()

    fun getUsers(
        onSuccess: (List<User>) -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.getUsers(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getUser(
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.getUser(
            userId = userId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun banForOneDay(
        userId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.banUserFromPosting(
            userId = userId,
            banType =
                AdminModerationRepository.BAN_ONE_DAY,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun banForOneWeek(
        userId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.banUserFromPosting(
            userId = userId,
            banType =
                AdminModerationRepository.BAN_ONE_WEEK,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun banForOneMonth(
        userId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.banUserFromPosting(
            userId = userId,
            banType =
                AdminModerationRepository.BAN_ONE_MONTH,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun banPermanently(
        userId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.banUserFromPosting(
            userId = userId,
            banType =
                AdminModerationRepository.BAN_PERMANENT,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun removePostingBan(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        adminModerationRepository.removePostingBan(
            userId = userId,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}