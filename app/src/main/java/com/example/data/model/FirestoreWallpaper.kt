package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreWallpaper(
    @DocumentId val id: String = "",
    val userId: String = "",
    val prompt: String = "",
    val base64Data: String? = null,
    val aspectRatio: String = "9:16",
    val imageSize: String = "1K",
    val modelUsed: String = "gemini-nano-banana-2.1",
    val isFavorite: Boolean = false,
    val variationNumber: Long = 1L,
    val remixedFromId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
