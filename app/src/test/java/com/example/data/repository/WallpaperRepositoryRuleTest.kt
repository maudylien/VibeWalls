package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Wallpaper
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class WallpaperRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveWallpaper_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = WallpaperRepository(firestore, auth)

        val testId = "wall_" + UUID.randomUUID().toString().replace("-", "")
        val wallpaper = Wallpaper(
            id = testId,
            prompt = "rainy cyberpunk lo-fi neon reflections",
            aspectRatio = "9:16",
            imageSize = "1K",
            modelUsed = "gemini-nano-banana-2.1",
            isFavorite = true,
            variationNumber = 1
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWallpaper(wallpaper)
        }
        assertTrue(result.isSuccess)
        assertEquals(testId, result.getOrThrow())
    }

    @Test
    fun getWallpaperById_authenticatedOwner_returnsMatchingWallpaper() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = WallpaperRepository(firestore, auth)

        val testId = "wall_" + UUID.randomUUID().toString().replace("-", "")
        val wallpaper = Wallpaper(
            id = testId,
            prompt = "rainy cyberpunk lo-fi rooftop view",
            aspectRatio = "9:16",
            imageSize = "1K",
            modelUsed = "gemini-nano-banana-2.1",
            isFavorite = false,
            variationNumber = 2
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWallpaper(wallpaper).getOrThrow()
        }

        val fetchResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getWallpaperById(testId)
        }
        assertTrue(fetchResult.isSuccess)
        val fetched = fetchResult.getOrThrow()
        assertEquals(testId, fetched.id)
        assertEquals("rainy cyberpunk lo-fi rooftop view", fetched.prompt)
    }

    @Test
    fun observeWallpapers_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = WallpaperRepository(firestore, auth)

        val testId = "wall_" + UUID.randomUUID().toString().replace("-", "")
        val wallpaper = Wallpaper(
            id = testId,
            prompt = "rainy cyberpunk coffee shop",
            aspectRatio = "9:16",
            imageSize = "1K",
            variationNumber = 3
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWallpaper(wallpaper).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeWallpapers().first { list ->
                list.any { it.id == testId }
            }
        }
        assertTrue(emitted.any { it.id == testId })
    }

    @Test
    fun getWallpaperById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = WallpaperRepository(firestore, auth)

        val testId = "wall_" + UUID.randomUUID().toString().replace("-", "")
        val wallpaper = Wallpaper(
            id = testId,
            prompt = "private alice wallpaper",
            aspectRatio = "9:16",
            imageSize = "1K",
            variationNumber = 1
        )
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveWallpaper(wallpaper).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = WallpaperRepository(firestore, auth)

        // Attempting to read Alice's wallpaper using Bob's repo should fail
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getWallpaperById(testId)
        }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        if (exception is FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception.code)
        }
    }

    @Test
    fun observeWallpapers_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = WallpaperRepository(firestore, auth)

        var failed = false
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeWallpapers().first()
            }
        } catch (e: Exception) {
            failed = true
        }
        assertTrue(failed)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
