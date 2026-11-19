package hu.wasteuber.app.data.model

data class Vehicle(
    val id: String = "",
    val ownerId: String = "",
    val brand: String = "",
    val model: String = "",
    val licensePlate: String = "",
    val vehicleType: String = "",
    val maxLoadKg: Int = 0,
    val hasTrailer: Boolean = false
)