package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.Notification
import hu.wasteuber.app.data.repository.NotificationRepository

class NotificationViewModel : ViewModel() {

    private val notificationRepository =
        NotificationRepository()

    fun createNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        relatedId: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        notificationRepository.createNotification(
            userId = userId,
            type = type,
            title = title,
            message = message,
            relatedId = relatedId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getMyNotifications(
        onSuccess: (List<Notification>) -> Unit,
        onError: (String) -> Unit
    ) {

        notificationRepository.getMyNotifications(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun markAsRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        notificationRepository.markAsRead(
            notificationId = notificationId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun markAllAsRead(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        notificationRepository.markAllAsRead(
            onSuccess = onSuccess,
            onError = onError
        )
    }
}