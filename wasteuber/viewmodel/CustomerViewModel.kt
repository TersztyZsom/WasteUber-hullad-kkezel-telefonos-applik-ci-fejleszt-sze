package hu.wasteuber.app.viewmodel

import androidx.lifecycle.ViewModel
import hu.wasteuber.app.data.repository.WasteRequestRepository

class CustomerViewModel : ViewModel() {

    private val wasteRequestRepository = WasteRequestRepository()

}
