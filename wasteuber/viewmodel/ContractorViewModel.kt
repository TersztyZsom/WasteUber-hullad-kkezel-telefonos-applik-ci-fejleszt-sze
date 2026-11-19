package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.model.ContractorApplication
import hu.wasteuber.app.data.repository.ContractorApplicationRepository

class ContractorViewModel : ViewModel() {

    private val contractorApplicationRepository =
        ContractorApplicationRepository()

    fun submitApplication(
        application: ContractorApplication,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        contractorApplicationRepository.submitApplication(
            application = application,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getCurrentUserApplication(
        onSuccess: (ContractorApplication?) -> Unit,
        onError: (String) -> Unit
    ) {
        contractorApplicationRepository.getCurrentUserApplication(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getPendingApplications(
        onSuccess: (List<ContractorApplication>) -> Unit,
        onError: (String) -> Unit
    ) {
        contractorApplicationRepository.getPendingApplications(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun approveApplication(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        contractorApplicationRepository.approveApplication(
            userId = userId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun rejectApplication(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        contractorApplicationRepository.rejectApplication(
            userId = userId,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}