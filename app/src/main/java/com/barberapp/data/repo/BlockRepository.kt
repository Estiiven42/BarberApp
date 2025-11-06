package com.barberapp.data.repo

import com.barberapp.data.model.BlockSlot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val col get() = db.collection("blocks")

    suspend fun create(slot: BlockSlot): String = col.add(
        mapOf("date" to slot.date, "time" to slot.time)
    ).await().id

    suspend fun list(): List<BlockSlot> = col.get().await().documents.map { d ->
        BlockSlot(id = d.id, date = d.getString("date") ?: "", time = d.getString("time") ?: "")
    }
}

