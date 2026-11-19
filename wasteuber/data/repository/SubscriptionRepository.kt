package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.data.model.SubscriptionPurchase
import hu.wasteuber.app.data.model.User
import java.util.Calendar
import java.util.UUID

class SubscriptionRepository {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    companion object {

        private const val PURCHASES_COLLECTION =
            "subscriptionPurchases"

        private const val TEST_TRANSACTION_PREFIX =
            "TEST"
    }

    /**
     * IDEIGLENES / TESZT aktiválás.
     *
     * Ez azért marad meg egyelőre, hogy a jelenlegi
     * SubscriptionViewModel és SubscriptionScreen
     * továbbra is működjön.
     *
     * Valódi fizetésnél NEM ezt használjuk majd,
     * hanem a transactionId-t fogadó változatot.
     */
    fun activateSubscription(
        packageCode: String,
        period: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val testTransactionId =
            "$TEST_TRANSACTION_PREFIX-${UUID.randomUUID()}"

        activateSubscription(
            packageCode = packageCode,
            period = period,
            transactionId = testTransactionId,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Valódi előfizetés aktiválása.
     *
     * Ezt majd csak sikeresen ellenőrzött
     * fizetés után hívjuk meg.
     *
     * A transactionId a fizetési szolgáltató
     * egyedi tranzakcióazonosítója.
     *
     * Ugyanaz a transactionId csak egyszer
     * dolgozható fel.
     */
    fun activateSubscription(
        packageCode: String,
        period: String,
        transactionId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            auth.currentUser?.uid

        if (userId == null) {
            onError(
                "Nincs bejelentkezett felhasználó."
            )
            return
        }

        if (
            !SubscriptionPlans.isValidPackage(
                packageCode
            )
        ) {
            onError(
                "Érvénytelen előfizetési csomag."
            )
            return
        }

        if (
            !SubscriptionPlans.isValidPeriod(
                period
            )
        ) {
            onError(
                "Érvénytelen előfizetési időszak."
            )
            return
        }

        val cleanedTransactionId =
            transactionId.trim()

        if (
            cleanedTransactionId.isBlank()
        ) {
            onError(
                "Hiányzik a fizetési tranzakció azonosítója."
            )
            return
        }

        val priceFt =
            SubscriptionPlans.getPrice(
                packageCode = packageCode,
                period = period
            )

        if (
            priceFt == null ||
            priceFt <= 0
        ) {
            onError(
                "Az előfizetés ára nem határozható meg."
            )
            return
        }

        val userReference =
            firestore
                .collection("users")
                .document(userId)

        /*
         * A fizetési transactionId-ból stabil
         * Firestore dokumentumazonosítót készítünk.
         *
         * Így ugyanaz a transactionId mindig
         * ugyanarra a dokumentumra mutat.
         */
        val purchaseDocumentId =
            UUID.nameUUIDFromBytes(
                cleanedTransactionId
                    .toByteArray(
                        Charsets.UTF_8
                    )
            ).toString()

        val purchaseReference =
            firestore
                .collection(
                    PURCHASES_COLLECTION
                )
                .document(
                    purchaseDocumentId
                )

        firestore.runTransaction { transaction ->

            /*
             * Firestore tranzakcióban először
             * minden szükséges dokumentumot olvasunk.
             */
            val userSnapshot =
                transaction.get(
                    userReference
                )

            val purchaseSnapshot =
                transaction.get(
                    purchaseReference
                )

            val user =
                userSnapshot
                    .toObject(
                        User::class.java
                    )
                    ?: throw Exception(
                        "A felhasználói adatok nem találhatók."
                    )

            /*
             * Ha ugyanaz a fizetési tranzakció
             * már sikeresen feldolgozásra került,
             * akkor nem aktiváljuk újra.
             *
             * Így +50 reputációt sem lehet
             * ugyanarra a fizetésre többször kapni.
             */
            if (
                purchaseSnapshot.exists()
            ) {

                val previousPurchase =
                    purchaseSnapshot
                        .toObject(
                            SubscriptionPurchase::class.java
                        )

                if (
                    previousPurchase
                        ?.reputationRewardApplied ==
                    true ||
                    previousPurchase?.status ==
                    SubscriptionPurchase.STATUS_PAID
                ) {
                    throw Exception(
                        "Ez a fizetési tranzakció már fel lett dolgozva."
                    )
                }
            }

            val now =
                System.currentTimeMillis()

            /*
             * Ha ugyanaz az aktív csomag és
             * ugyanaz az időszak kerül megújításra,
             * akkor a jelenlegi lejárat végétől
             * hosszabbítjuk meg.
             */
            val sameActiveSubscription =
                user.subscriptionActive &&
                        user.subscriptionPackage ==
                        packageCode &&
                        user.subscriptionPeriod ==
                        period &&
                        user.subscriptionEndAt >
                        now

            val subscriptionStart =
                if (
                    sameActiveSubscription
                ) {
                    user.subscriptionStartAt
                } else {
                    now
                }

            val extensionStart =
                if (
                    sameActiveSubscription
                ) {
                    user.subscriptionEndAt
                } else {
                    now
                }

            val subscriptionEnd =
                calculateSubscriptionEnd(
                    startTimestamp =
                        extensionStart,

                    period =
                        period
                )

            /*
             * Minden különálló, sikeres fizetés:
             *
             * +50 reputáció.
             *
             * A transactionId biztosítja,
             * hogy ugyanaz a fizetés ezt
             * csak egyszer adhassa meg.
             */
            val newReputationPoints =
                user.reputationPoints +
                        SubscriptionPlans
                            .SUBSCRIPTION_REPUTATION_REWARD

            val purchase =
                SubscriptionPurchase(
                    id =
                        purchaseDocumentId,

                    userId =
                        userId,

                    transactionId =
                        cleanedTransactionId,

                    packageCode =
                        packageCode,

                    period =
                        period,

                    priceFt =
                        priceFt,

                    status =
                        SubscriptionPurchase
                            .STATUS_PAID,

                    createdAt =
                        now,

                    verifiedAt =
                        now,

                    subscriptionStartAt =
                        subscriptionStart,

                    subscriptionEndAt =
                        subscriptionEnd,

                    reputationRewardApplied =
                        true
                )

            /*
             * Előfizetés frissítése.
             */
            transaction.update(
                userReference,
                mapOf(
                    "subscriptionPackage" to
                            packageCode,

                    "subscriptionPeriod" to
                            period,

                    "subscriptionActive" to
                            true,

                    "subscriptionStartAt" to
                            subscriptionStart,

                    "subscriptionEndAt" to
                            subscriptionEnd,

                    "reputationPoints" to
                            newReputationPoints
                )
            )

            /*
             * Vásárlási előzmény eltárolása.
             */
            transaction.set(
                purchaseReference,
                purchase
            )
        }
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült aktiválni az előfizetést."
                )
            }
    }

    /**
     * Az előfizetés lemondása.
     *
     * Egyelőre a lemondás nem kapcsolja ki
     * azonnal az előfizetést.
     *
     * A felhasználó a már kifizetett
     * időszak végéig használhatja.
     *
     * Az automatikus megújítás kezelését
     * majd a fizetési rendszerhez kötjük.
     */
    fun cancelSubscription(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            auth.currentUser?.uid

        if (userId == null) {
            onError(
                "Nincs bejelentkezett felhasználó."
            )
            return
        }

        val userReference =
            firestore
                .collection("users")
                .document(userId)

        userReference
            .get()
            .addOnSuccessListener { snapshot ->

                val user =
                    snapshot
                        .toObject(
                            User::class.java
                        )

                if (user == null) {
                    onError(
                        "A felhasználói adatok nem találhatók."
                    )

                    return@addOnSuccessListener
                }

                val now =
                    System.currentTimeMillis()

                val hasValidSubscription =
                    user.subscriptionActive &&
                            user.subscriptionPackage !=
                            SubscriptionPlans.PACKAGE_NONE &&
                            user.subscriptionEndAt >
                            now

                if (
                    !hasValidSubscription
                ) {
                    onError(
                        "Nincs aktív előfizetésed."
                    )

                    return@addOnSuccessListener
                }

                /*
                 * Egyelőre nincs automatikus
                 * megújítási mezőnk.
                 *
                 * Emiatt itt még csak jelezzük,
                 * hogy a lemondási kérés sikeres.
                 *
                 * A már kifizetett időszak
                 * változatlanul megmarad.
                 */
                onSuccess()
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült lemondani az előfizetést."
                )
            }
    }

    /**
     * Ellenőrzi, hogy jelenleg
     * érvényes-e az előfizetés.
     */
    fun checkSubscription(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId =
            auth.currentUser?.uid

        if (userId == null) {
            onError(
                "Nincs bejelentkezett felhasználó."
            )
            return
        }

        val userReference =
            firestore
                .collection("users")
                .document(userId)

        userReference
            .get()
            .addOnSuccessListener { snapshot ->

                val user =
                    snapshot
                        .toObject(
                            User::class.java
                        )

                if (user == null) {
                    onError(
                        "A felhasználói adatok nem találhatók."
                    )

                    return@addOnSuccessListener
                }

                val now =
                    System.currentTimeMillis()

                val isValid =
                    user.subscriptionActive &&
                            user.subscriptionPackage !=
                            SubscriptionPlans.PACKAGE_NONE &&
                            user.subscriptionEndAt >
                            now

                onSuccess(
                    isValid
                )
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült ellenőrizni az előfizetést."
                )
            }
    }

    private fun calculateSubscriptionEnd(
        startTimestamp: Long,
        period: String
    ): Long {
        val calendar =
            Calendar.getInstance()

        calendar.timeInMillis =
            startTimestamp

        when (period) {

            SubscriptionPlans.PERIOD_WEEKLY -> {

                calendar.add(
                    Calendar.DAY_OF_YEAR,
                    7
                )
            }

            SubscriptionPlans.PERIOD_MONTHLY -> {

                calendar.add(
                    Calendar.MONTH,
                    1
                )
            }

            else -> {

                throw IllegalArgumentException(
                    "Érvénytelen előfizetési időszak."
                )
            }
        }

        return calendar.timeInMillis
    }
}