package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.Notification
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.data.model.User
import hu.wasteuber.app.data.model.WasteRequest

class WasteRequestRepository {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    companion object {

        private const val USERS_COLLECTION =
            "users"

        private const val REQUESTS_COLLECTION =
            "wasteRequests"

        private const val NOTIFICATIONS_COLLECTION =
            "notifications"

        private const val ROLE_ADMIN =
            "ADMIN"

        private const val ROLE_CONTRACTOR =
            "CONTRACTOR"
    }

    // -------------------------------------------------------------------------
    // REFERENCES
    // -------------------------------------------------------------------------

    private fun currentUserId(): String? =
        auth.currentUser?.uid

    private fun userRef(
        userId: String
    ) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)

    private fun requestRef(
        requestId: String
    ) =
        firestore
            .collection(REQUESTS_COLLECTION)
            .document(requestId)

    private fun errorMessage(
        exception: Exception,
        fallback: String
    ): String =
        exception.message ?: fallback

    // -------------------------------------------------------------------------
    // CREATE REQUEST
    // -------------------------------------------------------------------------

    fun createWasteRequest(
        wasteType: String,
        amount: Double,
        address: String,
        description: String,
        imageUrl: String,
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

        val cleanedWasteType =
            wasteType.trim()

        val cleanedAddress =
            address.trim()

        val cleanedDescription =
            description.trim()

        if (cleanedWasteType.isBlank()) {
            onError(
                "Add meg a hulladék típusát."
            )
            return
        }

        if (amount <= 0.0) {
            onError(
                "A hulladék mennyiségének nullánál nagyobbnak kell lennie."
            )
            return
        }

        if (cleanedAddress.isBlank()) {
            onError(
                "Add meg az elszállítás címét."
            )
            return
        }

        val userReference =
            userRef(userId)

        val requestReference =
            firestore
                .collection(REQUESTS_COLLECTION)
                .document()

        firestore.runTransaction { transaction ->

            val user =
                transaction
                    .get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználói adatok nem olvashatók."
                    )

            validateActiveSubscription(
                user
            )

            val request =
                WasteRequest(
                    id =
                        requestReference.id,

                    customerId =
                        userId,

                    customerName =
                        user.name.ifBlank {
                            "Felhasználó"
                        },

                    contractorId =
                        "",

                    wasteType =
                        cleanedWasteType,

                    amount =
                        amount,

                    address =
                        cleanedAddress,

                    description =
                        cleanedDescription,

                    imageUrl =
                        imageUrl,

                    status =
                        WasteRequest.STATUS_OPEN,

                    createdAt =
                        System.currentTimeMillis(),

                    acceptedBy =
                        "",

                    acceptedByName =
                        "",

                    acceptedAt =
                        0L,

                    rejectedBy =
                        "",

                    rejectedByName =
                        "",

                    rejectedAt =
                        0L,

                    rejectionReason =
                        "",

                    completedAt =
                        0L
                )

            transaction.set(
                requestReference,
                request
            )
        }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült létrehozni az elszállítási igényt."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // MY REQUESTS
    // -------------------------------------------------------------------------

    fun getMyRequests(
        onSuccess: (List<WasteRequest>) -> Unit,
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

        firestore
            .collection(REQUESTS_COLLECTION)
            .whereEqualTo(
                "customerId",
                userId
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val requests =
                    snapshot.documents
                        .mapNotNull { document ->

                            document
                                .toObject(
                                    WasteRequest::class.java
                                )
                                ?.copy(
                                    id = document.id
                                )
                        }
                        .sortedByDescending {
                            it.createdAt
                        }

                onSuccess(
                    requests
                )
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült lekérni a saját elszállítási igényeket."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // AVAILABLE REQUESTS
    // -------------------------------------------------------------------------

    fun getAvailableRequests(
        onSuccess: (List<WasteRequest>) -> Unit,
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

        userRef(userId)
            .get()
            .addOnSuccessListener { userDocument ->

                val user =
                    userDocument
                        .toObject(User::class.java)

                if (user == null) {

                    onError(
                        "A felhasználói adatok nem találhatók."
                    )

                    return@addOnSuccessListener
                }

                if (!canHandleRequests(user)) {

                    onError(
                        "Nincs jogosultságod az elszállítási igények megtekintéséhez."
                    )

                    return@addOnSuccessListener
                }

                firestore
                    .collection(REQUESTS_COLLECTION)
                    .whereEqualTo(
                        "status",
                        WasteRequest.STATUS_OPEN
                    )
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val requests =
                            snapshot.documents
                                .mapNotNull { document ->

                                    document
                                        .toObject(
                                            WasteRequest::class.java
                                        )
                                        ?.copy(
                                            id = document.id
                                        )
                                }
                                .sortedByDescending {
                                    it.createdAt
                                }

                        onSuccess(
                            requests
                        )
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            errorMessage(
                                exception,
                                "Nem sikerült lekérni az elérhető elszállítási igényeket."
                            )
                        )
                    }
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült ellenőrizni a jogosultságot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // ACTIVE / ACCEPTED REQUESTS
    // -------------------------------------------------------------------------

    fun getActiveRequests(
        onSuccess: (List<WasteRequest>) -> Unit,
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

        userRef(userId)
            .get()
            .addOnSuccessListener { userDocument ->

                val user =
                    userDocument
                        .toObject(User::class.java)

                if (user == null) {

                    onError(
                        "A felhasználói adatok nem találhatók."
                    )

                    return@addOnSuccessListener
                }

                if (!canHandleRequests(user)) {

                    onError(
                        "Nincs jogosultságod az aktív fuvarok megtekintéséhez."
                    )

                    return@addOnSuccessListener
                }

                firestore
                    .collection(REQUESTS_COLLECTION)
                    .whereEqualTo(
                        "status",
                        WasteRequest.STATUS_ACCEPTED
                    )
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val allAcceptedRequests =
                            snapshot.documents
                                .mapNotNull { document ->

                                    document
                                        .toObject(
                                            WasteRequest::class.java
                                        )
                                        ?.copy(
                                            id = document.id
                                        )
                                }

                        /*
                         * ADMIN:
                         * minden aktív fuvarra rálát.
                         *
                         * WasteUber / WasteUber+:
                         * csak arra, amit ő fogadott el.
                         */
                        val visibleRequests =
                            if (
                                user.role ==
                                ROLE_ADMIN
                            ) {

                                allAcceptedRequests

                            } else {

                                allAcceptedRequests
                                    .filter { request ->

                                        request.acceptedBy ==
                                                userId ||
                                                request.contractorId ==
                                                userId
                                    }
                            }
                                .sortedByDescending {
                                    it.acceptedAt
                                }

                        onSuccess(
                            visibleRequests
                        )
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            errorMessage(
                                exception,
                                "Nem sikerült lekérni az aktív fuvarokat."
                            )
                        )
                    }
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült ellenőrizni a jogosultságot."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // ACCEPT REQUEST
    // -------------------------------------------------------------------------

    fun acceptWasteRequest(
        requestId: String,
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

        if (requestId.isBlank()) {

            onError(
                "Az elszállítási igény azonosítója hiányzik."
            )

            return
        }

        val userReference =
            userRef(userId)

        val requestReference =
            requestRef(requestId)

        val notificationReference =
            firestore
                .collection(
                    NOTIFICATIONS_COLLECTION
                )
                .document()

        firestore.runTransaction { transaction ->

            val user =
                transaction
                    .get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználói adatok nem olvashatók."
                    )

            val request =
                transaction
                    .get(requestReference)
                    .toObject(WasteRequest::class.java)
                    ?: throw Exception(
                        "Az elszállítási igény nem található."
                    )

            if (!canHandleRequests(user)) {

                throw Exception(
                    "Nincs jogosultságod elszállítási igény elfogadásához."
                )
            }

            if (
                request.status !=
                WasteRequest.STATUS_OPEN
            ) {

                throw Exception(
                    "Ezt az elszállítási igényt már nem lehet elfogadni."
                )
            }

            if (
                request.acceptedBy.isNotBlank() ||
                request.contractorId.isNotBlank()
            ) {

                throw Exception(
                    "Ezt az elszállítási igényt már valaki elfogadta."
                )
            }

            if (
                request.customerId.isBlank()
            ) {

                throw Exception(
                    "Az elszállítási igényhez nem tartozik érvényes megrendelő."
                )
            }

            val acceptedAt =
                System.currentTimeMillis()

            val handlerName =
                user.name.ifBlank {
                    "WasteUber"
                }

            transaction.update(
                requestReference,
                mapOf(
                    "contractorId" to
                            userId,

                    "acceptedBy" to
                            userId,

                    "acceptedByName" to
                            handlerName,

                    "acceptedAt" to
                            acceptedAt,

                    "status" to
                            WasteRequest.STATUS_ACCEPTED
                )
            )

            val notification =
                Notification(
                    id =
                        notificationReference.id,

                    userId =
                        request.customerId,

                    type =
                        Notification
                            .TYPE_WASTE_REQUEST_ACCEPTED,

                    title =
                        "Elszállítási igény elfogadva",

                    message =
                        "$handlerName elfogadta az elszállítási igényedet.",

                    relatedId =
                        requestId,

                    createdAt =
                        acceptedAt,

                    read =
                        false
                )

            transaction.set(
                notificationReference,
                notification
            )
        }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült elfogadni az elszállítási igényt."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // ADMIN REJECT REQUEST
    // -------------------------------------------------------------------------

    fun rejectWasteRequest(
        requestId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val adminId =
            currentUserId()
                ?: run {
                    onError(
                        "Nincs bejelentkezett felhasználó."
                    )
                    return
                }

        val cleanedReason =
            reason.trim()

        if (requestId.isBlank()) {

            onError(
                "Az elszállítási igény azonosítója hiányzik."
            )

            return
        }

        if (cleanedReason.isBlank()) {

            onError(
                "Az elutasítás okát kötelező megadni."
            )

            return
        }

        val adminReference =
            userRef(adminId)

        val requestReference =
            requestRef(requestId)

        val notificationReference =
            firestore
                .collection(
                    NOTIFICATIONS_COLLECTION
                )
                .document()

        firestore.runTransaction { transaction ->

            val admin =
                transaction
                    .get(adminReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "Az admin felhasználó nem található."
                    )

            val request =
                transaction
                    .get(requestReference)
                    .toObject(WasteRequest::class.java)
                    ?: throw Exception(
                        "Az elszállítási igény nem található."
                    )

            if (
                admin.role !=
                ROLE_ADMIN
            ) {

                throw Exception(
                    "Csak admin utasíthat el elszállítási igényt."
                )
            }

            if (
                request.status !=
                WasteRequest.STATUS_OPEN
            ) {

                throw Exception(
                    "Csak még el nem fogadott elszállítási igény utasítható el."
                )
            }

            if (
                request.customerId.isBlank()
            ) {

                throw Exception(
                    "Az elszállítási igényhez nem tartozik érvényes megrendelő."
                )
            }

            val rejectedAt =
                System.currentTimeMillis()

            transaction.update(
                requestReference,
                mapOf(
                    "status" to
                            WasteRequest.STATUS_REJECTED,

                    "rejectedBy" to
                            adminId,

                    "rejectedByName" to
                            admin.name.ifBlank {
                                "Admin"
                            },

                    "rejectedAt" to
                            rejectedAt,

                    "rejectionReason" to
                            cleanedReason
                )
            )

            val notification =
                Notification(
                    id =
                        notificationReference.id,

                    userId =
                        request.customerId,

                    type =
                        Notification
                            .TYPE_WASTE_REQUEST_REJECTED,

                    title =
                        "Elszállítási igény elutasítva",

                    message =
                        "Az elszállítási igényedet elutasították. Indok: $cleanedReason",

                    relatedId =
                        requestId,

                    createdAt =
                        rejectedAt,

                    read =
                        false
                )

            transaction.set(
                notificationReference,
                notification
            )
        }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült elutasítani az elszállítási igényt."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // COMPLETE REQUEST
    // -------------------------------------------------------------------------

    fun completeWasteRequest(
        requestId: String,
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

        if (requestId.isBlank()) {

            onError(
                "Az elszállítási igény azonosítója hiányzik."
            )

            return
        }

        val userReference =
            userRef(userId)

        val requestReference =
            requestRef(requestId)

        val notificationReference =
            firestore
                .collection(
                    NOTIFICATIONS_COLLECTION
                )
                .document()

        firestore.runTransaction { transaction ->

            val user =
                transaction
                    .get(userReference)
                    .toObject(User::class.java)
                    ?: throw Exception(
                        "A felhasználói adatok nem olvashatók."
                    )

            val request =
                transaction
                    .get(requestReference)
                    .toObject(WasteRequest::class.java)
                    ?: throw Exception(
                        "Az elszállítási igény nem található."
                    )

            if (
                request.status !=
                WasteRequest.STATUS_ACCEPTED
            ) {

                throw Exception(
                    "Csak elfogadott elszállítási igény teljesíthető."
                )
            }

            val isAssignedHandler =
                request.acceptedBy == userId ||
                        request.contractorId == userId

            val isAdmin =
                user.role == ROLE_ADMIN

            if (
                !isAssignedHandler &&
                !isAdmin
            ) {

                throw Exception(
                    "Nincs jogosultságod ezt az elszállítási igényt teljesítettnek jelölni."
                )
            }

            if (
                request.customerId.isBlank()
            ) {

                throw Exception(
                    "Az elszállítási igényhez nem tartozik érvényes megrendelő."
                )
            }

            val completedAt =
                System.currentTimeMillis()

            transaction.update(
                requestReference,
                mapOf(
                    "status" to
                            WasteRequest.STATUS_COMPLETED,

                    "completedAt" to
                            completedAt
                )
            )

            val notification =
                Notification(
                    id =
                        notificationReference.id,

                    userId =
                        request.customerId,

                    type =
                        Notification
                            .TYPE_WASTE_REQUEST_COMPLETED,

                    title =
                        "Elszállítás teljesítve",

                    message =
                        "Az elszállítási igényed teljesítettként lett lezárva.",

                    relatedId =
                        requestId,

                    createdAt =
                        completedAt,

                    read =
                        false
                )

            transaction.set(
                notificationReference,
                notification
            )
        }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    errorMessage(
                        exception,
                        "Nem sikerült teljesítettnek jelölni az elszállítási igényt."
                    )
                )
            }
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    private fun validateActiveSubscription(
        user: User
    ) {
        val now =
            System.currentTimeMillis()

        val hasActiveSubscription =
            user.subscriptionActive &&
                    user.subscriptionPackage !=
                    SubscriptionPlans.PACKAGE_NONE &&
                    user.subscriptionEndAt > now

        if (!hasActiveSubscription) {

            throw Exception(
                "Hulladékelszállítási igény létrehozásához aktív előfizetés szükséges."
            )
        }
    }

    private fun canHandleRequests(
        user: User
    ): Boolean {

        return user.role == ROLE_ADMIN ||
                user.role == ROLE_CONTRACTOR ||
                user.isUberPlus
    }
}