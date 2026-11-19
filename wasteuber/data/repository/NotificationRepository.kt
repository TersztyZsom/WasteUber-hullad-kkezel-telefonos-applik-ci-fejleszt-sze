package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.Notification

class NotificationRepository {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    companion object {

        private const val NOTIFICATIONS_COLLECTION =
            "notifications"
    }

    // -------------------------------------------------------------------------
    // REFERENCES
    // -------------------------------------------------------------------------

    private fun currentUserId(): String? =
        auth.currentUser?.uid

    private fun notificationsCollection() =
        firestore.collection(
            NOTIFICATIONS_COLLECTION
        )

    private fun notificationRef(
        notificationId: String
    ): DocumentReference =
        notificationsCollection()
            .document(
                notificationId
            )

    /*
     * Egy eseményhez ugyanannál a felhasználónál
     * mindig ugyanazt a dokumentumazonosítót készítjük.
     *
     * Példa:
     *
     * POST_MENTION + postId + userId
     *
     * Így hiába kerül ugyanaz a user több role vagy
     * előfizetési csoport miatt is a címzettek közé,
     * ugyanabból az eseményből nem jöhet létre
     * több értesítése.
     */
    private fun uniqueNotificationRef(
        userId: String,
        type: String,
        relatedId: String
    ): DocumentReference {

        val safeUserId =
            userId.replace(
                "/",
                "_"
            )

        val safeType =
            type.replace(
                "/",
                "_"
            )

        val safeRelatedId =
            relatedId.replace(
                "/",
                "_"
            )

        val documentId =
            "${safeType}_${safeRelatedId}_${safeUserId}"

        return notificationsCollection()
            .document(
                documentId
            )
    }

    private fun errorMessage(
        exception: Exception,
        fallback: String
    ): String =
        exception.message
            ?: fallback

    // -------------------------------------------------------------------------
    // CREATE NOTIFICATION
    // -------------------------------------------------------------------------

    fun createNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        relatedId: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanedUserId =
            userId.trim()

        val cleanedType =
            type.trim()

        val cleanedTitle =
            title.trim()

        val cleanedMessage =
            message.trim()

        val cleanedRelatedId =
            relatedId.trim()

        if (
            cleanedUserId.isBlank()
        ) {

            onError(
                "Az értesítés címzettje hiányzik."
            )

            return
        }

        if (
            cleanedType.isBlank()
        ) {

            onError(
                "Az értesítés típusa hiányzik."
            )

            return
        }

        if (
            cleanedTitle.isBlank()
        ) {

            onError(
                "Az értesítés címe hiányzik."
            )

            return
        }

        if (
            cleanedMessage.isBlank()
        ) {

            onError(
                "Az értesítés szövege hiányzik."
            )

            return
        }

        /*
         * Ha van relatedId, akkor az értesítés
         * egy konkrét eseményhez kapcsolódik.
         *
         * Ebben az esetben determinisztikus
         * dokumentumazonosítót használunk.
         *
         * Ez biztosítja:
         *
         * ugyanaz a user
         * + ugyanaz a type
         * + ugyanaz a relatedId
         *
         * = pontosan 1 értesítés.
         *
         * Ha nincs relatedId, megtartjuk a hagyományos
         * automatikus Firestore dokumentumazonosítót.
         */
        val reference =
            if (
                cleanedRelatedId.isNotBlank()
            ) {

                uniqueNotificationRef(
                    userId =
                        cleanedUserId,

                    type =
                        cleanedType,

                    relatedId =
                        cleanedRelatedId
                )

            } else {

                notificationsCollection()
                    .document()
            }

        val notification =
            Notification(
                id =
                    reference.id,

                userId =
                    cleanedUserId,

                type =
                    cleanedType,

                title =
                    cleanedTitle,

                message =
                    cleanedMessage,

                relatedId =
                    cleanedRelatedId,

                createdAt =
                    System.currentTimeMillis(),

                read =
                    false
            )

        /*
         * Ha nincs relatedId, nincs szükség
         * eseményalapú duplikációvédelemre.
         */
        if (
            cleanedRelatedId.isBlank()
        ) {

            reference
                .set(
                    notification
                )
                .addOnSuccessListener {

                    onSuccess()
                }
                .addOnFailureListener { exception ->

                    onError(
                        errorMessage(
                            exception,
                            "Nem sikerült létrehozni az értesítést."
                        )
                    )
                }

            return
        }

        /*
         * relatedId esetén tranzakcióban ellenőrizzük,
         * hogy létezik-e már ugyanaz az értesítés.
         *
         * Ha már létezik:
         * nem hozunk létre újat,
         * nem állítjuk újra olvasatlanra,
         * nem módosítjuk az időpontját.
         */
        firestore
            .runTransaction { transaction ->

                val existingSnapshot =
                    transaction.get(
                        reference
                    )

                if (
                    !existingSnapshot.exists()
                ) {

                    transaction.set(
                        reference,
                        notification
                    )
                }
            }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült létrehozni az értesítést."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // GET MY NOTIFICATIONS
    // -------------------------------------------------------------------------

    fun getMyNotifications(
        onSuccess: (List<Notification>) -> Unit,
        onError: (String) -> Unit
    ) {

        val userId =
            currentUserId()
                ?: run {

                    onError(
                        "Nincs bejelentkezett felhasználó."
                    )

                    return
                }

        /*
         * Nem használunk itt orderBy-t,
         * így jelenleg nem kell külön
         * Firestore composite index.
         *
         * A rendezést Kotlinban végezzük.
         */
        notificationsCollection()
            .whereEqualTo(
                "userId",
                userId
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val notifications =
                    snapshot.documents
                        .mapNotNull { document ->

                            document
                                .toObject(
                                    Notification::class.java
                                )
                                ?.copy(
                                    id =
                                        document.id
                                )
                        }
                        .sortedByDescending {
                            it.createdAt
                        }

                onSuccess(
                    notifications
                )
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni az értesítéseket."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // MARK AS READ
    // -------------------------------------------------------------------------

    fun markAsRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val userId =
            currentUserId()
                ?: run {

                    onError(
                        "Nincs bejelentkezett felhasználó."
                    )

                    return
                }

        val cleanedNotificationId =
            notificationId.trim()

        if (
            cleanedNotificationId.isBlank()
        ) {

            onError(
                "Az értesítés azonosítója hiányzik."
            )

            return
        }

        val reference =
            notificationRef(
                cleanedNotificationId
            )

        firestore
            .runTransaction { transaction ->

                val snapshot =
                    transaction.get(
                        reference
                    )

                val notification =
                    snapshot
                        .toObject(
                            Notification::class.java
                        )
                        ?: throw Exception(
                            "Az értesítés nem található."
                        )

                /*
                 * Más felhasználó értesítését
                 * kliensoldalról sem engedjük
                 * olvasottra állítani.
                 */
                if (
                    notification.userId !=
                    userId
                ) {

                    throw Exception(
                        "Ehhez az értesítéshez nincs jogosultságod."
                    )
                }

                if (
                    !notification.read
                ) {

                    transaction.update(
                        reference,
                        "read",
                        true
                    )
                }
            }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült olvasottra állítani az értesítést."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // MARK ALL AS READ
    // -------------------------------------------------------------------------

    fun markAllAsRead(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val userId =
            currentUserId()
                ?: run {

                    onError(
                        "Nincs bejelentkezett felhasználó."
                    )

                    return
                }

        notificationsCollection()
            .whereEqualTo(
                "userId",
                userId
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val unreadDocuments =
                    snapshot.documents
                        .filter { document ->

                            document
                                .toObject(
                                    Notification::class.java
                                )
                                ?.read == false
                        }

                if (
                    unreadDocuments.isEmpty()
                ) {

                    onSuccess()
                    return@addOnSuccessListener
                }

                val batch =
                    firestore.batch()

                unreadDocuments
                    .forEach { document ->

                        batch.update(
                            document.reference,
                            "read",
                            true
                        )
                    }

                batch
                    .commit()
                    .addOnSuccessListener {

                        onSuccess()
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            errorMessage(
                                exception,
                                "Nem sikerült olvasottra állítani az értesítéseket."
                            )
                        )
                    }
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni az értesítéseket."
                    )
                )
            }
    }
}