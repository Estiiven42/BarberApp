package com.barberapp.data.repo

import com.barberapp.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val col get() = db.collection("users")

    suspend fun getUser(uid: String): User? =
        col.document(uid).get().await().toObject(User::class.java)?.copy(uid = uid)

    suspend fun upsert(user: User) {
        col.document(user.uid).set(user).await()
    }
}

