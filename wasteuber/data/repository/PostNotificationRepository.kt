package hu.wasteuber.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.Notification
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.data.model.User

class PostNotificationRepository {

    private val firestore =
        FirebaseFirestore.getInstance()

    private val notificationRepository =
        NotificationRepository()

    companion object {

        private const val USERS_COLLECTION =
            "users"

        private const val ROLE_CUSTOMER =
            "CUSTOMER"

        private const val ROLE_CONTRACTOR =
            "CONTRACTOR"

        private const val ROLE_MENTION_CUSTOMERS =
            "CUSTOMERS"

        private const val ROLE_MENTION_CONTRACTORS =
            "CONTRACTORS"
    }

    // -------------------------------------------------------------------------
    // POST MENTION NOTIFICATIONS
    // -------------------------------------------------------------------------

    fun sendPostMentionNotifications(
        postId: String,
        authorId: String,
        authorName: String,
        roleMentions: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val cleanedPostId =
            postId.trim()

        val cleanedAuthorId =
            authorId.trim()

        if (
            cleanedPostId.isBlank() ||
            roleMentions.isEmpty()
        ) {

            onSuccess()
            return
        }

        firestore
            .collection(
                USERS_COLLECTION
            )
            .get()
            .addOnSuccessListener { snapshot ->

                /*
                 * SET-et használunk.
                 *
                 * Ha ugyanaz a felhasználó egyszerre:
                 *
                 * - WasteUberes
                 * - WasteUber+
                 * - WasteGo / WastePlus / WasteMax előfizető
                 *
                 * és több rá vonatkozó tag is szerepel
                 * ugyanabban a posztban, a userId akkor
                 * is csak EGYSZER kerül bele.
                 */
                val recipientUserIds =
                    mutableSetOf<String>()

                snapshot.documents
                    .forEach { document ->

                        val user =
                            document
                                .toObject(
                                    User::class.java
                                )
                                ?.copy(
                                    id =
                                        document.id
                                )
                                ?: return@forEach

                        /*
                         * A poszt szerzőjének nem küldünk
                         * értesítést a saját megjelöléséről.
                         */
                        if (
                            user.id ==
                            cleanedAuthorId
                        ) {

                            return@forEach
                        }

                        // -----------------------------------------------------
                        // @ügyfelek
                        // -----------------------------------------------------

                        if (
                            ROLE_MENTION_CUSTOMERS
                            in roleMentions &&
                            user.role ==
                            ROLE_CUSTOMER
                        ) {

                            recipientUserIds.add(
                                user.id
                            )
                        }

                        // -----------------------------------------------------
                        // @wasteuberesek
                        // -----------------------------------------------------

                        if (
                            ROLE_MENTION_CONTRACTORS
                            in roleMentions &&
                            user.role ==
                            ROLE_CONTRACTOR
                        ) {

                            recipientUserIds.add(
                                user.id
                            )
                        }

                        // -----------------------------------------------------
                        // @wastego
                        // -----------------------------------------------------

                        if (
                            SubscriptionPlans
                                .MENTION_WASTE_GO
                            in roleMentions &&
                            hasActiveSubscription(
                                user =
                                    user,
                                packageCode =
                                    SubscriptionPlans
                                        .PACKAGE_WASTE_GO
                            )
                        ) {

                            recipientUserIds.add(
                                user.id
                            )
                        }

                        // -----------------------------------------------------
                        // @wasteplus
                        // -----------------------------------------------------

                        /*
                         * Ez a WastePlus ELŐFIZETÉS.
                         *
                         * Nem azonos:
                         *
                         * user.isUberPlus
                         *
                         * mezővel.
                         */
                        if (
                            SubscriptionPlans
                                .MENTION_WASTE_PLUS
                            in roleMentions &&
                            hasActiveSubscription(
                                user =
                                    user,
                                packageCode =
                                    SubscriptionPlans
                                        .PACKAGE_WASTE_PLUS
                            )
                        ) {

                            recipientUserIds.add(
                                user.id
                            )
                        }

                        // -----------------------------------------------------
                        // @wastemax
                        // -----------------------------------------------------

                        if (
                            SubscriptionPlans
                                .MENTION_WASTE_MAX
                            in roleMentions &&
                            hasActiveSubscription(
                                user =
                                    user,
                                packageCode =
                                    SubscriptionPlans
                                        .PACKAGE_WASTE_MAX
                            )
                        ) {

                            recipientUserIds.add(
                                user.id
                            )
                        }
                    }

                if (
                    recipientUserIds.isEmpty()
                ) {

                    onSuccess()
                    return@addOnSuccessListener
                }

                createMentionNotifications(
                    recipientUserIds =
                        recipientUserIds,

                    postId =
                        cleanedPostId,

                    authorName =
                        authorName,

                    onSuccess =
                        onSuccess,

                    onError =
                        onError
                )
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült meghatározni az értesítés címzettjeit."
                )
            }
    }

    // -------------------------------------------------------------------------
    // CREATE NOTIFICATIONS
    // -------------------------------------------------------------------------

    private fun createMentionNotifications(
        recipientUserIds: Set<String>,
        postId: String,
        authorName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        var remainingCount =
            recipientUserIds.size

        var firstErrorMessage: String? =
            null

        val displayAuthorName =
            authorName
                .trim()
                .ifBlank {
                    "Egy felhasználó"
                }

        fun notificationFinished(
            error: String? = null
        ) {

            if (
                error != null &&
                firstErrorMessage == null
            ) {

                firstErrorMessage =
                    error
            }

            remainingCount--

            if (
                remainingCount > 0
            ) {

                return
            }

            val finalError =
                firstErrorMessage

            if (
                finalError == null
            ) {

                onSuccess()

            } else {

                onError(
                    finalError
                )
            }
        }

        recipientUserIds
            .forEach { recipientUserId ->

                /*
                 * A NotificationRepository a:
                 *
                 * type + relatedId + userId
                 *
                 * alapján deduplikál.
                 *
                 * Ezért a Set mellett van egy második
                 * védelem is a duplikált értesítések ellen.
                 */
                notificationRepository
                    .createNotification(

                        userId =
                            recipientUserId,

                        type =
                            Notification
                                .TYPE_POST_MENTION,

                        title =
                            "Új csoportos megjelölés",

                        message =
                            "$displayAuthorName olyan csoportot jelölt meg egy bejegyzésben, amelyhez tartozol.",

                        relatedId =
                            postId,

                        onSuccess = {

                            notificationFinished()
                        },

                        onError = { error ->

                            notificationFinished(
                                error =
                                    error
                            )
                        }
                    )
            }
    }

    // -------------------------------------------------------------------------
    // SUBSCRIPTION CHECK
    // -------------------------------------------------------------------------

    private fun hasActiveSubscription(
        user: User,
        packageCode: String
    ): Boolean {

        val now =
            System.currentTimeMillis()

        return user.subscriptionActive &&
                user.subscriptionPackage ==
                packageCode &&
                user.subscriptionEndAt >
                now
    }
}