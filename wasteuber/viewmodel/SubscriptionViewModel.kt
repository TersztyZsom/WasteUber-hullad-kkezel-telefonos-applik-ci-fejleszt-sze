package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.repository.SubscriptionRepository

class SubscriptionViewModel : ViewModel() {

    private val subscriptionRepository =
        SubscriptionRepository()

    /**
     * TESZT / fejlesztői aktiválás.
     *
     * Ez a jelenlegi SubscriptionScreen működését
     * tartja meg.
     *
     * A repository automatikusan generál hozzá
     * egy teszt tranzakcióazonosítót.
     */
    fun activateSubscription(
        packageCode: String,
        period: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        subscriptionRepository.activateSubscription(
            packageCode = packageCode,
            period = period,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * VALÓDI fizetés utáni aktiválás.
     *
     * Ezt majd akkor használjuk, amikor
     * a fizetési szolgáltatótól megkapjuk
     * az ellenőrzött tranzakcióazonosítót.
     *
     * Ugyanaz a transactionId a repository
     * oldalán csak egyszer dolgozható fel.
     */
    fun activateSubscription(
        packageCode: String,
        period: String,
        transactionId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        subscriptionRepository.activateSubscription(
            packageCode = packageCode,
            period = period,
            transactionId = transactionId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun cancelSubscription(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        subscriptionRepository.cancelSubscription(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun checkSubscription(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        subscriptionRepository.checkSubscription(
            onSuccess = onSuccess,
            onError = onError
        )
    }
}