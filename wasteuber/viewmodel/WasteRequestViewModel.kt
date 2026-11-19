package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.WasteRequest
import hu.wasteuber.app.data.repository.WasteRequestRepository

class WasteRequestViewModel : ViewModel() {

    private val wasteRequestRepository =
        WasteRequestRepository()

    // -------------------------------------------------------------------------
    // CREATE REQUEST
    // -------------------------------------------------------------------------

    fun createWasteRequest(
        wasteType: String,
        amount: Double,
        address: String,
        description: String,
        imageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.createWasteRequest(
            wasteType = wasteType,
            amount = amount,
            address = address,
            description = description,
            imageUrl = imageUrl,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // MY REQUESTS
    // -------------------------------------------------------------------------

    fun getMyRequests(
        onSuccess: (List<WasteRequest>) -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.getMyRequests(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // AVAILABLE REQUESTS
    // -------------------------------------------------------------------------

    fun getAvailableRequests(
        onSuccess: (List<WasteRequest>) -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.getAvailableRequests(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // ACTIVE / ACCEPTED REQUESTS
    // -------------------------------------------------------------------------

    fun getActiveRequests(
        onSuccess: (List<WasteRequest>) -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.getActiveRequests(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // ACCEPT REQUEST
    // -------------------------------------------------------------------------

    fun acceptWasteRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.acceptWasteRequest(
            requestId = requestId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // ADMIN REJECT REQUEST
    // -------------------------------------------------------------------------

    fun rejectWasteRequest(
        requestId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.rejectWasteRequest(
            requestId = requestId,
            reason = reason,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // COMPLETE REQUEST
    // -------------------------------------------------------------------------

    fun completeWasteRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        wasteRequestRepository.completeWasteRequest(
            requestId = requestId,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}