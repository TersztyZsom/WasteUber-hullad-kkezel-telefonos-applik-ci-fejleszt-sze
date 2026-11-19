package hu.wasteuber.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.wasteuber.app.data.model.User

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fullPhoneNumber = "+36$phoneNumber"

        firestore.collection("users")
            .whereEqualTo("phoneNumber", fullPhoneNumber)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->

                if (!documents.isEmpty) {
                    onError("Ez a telefonszám már használatban van.")
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        if (!task.isSuccessful) {
                            onError(
                                task.exception?.message
                                    ?: "Nem sikerült létrehozni a fiókot."
                            )
                            return@addOnCompleteListener
                        }

                        val firebaseUser = auth.currentUser

                        if (firebaseUser == null) {
                            onError("Nem sikerült létrehozni a felhasználót.")
                            return@addOnCompleteListener
                        }

                        val userId = firebaseUser.uid

                        val user = User(
                            id = userId,
                            name = name,
                            email = email,
                            phoneNumber = fullPhoneNumber,
                            role = "CUSTOMER",
                            contractorApplicationStatus = "NONE",
                            isUberPlus = false
                        )

                        firestore.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->

                                firebaseUser.delete()

                                onError(
                                    exception.message
                                        ?: "Nem sikerült elmenteni a felhasználói adatokat."
                                )
                            }
                    }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült ellenőrizni a telefonszámot."
                )
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(
                        task.exception?.message
                            ?: "Hibás e-mail cím vagy jelszó."
                    )
                }
            }
    }

    fun getCurrentUser(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onError("Nincs bejelentkezett felhasználó.")
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                val user = document.toObject(User::class.java)

                if (user != null) {
                    onSuccess(user)
                } else {
                    onError("Nem találhatók a felhasználói adatok.")
                }
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Nem sikerült lekérni a felhasználói adatokat."
                )
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}