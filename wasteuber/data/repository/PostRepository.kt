package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import hu.wasteuber.app.data.model.Comment
import hu.wasteuber.app.data.model.Notification
import hu.wasteuber.app.data.model.Post
import hu.wasteuber.app.data.model.PostEdit
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val postNotificationRepository =
        PostNotificationRepository()

    private val notificationRepository =
        NotificationRepository()

    companion object {
        private const val ROLE_ADMIN = "ADMIN"
        private const val ROLE_CONTRACTOR = "CONTRACTOR"

        private const val ROLE_MENTION_CUSTOMERS = "CUSTOMERS"
        private const val ROLE_MENTION_CONTRACTORS = "CONTRACTORS"

        private const val CUSTOMER_MENTION_TEXT = "@ügyfelek"
        private const val CONTRACTOR_MENTION_TEXT = "@wasteuberesek"

        private const val WASTE_GO_MENTION_TEXT = "@wastego"
        private const val WASTE_PLUS_MENTION_TEXT = "@wasteplus"
        private const val WASTE_MAX_MENTION_TEXT = "@wastemax"

        private const val CONTRACTOR_DAILY_MENTION_LIMIT = 2
        private const val SUBSCRIPTION_DAILY_MENTION_LIMIT = 2

        private const val AUTO_POST_POINT_LIMIT = 50

        private const val STATUS_APPROVED = "APPROVED"
        private const val STATUS_PENDING = "PENDING"
        private const val STATUS_REJECTED = "REJECTED"
    }

    // -------------------------------------------------------------------------
    // REFERENCES
    // -------------------------------------------------------------------------

    private fun currentUserId(): String? =
        auth.currentUser?.uid

    private fun userRef(userId: String): DocumentReference =
        firestore.collection("users").document(userId)

    private fun postRef(postId: String): DocumentReference =
        firestore.collection("posts").document(postId)

    private fun commentsRef(postId: String) =
        postRef(postId).collection("comments")

    private fun commentRef(
        postId: String,
        commentId: String
    ): DocumentReference =
        commentsRef(postId).document(commentId)

    private fun postLikeRef(
        postId: String,
        userId: String
    ): DocumentReference =
        postRef(postId)
            .collection("likes")
            .document(userId)

    /*
     * Ez a dokumentum like visszavonásakor is megmarad.
     * Így ugyanazért a posztért csak egyszer jár +1 pont.
     */
    private fun postLikeRewardRef(
        postId: String,
        userId: String
    ): DocumentReference =
        postRef(postId)
            .collection("likeRewards")
            .document(userId)

    private fun commentLikeRef(
        postId: String,
        commentId: String,
        userId: String
    ): DocumentReference =
        commentRef(postId, commentId)
            .collection("likes")
            .document(userId)

    private fun errorMessage(
        exception: Exception,
        fallback: String
    ): String =
        exception.message ?: fallback

    // -------------------------------------------------------------------------
    // CREATE POST
    // -------------------------------------------------------------------------

    fun createPost(
        text: String,
        mediaUrl: String,
        mediaType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val cleanedText = text.trim()

        if (
            cleanedText.isBlank() &&
            mediaUrl.isBlank()
        ) {
            onError("A poszt nem lehet teljesen üres.")
            return
        }

        val userReference = userRef(userId)
        val postReference =
            firestore.collection("posts").document()

        firestore.runTransaction { transaction ->

            val user =
                transaction.get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználói adatok nem olvashatók."
                    )

            validatePostBan(user)

            val roleMentions =
                getRoleMentions(cleanedText)

            validateRoleMentions(
                user = user,
                roleMentions = roleMentions
            )

            if (
                user.role == ROLE_CONTRACTOR &&
                ROLE_MENTION_CUSTOMERS in roleMentions
            ) {
                updateContractorMentionCounter(
                    transaction = transaction,
                    userReference = userReference,
                    user = user
                )
            }

            val subscriptionMentionCount =
                roleMentions.count {
                    SubscriptionPlans.isSubscriptionMention(it)
                }

            if (
                user.role != ROLE_ADMIN &&
                subscriptionMentionCount > 0
            ) {
                updateSubscriptionMentionCounter(
                    transaction = transaction,
                    userReference = userReference,
                    user = user,
                    amount = subscriptionMentionCount
                )
            }

            /*
             * Első jóváhagyott poszt előtt:
             * mindig PENDING.
             *
             * Utána ADMIN vagy 50+ reputáció:
             * automatikusan APPROVED.
             *
             * Posztoláskor reputációs pont NEM fogy.
             */
            val approvalStatus =
                when {
                    !user.firstPostApproved ->
                        STATUS_PENDING

                    user.role == ROLE_ADMIN ->
                        STATUS_APPROVED

                    user.reputationPoints >= AUTO_POST_POINT_LIMIT ->
                        STATUS_APPROVED

                    else ->
                        STATUS_PENDING
                }

            val post =
                Post(
                    id = postReference.id,
                    userId = userId,
                    userName =
                        user.name.ifBlank {
                            "Felhasználó"
                        },
                    text = cleanedText,
                    mediaUrl = mediaUrl,
                    mediaType = mediaType,
                    createdAt = System.currentTimeMillis(),
                    isEdited = false,
                    editedAt = 0L,
                    likeCount = 0,
                    commentCount = 0,
                    approvalStatus = approvalStatus,
                    reviewedBy = "",
                    reviewedByName = "",
                    reviewedAt = 0L,
                    rejectionReason = "",
                    usedPostCredit = false,
                    isDeleted = false,
                    deletedAt = 0L,
                    deletedBy = "",
                    deletionReason = "",
                    roleMentions = roleMentions
                )

            transaction.set(
                postReference,
                post
            )

            post
        }
            .addOnSuccessListener { createdPost ->

                if (
                    createdPost.approvalStatus == STATUS_APPROVED &&
                    createdPost.roleMentions.isNotEmpty()
                ) {

                    postNotificationRepository
                        .sendPostMentionNotifications(
                            postId = createdPost.id,
                            authorId = createdPost.userId,
                            authorName = createdPost.userName,
                            roleMentions = createdPost.roleMentions,
                            onSuccess = {
                                onSuccess()
                            },
                            onError = {
                                /*
                                 * A poszt ekkor már létrejött.
                                 * Értesítési hiba miatt nem jelezzük
                                 * sikertelennek a poszt létrehozását.
                                 */
                                onSuccess()
                            }
                        )

                } else {

                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült létrehozni a posztot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // POSTS
    // -------------------------------------------------------------------------

    fun getPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("posts")
            .orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val posts =
                    snapshot.documents
                        .mapNotNull { document ->
                            document
                                .toObject(Post::class.java)
                                ?.copy(id = document.id)
                        }
                        .filter {
                            !it.isDeleted &&
                                    it.approvalStatus == STATUS_APPROVED
                        }

                onSuccess(posts)
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a posztokat."
                    )
                )
            }
    }

    fun getMyPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->

                val posts =
                    snapshot.documents
                        .mapNotNull { document ->
                            document
                                .toObject(Post::class.java)
                                ?.copy(id = document.id)
                        }
                        .filter {
                            !it.isDeleted
                        }
                        .sortedByDescending {
                            it.createdAt
                        }

                onSuccess(posts)
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a saját posztokat."
                    )
                )
            }
    }

    fun getPost(
        postId: String,
        onSuccess: (Post) -> Unit,
        onError: (String) -> Unit
    ) {
        postRef(postId)
            .get()
            .addOnSuccessListener { document ->

                val post =
                    document
                        .toObject(Post::class.java)
                        ?.copy(id = document.id)

                when {
                    post == null ->
                        onError("A poszt nem található.")

                    post.isDeleted ->
                        onError("Ez a poszt törölve lett.")

                    else ->
                        onSuccess(post)
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a posztot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // ADMIN PENDING POSTS
    // -------------------------------------------------------------------------

    fun getPendingPosts(
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        val adminId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        checkAdmin(
            adminId = adminId,

            onSuccess = {

                firestore.collection("posts")
                    .whereEqualTo(
                        "approvalStatus",
                        STATUS_PENDING
                    )
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val posts =
                            snapshot.documents
                                .mapNotNull { document ->
                                    document
                                        .toObject(Post::class.java)
                                        ?.copy(id = document.id)
                                }
                                .filter {
                                    !it.isDeleted
                                }
                                .sortedByDescending {
                                    it.createdAt
                                }

                        onSuccess(posts)
                    }
                    .addOnFailureListener { exception ->
                        onError(
                            errorMessage(
                                exception,
                                "Nem sikerült lekérni a jóváhagyásra váró posztokat."
                            )
                        )
                    }
            },

            onError = onError
        )
    }

    fun approvePendingPost(
        postId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val adminId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val adminReference =
            userRef(adminId)

        val postReference =
            postRef(postId)

        firestore.runTransaction { transaction ->

            val admin =
                transaction.get(adminReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "Az admin felhasználó nem található."
                    )

            val post =
                transaction.get(postReference)
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt nem található."
                    )

            if (admin.role != ROLE_ADMIN) {
                throw Exception(
                    "Nincs admin jogosultságod."
                )
            }

            if (post.isDeleted) {
                throw Exception(
                    "Törölt poszt nem hagyható jóvá."
                )
            }

            if (
                post.approvalStatus != STATUS_PENDING
            ) {
                throw Exception(
                    "Ez a poszt már nincs jóváhagyásra váró állapotban."
                )
            }

            val postOwnerReference =
                userRef(post.userId)

            val postOwner =
                transaction.get(postOwnerReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A poszt szerzője nem található."
                    )

            transaction.update(
                postReference,
                mapOf(
                    "approvalStatus" to STATUS_APPROVED,
                    "reviewedBy" to adminId,
                    "reviewedByName" to
                            admin.name.ifBlank {
                                "Admin"
                            },
                    "reviewedAt" to
                            System.currentTimeMillis(),
                    "rejectionReason" to ""
                )
            )

            /*
             * Csak tényleges admin jóváhagyás után
             * tekintjük teljesítettnek az első posztot.
             */
            if (!postOwner.firstPostApproved) {
                transaction.update(
                    postOwnerReference,
                    "firstPostApproved",
                    true
                )
            }

            /*
             * Régebbi posztnál előfordulhat, hogy a
             * roleMentions mező még üres, ezért szükség
             * esetén a szövegből újra kiszámoljuk.
             */
            val effectiveRoleMentions =
                post.roleMentions.ifEmpty {
                    getRoleMentions(post.text)
                }

            post.copy(
                approvalStatus = STATUS_APPROVED,
                roleMentions = effectiveRoleMentions
            )
        }
            .addOnSuccessListener { approvedPost ->

                if (
                    approvedPost.roleMentions.isNotEmpty()
                ) {

                    postNotificationRepository
                        .sendPostMentionNotifications(
                            postId = approvedPost.id.ifBlank {
                                postId
                            },
                            authorId = approvedPost.userId,
                            authorName = approvedPost.userName,
                            roleMentions = approvedPost.roleMentions,
                            onSuccess = {
                                onSuccess()
                            },
                            onError = {
                                /*
                                 * A jóváhagyás ekkor már sikeresen
                                 * megtörtént. Értesítési hiba miatt
                                 * nem jelezzük hibásnak az approvalt.
                                 */
                                onSuccess()
                            }
                        )

                } else {

                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült jóváhagyni a posztot."
                    )
                )
            }
    }

    fun rejectPendingPost(
        postId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val adminId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val cleanedReason =
            reason.trim()

        if (cleanedReason.isBlank()) {
            onError(
                "Az elutasítás okát kötelező megadni."
            )
            return
        }

        val adminReference =
            userRef(adminId)

        val postReference =
            postRef(postId)

        firestore.runTransaction { transaction ->

            val admin =
                transaction.get(adminReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "Az admin felhasználó nem található."
                    )

            val post =
                transaction.get(postReference)
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt nem található."
                    )

            if (admin.role != ROLE_ADMIN) {
                throw Exception(
                    "Nincs admin jogosultságod."
                )
            }

            if (post.isDeleted) {
                throw Exception(
                    "Törölt poszt nem utasítható el."
                )
            }

            if (
                post.approvalStatus != STATUS_PENDING
            ) {
                throw Exception(
                    "Ez a poszt már nincs jóváhagyásra váró állapotban."
                )
            }

            transaction.update(
                postReference,
                mapOf(
                    "approvalStatus" to STATUS_REJECTED,
                    "reviewedBy" to adminId,
                    "reviewedByName" to
                            admin.name.ifBlank {
                                "Admin"
                            },
                    "reviewedAt" to
                            System.currentTimeMillis(),
                    "rejectionReason" to
                            cleanedReason
                )
            )

            /*
             * Elutasításkor firstPostApproved
             * nem változik.
             */
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült elutasítani a posztot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // EDIT POST
    // -------------------------------------------------------------------------

    fun editPost(
        postId: String,
        newText: String,
        newMediaUrl: String,
        newMediaType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val cleanedText =
            newText.trim()

        if (
            cleanedText.isBlank() &&
            newMediaUrl.isBlank()
        ) {
            onError(
                "A poszt nem lehet teljesen üres."
            )
            return
        }

        val userReference =
            userRef(userId)

        val postReference =
            postRef(postId)

        val editReference =
            postReference
                .collection("editHistory")
                .document()

        firestore.runTransaction { transaction ->

            val user =
                transaction.get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználói adatok nem olvashatók."
                    )

            val post =
                transaction.get(postReference)
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt adatai nem olvashatók."
                    )

            if (post.userId != userId) {
                throw Exception(
                    "Csak a saját posztodat szerkesztheted."
                )
            }

            if (post.isDeleted) {
                throw Exception(
                    "Törölt poszt nem szerkeszthető."
                )
            }

            val newRoleMentions =
                getRoleMentions(cleanedText)

            validateRoleMentions(
                user = user,
                roleMentions = newRoleMentions
            )

            val oldRoleMentions =
                post.roleMentions.ifEmpty {
                    getRoleMentions(post.text)
                }

            val addedCustomerMention =
                ROLE_MENTION_CUSTOMERS in newRoleMentions &&
                        ROLE_MENTION_CUSTOMERS !in oldRoleMentions

            if (
                user.role == ROLE_CONTRACTOR &&
                addedCustomerMention
            ) {
                updateContractorMentionCounter(
                    transaction = transaction,
                    userReference = userReference,
                    user = user
                )
            }

            /*
             * Szerkesztéskor csak az újonnan
             * hozzáadott csomagtagek számítanak.
             */
            val addedSubscriptionMentions =
                newRoleMentions
                    .filter {
                        SubscriptionPlans.isSubscriptionMention(it)
                    }
                    .filter {
                        it !in oldRoleMentions
                    }

            if (
                user.role != ROLE_ADMIN &&
                addedSubscriptionMentions.isNotEmpty()
            ) {
                updateSubscriptionMentionCounter(
                    transaction = transaction,
                    userReference = userReference,
                    user = user,
                    amount =
                        addedSubscriptionMentions.size
                )
            }

            val changed =
                cleanedText != post.text ||
                        newMediaUrl != post.mediaUrl ||
                        newMediaType != post.mediaType ||
                        newRoleMentions != post.roleMentions

            if (!changed) {
                throw Exception(
                    "Nem történt változtatás."
                )
            }

            val editedAt =
                System.currentTimeMillis()

            val edit =
                PostEdit(
                    id = editReference.id,
                    postId = postId,
                    editedBy = userId,
                    oldText = post.text,
                    newText = cleanedText,
                    oldMediaUrl = post.mediaUrl,
                    newMediaUrl = newMediaUrl,
                    oldMediaType = post.mediaType,
                    newMediaType = newMediaType,
                    editedAt = editedAt
                )

            transaction.set(
                editReference,
                edit
            )

            transaction.update(
                postReference,
                mapOf(
                    "text" to cleanedText,
                    "mediaUrl" to newMediaUrl,
                    "mediaType" to newMediaType,
                    "isEdited" to true,
                    "editedAt" to editedAt,
                    "roleMentions" to newRoleMentions
                )
            )
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült szerkeszteni a posztot."
                    )
                )
            }
    }

    fun getEditHistory(
        postId: String,
        onSuccess: (List<PostEdit>) -> Unit,
        onError: (String) -> Unit
    ) {
        postRef(postId)
            .collection("editHistory")
            .orderBy(
                "editedAt",
                Query.Direction.DESCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val edits =
                    snapshot.documents
                        .mapNotNull { document ->
                            document
                                .toObject(PostEdit::class.java)
                                ?.copy(id = document.id)
                        }

                onSuccess(edits)
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a szerkesztési előzményeket."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // DELETE POST
    // -------------------------------------------------------------------------

    fun deleteOwnPost(
        postId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val reference =
            postRef(postId)

        reference.get()
            .addOnSuccessListener { document ->

                val post =
                    document.toObject(Post::class.java)

                when {
                    post == null ->
                        onError(
                            "A poszt nem található."
                        )

                    post.userId != userId ->
                        onError(
                            "Csak a saját posztodat törölheted."
                        )

                    post.isDeleted ->
                        onError(
                            "A poszt már törölve lett."
                        )

                    else ->
                        reference.update(
                            mapOf(
                                "isDeleted" to true,
                                "deletedAt" to
                                        System.currentTimeMillis(),
                                "deletedBy" to userId,
                                "deletionReason" to
                                        "Felhasználó által törölve"
                            )
                        )
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                onError(
                                    errorMessage(
                                        exception,
                                        "Nem sikerült törölni a posztot."
                                    )
                                )
                            }
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a posztot."
                    )
                )
            }
    }

    fun adminDeletePost(
        postId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val adminId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val cleanedReason =
            reason.trim()

        if (cleanedReason.isBlank()) {
            onError(
                "A törlés okát kötelező megadni."
            )
            return
        }

        checkAdmin(
            adminId = adminId,

            onSuccess = {

                val reference =
                    postRef(postId)

                reference.get()
                    .addOnSuccessListener { document ->

                        val post =
                            document.toObject(Post::class.java)

                        when {
                            post == null ->
                                onError(
                                    "A poszt nem található."
                                )

                            post.isDeleted ->
                                onError(
                                    "A poszt már törölve lett."
                                )

                            else ->
                                reference.update(
                                    mapOf(
                                        "isDeleted" to true,
                                        "deletedAt" to
                                                System.currentTimeMillis(),
                                        "deletedBy" to adminId,
                                        "deletionReason" to
                                                cleanedReason
                                    )
                                )
                                    .addOnSuccessListener {

                                        /*
                                         * Ha az admin más felhasználó
                                         * posztját törölte, a tulajdonos
                                         * kap egy értesítést a törlés okával.
                                         *
                                         * A relatedId a postId, ezért a
                                         * NotificationRepository ugyanarról
                                         * a törlésről ugyanannak a usernek
                                         * nem tud duplikált értesítést létrehozni.
                                         */
                                        if (
                                            post.userId.isNotBlank() &&
                                            post.userId != adminId
                                        ) {

                                            notificationRepository
                                                .createNotification(
                                                    userId =
                                                        post.userId,
                                                    type =
                                                        Notification
                                                            .TYPE_POST_DELETED_BY_ADMIN,
                                                    title =
                                                        "A bejegyzésed törölve lett",
                                                    message =
                                                        "Egy admin törölte a bejegyzésedet. Ok: $cleanedReason",
                                                    relatedId =
                                                        post.id.ifBlank {
                                                            postId
                                                        },
                                                    onSuccess = {
                                                        onSuccess()
                                                    },
                                                    onError = {
                                                        /*
                                                         * Maga a törlés ekkor már
                                                         * sikeresen megtörtént.
                                                         * Értesítési hiba miatt nem
                                                         * jelezzük sikertelennek a törlést.
                                                         */
                                                        onSuccess()
                                                    }
                                                )

                                        } else {

                                            onSuccess()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        onError(
                                            errorMessage(
                                                exception,
                                                "Nem sikerült törölni a posztot."
                                            )
                                        )
                                    }
                        }
                    }
                    .addOnFailureListener { exception ->
                        onError(
                            errorMessage(
                                exception,
                                "Nem sikerült lekérni a posztot."
                            )
                        )
                    }
            },

            onError = onError
        )
    }

    // -------------------------------------------------------------------------
    // POST LIKES + REPUTATION
    // -------------------------------------------------------------------------

    fun isPostLiked(
        postId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        postLikeRef(
            postId,
            userId
        )
            .get()
            .addOnSuccessListener {
                onSuccess(it.exists())
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült ellenőrizni a like állapotát."
                    )
                )
            }
    }

    fun toggleLike(
        postId: String,
        onSuccess: (Boolean, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val postReference =
            postRef(postId)

        val likeReference =
            postLikeRef(
                postId,
                userId
            )

        val rewardReference =
            postLikeRewardRef(
                postId,
                userId
            )

        val userReference =
            userRef(userId)

        firestore.runTransaction { transaction ->

            val postSnapshot =
                transaction.get(postReference)

            val likeSnapshot =
                transaction.get(likeReference)

            val rewardSnapshot =
                transaction.get(rewardReference)

            val user =
                transaction.get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználó nem található."
                    )

            val post =
                postSnapshot
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt nem található."
                    )

            validateInteractivePost(post)

            val currentCount =
                postSnapshot
                    .getLong("likeCount")
                    ?.toInt()
                    ?: 0

            if (likeSnapshot.exists()) {

                val newCount =
                    (currentCount - 1)
                        .coerceAtLeast(0)

                transaction.delete(
                    likeReference
                )

                transaction.update(
                    postReference,
                    "likeCount",
                    newCount
                )

                Pair(
                    false,
                    newCount
                )

            } else {

                val newCount =
                    currentCount + 1

                transaction.set(
                    likeReference,
                    mapOf(
                        "userId" to userId,
                        "likedAt" to
                                System.currentTimeMillis()
                    )
                )

                transaction.update(
                    postReference,
                    "likeCount",
                    newCount
                )

                /*
                 * Ugyanazon poszt első like-ja:
                 * +1 reputációs pont.
                 */
                if (!rewardSnapshot.exists()) {

                    transaction.set(
                        rewardReference,
                        mapOf(
                            "userId" to userId,
                            "awardedAt" to
                                    System.currentTimeMillis()
                        )
                    )

                    transaction.update(
                        userReference,
                        "reputationPoints",
                        user.reputationPoints + 1
                    )
                }

                Pair(
                    true,
                    newCount
                )
            }
        }
            .addOnSuccessListener { result ->
                onSuccess(
                    result.first,
                    result.second
                )
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült módosítani a like-ot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // COMMENTS
    // -------------------------------------------------------------------------

    fun getComments(
        postId: String,
        onSuccess: (List<Comment>) -> Unit,
        onError: (String) -> Unit
    ) {
        commentsRef(postId)
            .orderBy(
                "createdAt",
                Query.Direction.ASCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val comments =
                    snapshot.documents
                        .mapNotNull { document ->
                            document
                                .toObject(Comment::class.java)
                                ?.copy(id = document.id)
                        }
                        .filter {
                            it.parentCommentId.isBlank()
                        }

                onSuccess(comments)
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a kommenteket."
                    )
                )
            }
    }

    fun getCommentReplies(
        postId: String,
        parentCommentId: String,
        onSuccess: (List<Comment>) -> Unit,
        onError: (String) -> Unit
    ) {
        commentsRef(postId)
            .orderBy(
                "createdAt",
                Query.Direction.ASCENDING
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val replies =
                    snapshot.documents
                        .mapNotNull { document ->
                            document
                                .toObject(Comment::class.java)
                                ?.copy(id = document.id)
                        }
                        .filter {
                            it.parentCommentId ==
                                    parentCommentId
                        }

                onSuccess(replies)
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a válaszokat."
                    )
                )
            }
    }

    fun addComment(
        postId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val cleanedText =
            text.trim()

        if (cleanedText.isBlank()) {
            onError(
                "A komment nem lehet üres."
            )
            return
        }

        val userReference =
            userRef(userId)

        val postReference =
            postRef(postId)

        val commentReference =
            commentsRef(postId).document()

        firestore.runTransaction { transaction ->

            val postSnapshot =
                transaction.get(postReference)

            val user =
                transaction.get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználó nem található."
                    )

            val post =
                postSnapshot
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt nem található."
                    )

            validateInteractivePost(post)

            val currentCommentCount =
                postSnapshot
                    .getLong("commentCount")
                    ?.toInt()
                    ?: 0

            val comment =
                Comment(
                    id = commentReference.id,
                    userId = userId,
                    userName =
                        user.name.ifBlank {
                            "Felhasználó"
                        },
                    text = cleanedText,
                    createdAt =
                        System.currentTimeMillis(),
                    parentCommentId = "",
                    likeCount = 0,
                    replyCount = 0
                )

            transaction.set(
                commentReference,
                comment
            )

            transaction.update(
                postReference,
                "commentCount",
                currentCommentCount + 1
            )

            /*
             * Komment:
             * +1 reputációs pont.
             */
            transaction.update(
                userReference,
                "reputationPoints",
                user.reputationPoints + 1
            )
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült elküldeni a kommentet."
                    )
                )
            }
    }

    fun addCommentReply(
        postId: String,
        parentCommentId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val cleanedText =
            text.trim()

        if (cleanedText.isBlank()) {
            onError(
                "A válasz nem lehet üres."
            )
            return
        }

        val userReference =
            userRef(userId)

        val postReference =
            postRef(postId)

        val parentReference =
            commentRef(
                postId,
                parentCommentId
            )

        val replyReference =
            commentsRef(postId).document()

        firestore.runTransaction { transaction ->

            val postSnapshot =
                transaction.get(postReference)

            val parentSnapshot =
                transaction.get(parentReference)

            val user =
                transaction.get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználó nem található."
                    )

            val post =
                postSnapshot
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt nem található."
                    )

            val parentComment =
                parentSnapshot
                    .toObject(Comment::class.java)
                    ?: throw Exception(
                        "A komment nem található."
                    )

            validateInteractivePost(post)

            if (
                parentComment.parentCommentId.isNotBlank()
            ) {
                throw Exception(
                    "Erre a válaszra közvetlenül nem lehet újabb szintű választ létrehozni."
                )
            }

            val currentPostCommentCount =
                postSnapshot
                    .getLong("commentCount")
                    ?.toInt()
                    ?: 0

            val currentReplyCount =
                parentSnapshot
                    .getLong("replyCount")
                    ?.toInt()
                    ?: 0

            val reply =
                Comment(
                    id = replyReference.id,
                    userId = userId,
                    userName =
                        user.name.ifBlank {
                            "Felhasználó"
                        },
                    text = cleanedText,
                    createdAt =
                        System.currentTimeMillis(),
                    parentCommentId =
                        parentCommentId,
                    likeCount = 0,
                    replyCount = 0
                )

            transaction.set(
                replyReference,
                reply
            )

            transaction.update(
                parentReference,
                "replyCount",
                currentReplyCount + 1
            )

            transaction.update(
                postReference,
                "commentCount",
                currentPostCommentCount + 1
            )

            /*
             * Kommentválasz:
             * +1 reputációs pont.
             */
            transaction.update(
                userReference,
                "reputationPoints",
                user.reputationPoints + 1
            )
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült elküldeni a választ."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // COMMENT LIKES
    // -------------------------------------------------------------------------

    fun isCommentLiked(
        postId: String,
        commentId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        commentLikeRef(
            postId = postId,
            commentId = commentId,
            userId = userId
        )
            .get()
            .addOnSuccessListener {
                onSuccess(it.exists())
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült ellenőrizni a komment like állapotát."
                    )
                )
            }
    }

    fun toggleCommentLike(
        postId: String,
        commentId: String,
        onSuccess: (Boolean, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            currentUserId()
                ?: run {
                    onError("Nincs bejelentkezett felhasználó.")
                    return
                }

        val postReference =
            postRef(postId)

        val commentReference =
            commentRef(
                postId,
                commentId
            )

        val likeReference =
            commentLikeRef(
                postId = postId,
                commentId = commentId,
                userId = userId
            )

        firestore.runTransaction { transaction ->

            val postSnapshot =
                transaction.get(postReference)

            val commentSnapshot =
                transaction.get(commentReference)

            val likeSnapshot =
                transaction.get(likeReference)

            val post =
                postSnapshot
                    .toObject(Post::class.java)
                    ?: throw Exception(
                        "A poszt nem található."
                    )

            if (!commentSnapshot.exists()) {
                throw Exception(
                    "A komment nem található."
                )
            }

            validateInteractivePost(post)

            val currentLikeCount =
                commentSnapshot
                    .getLong("likeCount")
                    ?.toInt()
                    ?: 0

            if (likeSnapshot.exists()) {

                val newCount =
                    (currentLikeCount - 1)
                        .coerceAtLeast(0)

                transaction.delete(
                    likeReference
                )

                transaction.update(
                    commentReference,
                    "likeCount",
                    newCount
                )

                Pair(
                    false,
                    newCount
                )

            } else {

                val newCount =
                    currentLikeCount + 1

                transaction.set(
                    likeReference,
                    mapOf(
                        "userId" to userId,
                        "likedAt" to
                                System.currentTimeMillis()
                    )
                )

                transaction.update(
                    commentReference,
                    "likeCount",
                    newCount
                )

                Pair(
                    true,
                    newCount
                )
            }
        }
            .addOnSuccessListener { result ->
                onSuccess(
                    result.first,
                    result.second
                )
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült módosítani a komment like-ot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // ADMIN
    // -------------------------------------------------------------------------

    private fun checkAdmin(
        adminId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        userRef(adminId)
            .get()
            .addOnSuccessListener { document ->

                val admin =
                    document
                        .toObject(User::class.java)
                        ?.copy(id = document.id)

                if (
                    admin == null ||
                    admin.role != ROLE_ADMIN
                ) {
                    onError(
                        "Nincs admin jogosultságod."
                    )
                    return@addOnSuccessListener
                }

                onSuccess(admin)
            }
            .addOnFailureListener { exception ->
                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült ellenőrizni az admin jogosultságot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    private fun validatePostBan(
        user: User
    ) {
        val now =
            System.currentTimeMillis()

        if (
            user.postBanUntil ==
            Long.MAX_VALUE
        ) {
            throw Exception(
                "Véglegesen le vagy tiltva a posztolásról."
            )
        }

        if (
            user.postBanUntil > now
        ) {
            throw Exception(
                "Jelenleg le vagy tiltva a posztolásról."
            )
        }
    }

    private fun validateInteractivePost(
        post: Post
    ) {
        if (post.isDeleted) {
            throw Exception(
                "Törölt posztnál ez a művelet nem végezhető el."
            )
        }

        if (
            post.approvalStatus != STATUS_APPROVED
        ) {
            throw Exception(
                "Ez a poszt még nincs jóváhagyva."
            )
        }
    }

    // -------------------------------------------------------------------------
    // ROLE + SUBSCRIPTION MENTIONS
    // -------------------------------------------------------------------------

    private fun getRoleMentions(
        text: String
    ): List<String> {

        val normalized =
            text.lowercase(Locale.ROOT)

        return buildList {

            if (
                normalized.contains(
                    CUSTOMER_MENTION_TEXT
                )
            ) {
                add(
                    ROLE_MENTION_CUSTOMERS
                )
            }

            if (
                normalized.contains(
                    CONTRACTOR_MENTION_TEXT
                )
            ) {
                add(
                    ROLE_MENTION_CONTRACTORS
                )
            }

            if (
                normalized.contains(
                    WASTE_GO_MENTION_TEXT
                )
            ) {
                add(
                    SubscriptionPlans.MENTION_WASTE_GO
                )
            }

            if (
                normalized.contains(
                    WASTE_PLUS_MENTION_TEXT
                )
            ) {
                add(
                    SubscriptionPlans.MENTION_WASTE_PLUS
                )
            }

            if (
                normalized.contains(
                    WASTE_MAX_MENTION_TEXT
                )
            ) {
                add(
                    SubscriptionPlans.MENTION_WASTE_MAX
                )
            }
        }
    }

    private fun validateRoleMentions(
        user: User,
        roleMentions: List<String>
    ) {
        if (roleMentions.isEmpty()) {
            return
        }

        /*
         * ADMIN:
         * minden jelenlegi mentiont használhat,
         * limit nélkül.
         */
        if (user.role == ROLE_ADMIN) {
            return
        }

        val hasCustomerMention =
            ROLE_MENTION_CUSTOMERS in roleMentions

        val hasContractorMention =
            ROLE_MENTION_CONTRACTORS in roleMentions

        /*
         * @wasteuberesek:
         * továbbra is kizárólag ADMIN.
         */
        if (hasContractorMention) {
            throw Exception(
                "A @wasteuberesek megjelölést csak admin használhatja."
            )
        }

        /*
         * CONTRACTOR:
         *
         * @ügyfelek
         * @wastego
         * @wasteplus
         * @wastemax
         *
         * használható.
         */
        if (user.role == ROLE_CONTRACTOR) {

            val hasForbiddenMention =
                roleMentions.any {
                    it != ROLE_MENTION_CUSTOMERS &&
                            !SubscriptionPlans
                                .isSubscriptionMention(it)
                }

            if (hasForbiddenMention) {
                throw Exception(
                    "Nincs jogosultságod ehhez a megjelöléshez."
                )
            }

            return
        }

        /*
         * WasteUber+:
         *
         * használhatja a csomag megjelöléseket,
         * de @ügyfelek-et nem.
         */
        if (user.isUberPlus) {

            if (hasCustomerMention) {
                throw Exception(
                    "A @ügyfelek megjelölést csak WasteUber kolléga vagy admin használhatja."
                )
            }

            val hasForbiddenMention =
                roleMentions.any {
                    !SubscriptionPlans
                        .isSubscriptionMention(it)
                }

            if (hasForbiddenMention) {
                throw Exception(
                    "Nincs jogosultságod ehhez a megjelöléshez."
                )
            }

            return
        }

        /*
         * Sima CUSTOMER:
         * sem role-, sem csomagtaget nem használhat.
         */
        throw Exception(
            "Nincs jogosultságod role vagy csomag megjelölés használatához."
        )
    }

    // -------------------------------------------------------------------------
    // @ÜGYFELEK DAILY LIMIT
    // -------------------------------------------------------------------------

    private fun updateContractorMentionCounter(
        transaction: Transaction,
        userReference: DocumentReference,
        user: User
    ) {
        val today =
            getTodayDate()

        val currentCount =
            if (
                user.contractorCustomerMentionDate ==
                today
            ) {
                user.contractorCustomerMentionCount
            } else {
                0
            }

        if (
            currentCount >=
            CONTRACTOR_DAILY_MENTION_LIMIT
        ) {
            throw Exception(
                "A napi @ügyfelek megjelölési limited elfogyott. Holnap újra használhatod."
            )
        }

        transaction.update(
            userReference,
            mapOf(
                "contractorCustomerMentionDate" to
                        today,
                "contractorCustomerMentionCount" to
                        currentCount + 1
            )
        )
    }

    // -------------------------------------------------------------------------
    // SUBSCRIPTION MENTION DAILY LIMIT
    // -------------------------------------------------------------------------

    private fun updateSubscriptionMentionCounter(
        transaction: Transaction,
        userReference: DocumentReference,
        user: User,
        amount: Int
    ) {
        if (amount <= 0) {
            return
        }

        val today =
            getTodayDate()

        val currentCount =
            if (
                user.subscriptionMentionDate ==
                today
            ) {
                user.subscriptionMentionCount
            } else {
                0
            }

        /*
         * Példa:
         *
         * mai számláló = 1
         * új posztban:
         * @wastego + @wasteplus
         *
         * 1 + 2 = 3
         *
         * -> nem engedjük.
         */
        if (
            currentCount + amount >
            SUBSCRIPTION_DAILY_MENTION_LIMIT
        ) {
            throw Exception(
                "A napi csomagmegjelölési limited legfeljebb 2. Holnap újra használhatod."
            )
        }

        transaction.update(
            userReference,
            mapOf(
                "subscriptionMentionDate" to
                        today,
                "subscriptionMentionCount" to
                        currentCount + amount
            )
        )
    }

    private fun getTodayDate(): String =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Date()
        )
}
