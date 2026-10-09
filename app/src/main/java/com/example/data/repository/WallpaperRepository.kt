package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.FirestoreWallpaper
import com.example.data.model.Wallpaper
import com.example.util.WallpaperUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class WallpaperRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeWallpapers(): Flow<List<Wallpaper>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/wallpapers"
        emitAll(
            db.collection("users").document(uid).collection("wallpapers")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreWallpaper::class.java).map { doc ->
                        Wallpaper(
                            id = doc.id,
                            prompt = doc.prompt,
                            bitmap = doc.base64Data?.let { WallpaperUtils.base64ToBitmap(it) },
                            base64Data = doc.base64Data,
                            aspectRatio = doc.aspectRatio,
                            imageSize = doc.imageSize,
                            modelUsed = doc.modelUsed,
                            isFavorite = doc.isFavorite,
                            variationNumber = doc.variationNumber.toInt(),
                            remixedFromId = doc.remixedFromId,
                            timestamp = doc.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun saveWallpaper(wallpaper: Wallpaper): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val path = "users/$uid/wallpapers/${wallpaper.id}"
        val docRef = db.collection("users").document(uid).collection("wallpapers").document(wallpaper.id)

        try {
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "prompt" to wallpaper.prompt,
                "aspectRatio" to wallpaper.aspectRatio,
                "imageSize" to wallpaper.imageSize,
                "modelUsed" to wallpaper.modelUsed,
                "isFavorite" to wallpaper.isFavorite,
                "variationNumber" to wallpaper.variationNumber.toLong(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            wallpaper.base64Data?.let { payload["base64Data"] = it }
            wallpaper.remixedFromId?.let { payload["remixedFromId"] = it }

            docRef.set(payload).await()
            Result.success(wallpaper.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(wallpaperId: String, isFavorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val path = "users/$uid/wallpapers/$wallpaperId"
        val docRef = db.collection("users").document(uid).collection("wallpapers").document(wallpaperId)

        try {
            docRef.update(
                mapOf(
                    "isFavorite" to isFavorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteWallpaper(wallpaperId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val path = "users/$uid/wallpapers/$wallpaperId"
        val docRef = db.collection("users").document(uid).collection("wallpapers").document(wallpaperId)

        try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun getWallpaperById(wallpaperId: String): Result<Wallpaper> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val path = "users/$uid/wallpapers/$wallpaperId"
        val docRef = db.collection("users").document(uid).collection("wallpapers").document(wallpaperId)

        try {
            val snapshot = docRef.get().await()
            val doc = snapshot.toObject(FirestoreWallpaper::class.java)
                ?: return@withContext Result.failure(Exception("Wallpaper document not found"))

            val wall = Wallpaper(
                id = doc.id,
                prompt = doc.prompt,
                bitmap = doc.base64Data?.let { WallpaperUtils.base64ToBitmap(it) },
                base64Data = doc.base64Data,
                aspectRatio = doc.aspectRatio,
                imageSize = doc.imageSize,
                modelUsed = doc.modelUsed,
                isFavorite = doc.isFavorite,
                variationNumber = doc.variationNumber.toInt(),
                remixedFromId = doc.remixedFromId,
                timestamp = doc.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            )
            Result.success(wall)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }
}
