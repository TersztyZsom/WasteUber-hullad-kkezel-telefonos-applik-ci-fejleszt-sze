package hu.wasteuber.app.data.model

data class Post(

    val id: String = "",

    val userId: String = "",
    val userName: String = "",

    val text: String = "",

    val mediaUrl: String = "",
    val mediaType: String = "NONE",

    val createdAt: Long = 0L,

    /*
     * FONTOS FIRESTORE:
     *
     * Boolean mező neve "is" előtaggal kezdődik,
     * ezért JvmField kell rá.
     *
     * Így Firestore-ban is pontosan:
     *
     * isEdited
     *
     * néven szerepel.
     */
    @field:JvmField
    val isEdited: Boolean = false,

    val editedAt: Long = 0L,

    val likeCount: Int = 0,
    val commentCount: Int = 0,

    /*
     * Poszt jóváhagyási állapota.
     *
     * APPROVED
     * -> a poszt megjelenhet a feedben
     *
     * PENDING
     * -> admin jóváhagyásra vár
     *
     * REJECTED
     * -> admin elutasította
     *
     * A régi posztok alapértelmezetten
     * APPROVED státuszt kapnak.
     */
    val approvalStatus: String = "APPROVED",

    /*
     * Az admin UID-ja, aki a posztot
     * jóváhagyta vagy elutasította.
     */
    val reviewedBy: String = "",

    /*
     * Az admin neve, aki a posztot
     * jóváhagyta vagy elutasította.
     */
    val reviewedByName: String = "",

    /*
     * A jóváhagyás / elutasítás időpontja.
     */
    val reviewedAt: Long = 0L,

    /*
     * Elutasítás oka.
     *
     * Jóváhagyott vagy még függő
     * posztnál üres.
     */
    val rejectionReason: String = "",

    /*
     * Régi mező.
     *
     * Jelenleg már nem használjuk
     * reputáció levonására vagy
     * posztolási kredit kezelésére,
     * de kompatibilitás miatt
     * egyelőre megmarad.
     */
    val usedPostCredit: Boolean = false,

    /*
     * FONTOS FIRESTORE:
     *
     * Boolean mező neve "is" előtaggal kezdődik,
     * ezért JvmField kell rá.
     *
     * Így Firestore-ban is pontosan:
     *
     * isDeleted
     *
     * néven szerepel.
     *
     * A PostRepository is ezt a mezőt
     * állítja true értékre törléskor.
     */
    @field:JvmField
    val isDeleted: Boolean = false,

    val deletedAt: Long = 0L,
    val deletedBy: String = "",
    val deletionReason: String = "",

    /*
     * Csoportos megjelölések.
     *
     * Lehetséges értékek például:
     *
     * CUSTOMERS
     * CONTRACTORS
     * SUBSCRIPTION_WASTE_GO
     * SUBSCRIPTION_WASTE_PLUS
     * SUBSCRIPTION_WASTE_MAX
     */
    val roleMentions: List<String> = emptyList()
)