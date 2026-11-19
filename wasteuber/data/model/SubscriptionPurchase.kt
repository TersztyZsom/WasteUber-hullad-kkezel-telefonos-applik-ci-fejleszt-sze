package hu.wasteuber.app.data.model

data class SubscriptionPurchase(
    val id: String = "",
    val userId: String = "",
    val transactionId: String = "",
    val packageCode: String = SubscriptionPlans.PACKAGE_NONE,
    val period: String = SubscriptionPlans.PERIOD_NONE,
    val priceFt: Int = 0,
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L,
    val verifiedAt: Long = 0L,
    val subscriptionStartAt: Long = 0L,
    val subscriptionEndAt: Long = 0L,
    val reputationRewardApplied: Boolean = false
) {

    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PAID = "PAID"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_REFUNDED = "REFUNDED"
    }
}