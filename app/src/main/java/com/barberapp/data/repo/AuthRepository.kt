package com.barberapp.data.repo

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun currentUid(): String? = auth.currentUser?.uid
    fun signOut() = auth.signOut()
}

