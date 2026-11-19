package hu.wasteuber.app.data.model

data class ContractorApplication(
    val userId: String = "",
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val licensePlate: String = "",
    val vehicleType: String = "",
    val maxLoadKg: Int = 0,
    val hasTrailer: Boolean = false,
    val status: String = "PENDING"
)