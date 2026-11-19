package hu.wasteuber.app.data.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val relatedId: String = "",
    val createdAt: Long = 0L,
    val read: Boolean = false
) {

    companion object {

        // ---------------------------------------------------------------------
        // WASTE REQUEST
        // ---------------------------------------------------------------------

        const val TYPE_WASTE_REQUEST_REJECTED =
            "WASTE_REQUEST_REJECTED"

        const val TYPE_WASTE_REQUEST_ACCEPTED =
            "WASTE_REQUEST_ACCEPTED"

        const val TYPE_WASTE_REQUEST_COMPLETED =
            "WASTE_REQUEST_COMPLETED"

        // ---------------------------------------------------------------------
        // POSTS
        // ---------------------------------------------------------------------

        /*
         * Akkor kapja a felhasználó, ha egy poszt
         * valamelyik olyan csoportot/csomagot megjelöli,
         * amelybe ő beletartozik.
         *
         * Fontos:
         * egy posztról ugyanaz a user csak EGYSZER
         * kaphat ilyen értesítést akkor is, ha több
         * megjelölés is vonatkozik rá.
         */
        const val TYPE_POST_MENTION =
            "POST_MENTION"

        /*
         * Akkor kapja a poszt tulajdonosa,
         * ha ADMIN törli a posztját.
         */
        const val TYPE_POST_DELETED_BY_ADMIN =
            "POST_DELETED_BY_ADMIN"
    }
}