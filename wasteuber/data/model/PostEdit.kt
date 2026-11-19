package hu.wasteuber.app.data.model

data class PostEdit(
    val id: String = "",

    // Melyik poszthoz tartozik
    val postId: String = "",

    // Ki végezte a módosítást
    val editedBy: String = "",

    // Szöveg módosítása
    val oldText: String = "",
    val newText: String = "",

    // Média módosítása
    val oldMediaUrl: String = "",
    val newMediaUrl: String = "",

    val oldMediaType: String = "NONE",
    val newMediaType: String = "NONE",

    // Mikor történt a szerkesztés
    val editedAt: Long = 0L
)