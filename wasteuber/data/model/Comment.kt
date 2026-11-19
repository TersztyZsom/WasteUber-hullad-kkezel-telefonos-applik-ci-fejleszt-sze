package hu.wasteuber.app.data.model

data class Comment(
    val id: String = "",

    val userId: String = "",
    val userName: String = "",

    val text: String = "",

    val createdAt: Long = 0L,

    /*
     * Ha ez üres, akkor ez egy normál,
     * felső szintű komment.
     *
     * Ha van benne ID, akkor ez a komment
     * egy másik kommentre adott válasz.
     */
    val parentCommentId: String = "",

    /*
     * Komment like-ok száma.
     */
    val likeCount: Int = 0,

    /*
     * Közvetlen válaszok száma.
     */
    val replyCount: Int = 0
)