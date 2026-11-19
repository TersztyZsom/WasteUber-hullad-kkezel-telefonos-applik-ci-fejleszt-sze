package hu.wasteuber.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.User

class UserRepository {

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    fun getUserById(
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId.isBlank()) {
            onError(
                "Érvénytelen felhasználói azonosító."
            )
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    onError(
                        "A felhasználó nem található."
                    )
                    return@addOnSuccessListener
                }

                val user =
                    document
                        .toObject(User::class.java)
                        ?.copy(
                            id = document.id
                        )

                if (user == null) {
                    onError(
                        "A felhasználói adatok nem olvashatók."
                    )
                    return@addOnSuccessListener
                }

                onSuccess(user)
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Nem sikerült lekérni a felhasználó profilját."
                )
            }
    }
}