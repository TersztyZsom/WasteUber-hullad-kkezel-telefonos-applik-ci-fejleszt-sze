package hu.wasteuber.app.data.model

data class WasteRequest(

    val id: String = "",

    /*
     * A felhasználó, aki az elszállítást kérte.
     */
    val customerId: String = "",
    val customerName: String = "",

    /*
     * Régi mező kompatibilitás miatt
     * egyelőre megmarad.
     *
     * Később az acceptedBy mezőt fogjuk
     * elsődlegesen használni.
     */
    val contractorId: String = "",

    /*
     * Hulladék adatai.
     */
    val wasteType: String = "",
    val amount: Double = 0.0,
    val address: String = "",
    val description: String = "",
    val imageUrl: String = "",

    /*
     * Az igény aktuális állapota.
     *
     * available
     * -> még senki nem fogadta el
     *
     * accepted
     * -> egy WasteUberes / WasteUber+ / Admin elfogadta
     *
     * rejected
     * -> admin elutasította
     *
     * completed
     * -> az elszállítás teljesítve
     */
    val status: String = STATUS_OPEN,

    /*
     * Igény létrehozásának időpontja.
     */
    val createdAt: Long = 0L,

    /*
     * ELFOGADÁS
     *
     * Elfogadhatja:
     * - ADMIN
     * - CONTRACTOR / WasteUberes
     * - WasteUber+
     */
    val acceptedBy: String = "",
    val acceptedByName: String = "",
    val acceptedAt: Long = 0L,

    /*
     * ELUTASÍTÁS
     *
     * Kizárólag ADMIN hajthatja végre.
     */
    val rejectedBy: String = "",
    val rejectedByName: String = "",
    val rejectedAt: Long = 0L,
    val rejectionReason: String = "",

    /*
     * Teljesítés időpontja.
     */
    val completedAt: Long = 0L

) {

    companion object {

        /*
         * Az értékeket egyelőre a jelenlegi
         * rendszerrel kompatibilis formában
         * tartjuk.
         */

        const val STATUS_OPEN =
            "available"

        const val STATUS_ACCEPTED =
            "accepted"

        const val STATUS_REJECTED =
            "rejected"

        const val STATUS_COMPLETED =
            "completed"
    }
}