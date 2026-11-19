package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.User

class AdminModerationRepository {

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    companion object {

        const val BAN_ONE_DAY =
            "ONE_DAY"

        const val BAN_ONE_WEEK =
            "ONE_WEEK"

        const val BAN_ONE_MONTH =
            "ONE_MONTH"

        const val BAN_PERMANENT =
            "PERMANENT"

        private const val ONE_DAY_MILLIS =
            24L * 60L * 60L * 1000L

        private const val ONE_WEEK_MILLIS =
            7L * ONE_DAY_MILLIS

        private const val ONE_MONTH_MILLIS =
            30L * ONE_DAY_MILLIS
    }

    fun getUsers(
        onSuccess: (List<User>) -> Unit,
        onError: (String) -> Unit
    ) {
        checkAdmin(
            onSuccess = {

                firestore
                    .collection("users")
                    .get()
                    .addOnSuccessListener { documents ->

                        val users =
                            documents.documents
                                .mapNotNull { document ->

                                    document
                                        .toObject(User::class.java)
                                        ?.copy(
                                            id = document.id
                                        )
                                }
                                .sortedBy { user ->
                                    user.name.lowercase()
                                }

                        onSuccess(users)
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            exception.message
                                ?: "Nem sikerült lekérni a felhasználókat."
                        )
                    }
            },

            onError = onError
        )
    }

    fun getUser(
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        checkAdmin(
            onSuccess = {

                firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->

                        val user =
                            document
                                .toObject(User::class.java)
                                ?.copy(
                                    id = document.id
                                )

                        if (user == null) {
                            onError(
                                "A felhasználó nem található."
                            )
                            return@addOnSuccessListener
                        }

                        onSuccess(user)
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            exception.message
                                ?: "Nem sikerült lekérni a felhasználót."
                        )
                    }
            },

            onError = onError
        )
    }

    fun banUserFromPosting(
        userId: String,
        banType: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (reason.isBlank()) {
            onError(
                "A kitiltás okát kötelező megadni."
            )
            return
        }

        checkAdmin(
            onSuccess = { admin ->

                val userReference =
                    firestore
                        .collection("users")
                        .document(userId)

                userReference
                    .get()
                    .addOnSuccessListener { userDocument ->

                        if (!userDocument.exists()) {
                            onError(
                                "A felhasználó nem található."
                            )
                            return@addOnSuccessListener
                        }

                        val targetUser =
                            userDocument.toObject(
                                User::class.java
                            )

                        if (targetUser == null) {
                            onError(
                                "A felhasználó adatai nem olvashatók."
                            )
                            return@addOnSuccessListener
                        }

                        if (targetUser.role == "ADMIN") {
                            onError(
                                "Adminisztrátort nem lehet posztolásról kitiltani."
                            )
                            return@addOnSuccessListener
                        }

                        val currentTime =
                            System.currentTimeMillis()

                        val banUntil =
                            when (banType) {

                                BAN_ONE_DAY ->
                                    currentTime +
                                            ONE_DAY_MILLIS

                                BAN_ONE_WEEK ->
                                    currentTime +
                                            ONE_WEEK_MILLIS

                                BAN_ONE_MONTH ->
                                    currentTime +
                                            ONE_MONTH_MILLIS

                                BAN_PERMANENT ->
                                    Long.MAX_VALUE

                                else -> {
                                    onError(
                                        "Ismeretlen kitiltási időtartam."
                                    )
                                    return@addOnSuccessListener
                                }
                            }

                        val currentBanCount =
                            userDocument
                                .getLong(
                                    "postBanCount"
                                )
                                ?.toInt()
                                ?: 0

                        val banData =
                            mapOf(
                                "postBanUntil" to
                                        banUntil,

                                "postBanCount" to
                                        currentBanCount + 1,

                                "postBanReason" to
                                        reason.trim(),

                                "postBannedBy" to
                                        admin.id,

                                "postBannedByName" to
                                        admin.name,

                                "postBannedAt" to
                                        currentTime
                            )

                        userReference
                            .update(banData)
                            .addOnSuccessListener {

                                onSuccess()
                            }
                            .addOnFailureListener { exception ->

                                onError(
                                    exception.message
                                        ?: "Nem sikerült kitiltani a felhasználót."
                                )
                            }
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            exception.message
                                ?: "Nem sikerült lekérni a felhasználót."
                        )
                    }
            },

            onError = onError
        )
    }

    fun removePostingBan(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        checkAdmin(
            onSuccess = {

                val userReference =
                    firestore
                        .collection("users")
                        .document(userId)

                userReference
                    .get()
                    .addOnSuccessListener { document ->

                        if (!document.exists()) {
                            onError(
                                "A felhasználó nem található."
                            )
                            return@addOnSuccessListener
                        }

                        val user =
                            document.toObject(
                                User::class.java
                            )

                        if (user == null) {
                            onError(
                                "A felhasználó adatai nem olvashatók."
                            )
                            return@addOnSuccessListener
                        }

                        if (user.role == "ADMIN") {
                            onError(
                                "Adminisztrátor tiltása nem módosítható."
                            )
                            return@addOnSuccessListener
                        }

                        /*
                         * Csak az aktív tiltást szüntetjük meg.
                         *
                         * A postBanReason,
                         * postBannedBy,
                         * postBannedByName és
                         * postBannedAt mezőket szándékosan
                         * megtartjuk, így később is látható
                         * marad a legutóbbi moderáció.
                         */
                        userReference
                            .update(
                                "postBanUntil",
                                0L
                            )
                            .addOnSuccessListener {

                                onSuccess()
                            }
                            .addOnFailureListener { exception ->

                                onError(
                                    exception.message
                                        ?: "Nem sikerült feloldani a kitiltást."
                                )
                            }
                    }
                    .addOnFailureListener { exception ->

                        onError(
                            exception.message
                                ?: "Nem sikerült lekérni a felhasználót."
                        )
                    }
            },

            onError = onError
        )
    }

    private fun checkAdmin(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser =
            auth.currentUser

        if (currentUser == null) {
            onError(
                "Nincs bejelentkezett felhasználó."
            )
            return
        }

        firestore
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    onError(
                        "Az admin felhasználó nem található."
                    )
                    return@addOnSuccessListener
                }

                val admin =
                    document
                        .toObject(User::class.java)
                        ?.copy(
                            id = document.id
                        )

                if (admin == null) {
                    onError(
                        "Az admin felhasználó adatai nem olvashatók."
                    )
                    return@addOnSuccessListener
                }

                if (admin.role != "ADMIN") {
                    onError(
                        "Nincs admin jogosultságod."
                    )
                    return@addOnSuccessListener
                }

                onSuccess(admin)
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült ellenőrizni az admin jogosultságot."
                )
            }
    }
}