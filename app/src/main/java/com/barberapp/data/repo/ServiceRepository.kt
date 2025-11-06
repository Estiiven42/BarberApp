package com.barberapp.data.repo

import com.barberapp.data.model.ServiceItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val col get() = db.collection("services")

    suspend fun list(): List<ServiceItem> = col.get().await().documents.map { d ->
        ServiceItem(
            id = d.id,
            name = d.getString("name") ?: "",
            description = d.getString("description") ?: "",
            price = d.getLong("price") ?: 0L
        )
    }

    suspend fun create(item: ServiceItem): String = col.add(
        mapOf(
            "name" to item.name,
            "description" to item.description,
            "price" to item.price
        )
    ).await().id

    suspend fun update(item: ServiceItem) {
        col.document(item.id).update(
            mapOf(
                "name" to item.name,
                "description" to item.description,
                "price" to item.price
            )
        ).await()
    }

    suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}

