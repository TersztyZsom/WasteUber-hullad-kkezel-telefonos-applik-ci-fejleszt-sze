package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.ContractorApplication

class ContractorApplicationRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun submitApplication(
        application: ContractorApplication,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onError("Nincs bejelentkezett felhasználó.")
            return
        }

        val applicationWithUser = application.copy(
            userId = userId,
            status = "PENDING"
        )

        firestore.collection("contractorApplications")
            .document(userId)
            .set(applicationWithUser)
            .addOnSuccessListener {

                firestore.collection("users")
                    .document(userId)
                    .update(
                        "contractorApplicationStatus",
                        "PENDING"
                    )
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(
                            exception.message
                                ?: "Nem sikerült frissíteni a jelentkezés állapotát."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült elküldeni a jelentkezést."
                )
            }
    }

    fun getCurrentUserApplication(
        onSuccess: (ContractorApplication?) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onError("Nincs bejelentkezett felhasználó.")
            return
        }

        firestore.collection("contractorApplications")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val application =
                        document.toObject(ContractorApplication::class.java)

                    onSuccess(application)

                } else {

                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült lekérni a jelentkezést."
                )
            }
    }

    fun getPendingApplications(
        onSuccess: (List<ContractorApplication>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("contractorApplications")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener { documents ->

                val applications =
                    documents.toObjects(ContractorApplication::class.java)

                onSuccess(applications)
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült lekérni a függő jelentkezéseket."
                )
            }
    }

    fun approveApplication(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("contractorApplications")
            .document(userId)
            .update(
                "status",
                "APPROVED"
            )
            .addOnSuccessListener {

                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "contractorApplicationStatus" to "APPROVED",
                            "role" to "CONTRACTOR"
                        )
                    )
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(
                            exception.message
                                ?: "Nem sikerült frissíteni a felhasználó szerepkörét."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült elfogadni a jelentkezést."
                )
            }
    }

    fun rejectApplication(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("contractorApplications")
            .document(userId)
            .update(
                "status",
                "REJECTED"
            )
            .addOnSuccessListener {

                firestore.collection("users")
                    .document(userId)
                    .update(
                        "contractorApplicationStatus",
                        "REJECTED"
                    )
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(
                            exception.message
                                ?: "Nem sikerült frissíteni a jelentkezés állapotát."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült elutasítani a jelentkezést."
                )
            }
    }
}