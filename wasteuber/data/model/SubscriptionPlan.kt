package hu.wasteuber.app.data.model

data class SubscriptionPlan(
    val packageCode: String,
    val name: String,
    val tag: String,
    val mentionCode: String,
    val weeklyPriceFt: Int,
    val monthlyPriceFt: Int
)

object SubscriptionPlans {

    const val PACKAGE_NONE = "NONE"

    const val PACKAGE_WASTE_GO = "WASTE_GO"
    const val PACKAGE_WASTE_PLUS = "WASTE_PLUS"
    const val PACKAGE_WASTE_MAX = "WASTE_MAX"

    const val PERIOD_NONE = "NONE"
    const val PERIOD_WEEKLY = "WEEKLY"
    const val PERIOD_MONTHLY = "MONTHLY"

    const val MENTION_WASTE_GO =
        "SUBSCRIPTION_WASTE_GO"

    const val MENTION_WASTE_PLUS =
        "SUBSCRIPTION_WASTE_PLUS"

    const val MENTION_WASTE_MAX =
        "SUBSCRIPTION_WASTE_MAX"

    const val SUBSCRIPTION_REPUTATION_REWARD =
        50

    val wasteGo =
        SubscriptionPlan(
            packageCode =
                PACKAGE_WASTE_GO,

            name =
                "WasteGo",

            tag =
                "@wastego",

            mentionCode =
                MENTION_WASTE_GO,

            weeklyPriceFt =
                600,

            monthlyPriceFt =
                1600
        )

    val wastePlus =
        SubscriptionPlan(
            packageCode =
                PACKAGE_WASTE_PLUS,

            name =
                "WastePlus",

            tag =
                "@wasteplus",

            mentionCode =
                MENTION_WASTE_PLUS,

            weeklyPriceFt =
                1200,

            monthlyPriceFt =
                2200
        )

    val wasteMax =
        SubscriptionPlan(
            packageCode =
                PACKAGE_WASTE_MAX,

            name =
                "WasteMax",

            tag =
                "@wastemax",

            mentionCode =
                MENTION_WASTE_MAX,

            weeklyPriceFt =
                2400,

            monthlyPriceFt =
                4400
        )

    val allPlans: List<SubscriptionPlan> =
        listOf(
            wasteGo,
            wastePlus,
            wasteMax
        )

    fun getPlan(
        packageCode: String
    ): SubscriptionPlan? =
        allPlans.find {
            it.packageCode ==
                    packageCode
        }

    fun getPlanByMentionCode(
        mentionCode: String
    ): SubscriptionPlan? =
        allPlans.find {
            it.mentionCode ==
                    mentionCode
        }

    fun getPlanByTag(
        tag: String
    ): SubscriptionPlan? =
        allPlans.find {
            it.tag.equals(
                tag,
                ignoreCase = true
            )
        }

    fun getPrice(
        packageCode: String,
        period: String
    ): Int? {

        val plan =
            getPlan(
                packageCode
            )
                ?: return null

        return when (period) {

            PERIOD_WEEKLY ->
                plan.weeklyPriceFt

            PERIOD_MONTHLY ->
                plan.monthlyPriceFt

            else ->
                null
        }
    }

    fun getPackageName(
        packageCode: String
    ): String =
        getPlan(
            packageCode
        )
            ?.name
            ?: "Nincs"

    fun getPackageTag(
        packageCode: String
    ): String =
        getPlan(
            packageCode
        )
            ?.tag
            ?: "-"

    fun getMentionCode(
        packageCode: String
    ): String =
        getPlan(
            packageCode
        )
            ?.mentionCode
            ?: ""

    fun getPeriodName(
        period: String
    ): String =
        when (period) {

            PERIOD_WEEKLY ->
                "Heti"

            PERIOD_MONTHLY ->
                "Havi"

            else ->
                "Nincs"
        }

    fun isValidPackage(
        packageCode: String
    ): Boolean =
        getPlan(
            packageCode
        ) != null

    fun isValidPeriod(
        period: String
    ): Boolean =
        period ==
                PERIOD_WEEKLY ||
                period ==
                PERIOD_MONTHLY

    fun isSubscriptionMention(
        mentionCode: String
    ): Boolean =
        mentionCode ==
                MENTION_WASTE_GO ||
                mentionCode ==
                MENTION_WASTE_PLUS ||
                mentionCode ==
                MENTION_WASTE_MAX
}