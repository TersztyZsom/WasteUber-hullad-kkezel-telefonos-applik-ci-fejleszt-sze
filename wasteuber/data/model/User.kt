package hu.wasteuber.app.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",

    val role: String = "CUSTOMER",

    val contractorApplicationStatus: String = "NONE",

    val isUberPlus: Boolean = false,

    /*
     * Reputációs pontok.
     *
     * Like: +1 pont
     * Komment: +1 pont
     * Sikeres előfizetés: +50 pont
     *
     * Posztoláskor NEM fogy.
     */
    val reputationPoints: Int = 0,

    /*
     * Igaz akkor, ha a felhasználónak már volt
     * legalább egy moderátor által jóváhagyott posztja.
     *
     * Amíg false:
     * minden új poszt PENDING lesz,
     * függetlenül a pontszámtól.
     */
    val firstPostApproved: Boolean = false,

    /*
     * Előfizetési csomag.
     *
     * NONE
     * WASTE_GO
     * WASTE_PLUS
     * WASTE_MAX
     */
    val subscriptionPackage: String = "NONE",

    /*
     * Előfizetési időszak.
     *
     * NONE
     * WEEKLY
     * MONTHLY
     */
    val subscriptionPeriod: String = "NONE",

    /*
     * Aktív-e jelenleg az előfizetés.
     */
    val subscriptionActive: Boolean = false,

    /*
     * Előfizetés kezdete.
     */
    val subscriptionStartAt: Long = 0L,

    /*
     * Előfizetés lejárata.
     */
    val subscriptionEndAt: Long = 0L,

    /*
     * Posztolási tiltás.
     *
     * 0 = nincs aktív tiltás
     * Long.MAX_VALUE = végleges tiltás
     * egyébként lejárati timestamp
     */
    val postBanUntil: Long = 0L,

    val postBanCount: Int = 0,

    val postBanReason: String = "",

    val postBannedBy: String = "",

    val postBannedByName: String = "",

    val postBannedAt: Long = 0L,

    /*
     * CONTRACTOR napi @ügyfelek mention követés.
     *
     * Ez külön limit a csomagtageléstől.
     */
    val contractorCustomerMentionDate: String = "",

    val contractorCustomerMentionCount: Int = 0,

    /*
     * Előfizetési csomagok tagelési limitje.
     *
     * @wastego
     * @wasteplus
     * @wastemax
     *
     * ADMIN:
     * korlátlan
     *
     * CONTRACTOR:
     * összesen maximum napi 2 alkalom
     *
     * WasteUber+:
     * összesen maximum napi 2 alkalom
     *
     * Példa:
     *
     * @wastego egyszer
     * +
     * @wastemax egyszer
     *
     * = 2 / 2 napi használat
     */
    val subscriptionMentionDate: String = "",

    val subscriptionMentionCount: Int = 0
)