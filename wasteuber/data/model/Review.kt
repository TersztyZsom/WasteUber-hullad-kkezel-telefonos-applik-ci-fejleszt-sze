package hu.wasteuber.app.data.model

data class Review(
    val id: String = "",
    val requestId: String = "",
    val reviewerId: String = "",
    val reviewedUserId: String = "",
    val rating: Int = 0,
    val comment: String = ""
)