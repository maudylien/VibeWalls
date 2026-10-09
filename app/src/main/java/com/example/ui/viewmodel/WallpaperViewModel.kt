package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.api.GeminiImageService
import com.example.data.model.Wallpaper
import com.example.data.repository.WallpaperRepository
import com.example.util.WallpaperUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val imageService = GeminiImageService()
    private val repository = WallpaperRepository(application)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _vibeInput = MutableStateFlow("rainy cyberpunk lo-fi")
    val vibeInput: StateFlow<String> = _vibeInput.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-nano-banana-2.1")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow("9:16")
    val selectedAspectRatio: StateFlow<String> = _selectedAspectRatio.asStateFlow()

    private val _selectedResolution = MutableStateFlow("1K")
    val selectedResolution: StateFlow<String> = _selectedResolution.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatingSlots = MutableStateFlow<Set<Int>>(emptySet())
    val generatingSlots: StateFlow<Set<Int>> = _generatingSlots.asStateFlow()

    private val _currentBatch = MutableStateFlow<List<Wallpaper>>(emptyList())
    val currentBatch: StateFlow<List<Wallpaper>> = _currentBatch.asStateFlow()

    private val _remixReferenceWallpaper = MutableStateFlow<Wallpaper?>(null)
    val remixReferenceWallpaper: StateFlow<Wallpaper?> = _remixReferenceWallpaper.asStateFlow()

    private val _selectedFullscreenWallpaper = MutableStateFlow<Wallpaper?>(null)
    val selectedFullscreenWallpaper: StateFlow<Wallpaper?> = _selectedFullscreenWallpaper.asStateFlow()

    private val _savedWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val savedWallpapers: StateFlow<List<Wallpaper>> = _savedWallpapers.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Generator, 1: Gallery
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _hasApiKey = MutableStateFlow(imageService.hasApiKey())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    init {
        loadInitialSampleBatch()
        setupAuthListener()
    }

    private fun setupAuthListener() {
        Firebase.auth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            if (auth.currentUser != null) {
                startObservingCloudWallpapers()
            }
        }
    }

    private fun startObservingCloudWallpapers() {
        viewModelScope.launch {
            try {
                repository.observeWallpapers()
                    .catch { error ->
                        Log.w("WallpaperVM", "Failed to observe cloud wallpapers: ${error.message}")
                    }
                    .collect { cloudList ->
                        if (cloudList.isNotEmpty()) {
                            _savedWallpapers.value = cloudList
                        }
                    }
            } catch (e: Exception) {
                Log.w("WallpaperVM", "Cloud observer initialization error: ${e.message}")
            }
        }
    }

    private fun loadInitialSampleBatch() {
        val context = getApplication<Application>()
        try {
            val sample1 = WallpaperUtils.drawableToBitmap(context, R.drawable.sample_wall_1)
            val sample2 = WallpaperUtils.drawableToBitmap(context, R.drawable.sample_wall_2)
            val sample3 = WallpaperUtils.drawableToBitmap(context, R.drawable.sample_wall_3)
            val sample4 = WallpaperUtils.drawableToBitmap(context, R.drawable.sample_wall_4)

            val initialBatch = listOf(
                Wallpaper(
                    prompt = "rainy cyberpunk lo-fi city street neon reflections",
                    bitmap = sample1,
                    drawableResId = R.drawable.sample_wall_1,
                    base64Data = WallpaperUtils.bitmapToBase64(sample1),
                    aspectRatio = "9:16",
                    variationNumber = 1
                ),
                Wallpaper(
                    prompt = "rainy cyberpunk lo-fi misty skyscraper rooftop view",
                    bitmap = sample2,
                    drawableResId = R.drawable.sample_wall_2,
                    base64Data = WallpaperUtils.bitmapToBase64(sample2),
                    aspectRatio = "9:16",
                    variationNumber = 2
                ),
                Wallpaper(
                    prompt = "rainy cyberpunk lo-fi cozy coffee shop rain-streaked window",
                    bitmap = sample3,
                    drawableResId = R.drawable.sample_wall_3,
                    base64Data = WallpaperUtils.bitmapToBase64(sample3),
                    aspectRatio = "9:16",
                    variationNumber = 3
                ),
                Wallpaper(
                    prompt = "rainy cyberpunk lo-fi neon alleyway atmospheric mist",
                    bitmap = sample4,
                    drawableResId = R.drawable.sample_wall_4,
                    base64Data = WallpaperUtils.bitmapToBase64(sample4),
                    aspectRatio = "9:16",
                    variationNumber = 4
                )
            )
            _currentBatch.value = initialBatch
            _savedWallpapers.value = listOf(initialBatch[0], initialBatch[2])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVibeInput(value: String) {
        _vibeInput.value = value
    }

    fun setSelectedModel(modelId: String) {
        _selectedModel.value = modelId
    }

    fun setSelectedAspectRatio(ratio: String) {
        _selectedAspectRatio.value = ratio
    }

    fun setSelectedResolution(size: String) {
        _selectedResolution.value = size
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun openFullscreen(wallpaper: Wallpaper) {
        _selectedFullscreenWallpaper.value = wallpaper
    }

    fun closeFullscreen() {
        _selectedFullscreenWallpaper.value = null
    }

    fun setRemixReference(wallpaper: Wallpaper) {
        _remixReferenceWallpaper.value = wallpaper
        _selectedFullscreenWallpaper.value = null
        _activeTab.value = 0
        _infoMessage.value = "Selected as reference! Enter adjustments or tap 'Remix Batch'."
    }

    fun clearRemixReference() {
        _remixReferenceWallpaper.value = null
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _showApiKeyDialog.value = show
    }

    fun updateCustomApiKey(key: String) {
        imageService.setCustomApiKey(key)
        _hasApiKey.value = imageService.hasApiKey()
        _showApiKeyDialog.value = false
        _infoMessage.value = if (imageService.hasApiKey()) "API Key updated!" else "API Key cleared"
    }

    fun clearMessage() {
        _infoMessage.value = null
        _errorMessage.value = null
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        val isFav = !wallpaper.isFavorite
        val currentSaved = _savedWallpapers.value.toMutableList()
        val existingIndex = currentSaved.indexOfFirst { it.id == wallpaper.id }
        if (existingIndex >= 0) {
            currentSaved.removeAt(existingIndex)
            _infoMessage.value = "Removed from Saved"
        } else {
            currentSaved.add(0, wallpaper.copy(isFavorite = true))
            _infoMessage.value = "Saved to Favorites"
        }
        _savedWallpapers.value = currentSaved

        // Update batch items
        _currentBatch.value = _currentBatch.value.map {
            if (it.id == wallpaper.id) it.copy(isFavorite = isFav) else it
        }

        if (_selectedFullscreenWallpaper.value?.id == wallpaper.id) {
            _selectedFullscreenWallpaper.value = _selectedFullscreenWallpaper.value?.copy(isFavorite = isFav)
        }

        // Sync to Firestore if authenticated
        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                if (existingIndex < 0) {
                    val base64 = wallpaper.base64Data ?: wallpaper.bitmap?.let { WallpaperUtils.bitmapToBase64(it) }
                    repository.saveWallpaper(wallpaper.copy(isFavorite = true, base64Data = base64))
                } else {
                    repository.deleteWallpaper(wallpaper.id)
                }
            }
        }
    }

    fun downloadWallpaper(wallpaper: Wallpaper) {
        val context = getApplication<Application>()
        val bitmap = wallpaper.bitmap ?: wallpaper.drawableResId?.let {
            WallpaperUtils.drawableToBitmap(context, it)
        }

        if (bitmap != null) {
            val uri = WallpaperUtils.saveWallpaperToGallery(context, bitmap, wallpaper.prompt)
            if (uri != null) {
                _infoMessage.value = "Wallpaper saved to Pictures/VibeWalls!"
                if (_savedWallpapers.value.none { it.id == wallpaper.id }) {
                    _savedWallpapers.value = listOf(wallpaper) + _savedWallpapers.value
                }
                // Save to Firestore as well
                if (Firebase.auth.currentUser != null) {
                    viewModelScope.launch {
                        val base64 = wallpaper.base64Data ?: WallpaperUtils.bitmapToBase64(bitmap)
                        repository.saveWallpaper(wallpaper.copy(base64Data = base64))
                    }
                }
            } else {
                _errorMessage.value = "Failed to save image to gallery."
            }
        } else {
            _errorMessage.value = "Image bitmap not available."
        }
    }

    fun applyWallpaper(wallpaper: Wallpaper) {
        val context = getApplication<Application>()
        val bitmap = wallpaper.bitmap ?: wallpaper.drawableResId?.let {
            WallpaperUtils.drawableToBitmap(context, it)
        }

        if (bitmap != null) {
            val success = WallpaperUtils.setAsDeviceWallpaper(context, bitmap)
            if (success) {
                _infoMessage.value = "Wallpaper successfully set on your device!"
            } else {
                _errorMessage.value = "Could not apply wallpaper directly."
            }
        }
    }

    fun shareWallpaper(wallpaper: Wallpaper) {
        val context = getApplication<Application>()
        val bitmap = wallpaper.bitmap ?: wallpaper.drawableResId?.let {
            WallpaperUtils.drawableToBitmap(context, it)
        }
        if (bitmap != null) {
            WallpaperUtils.shareWallpaper(context, bitmap, wallpaper.prompt)
        }
    }

    fun generateVariations() {
        val vibe = _vibeInput.value.trim()
        if (vibe.isBlank()) {
            _errorMessage.value = "Please describe your desired vibe first!"
            return
        }

        val refWallpaper = _remixReferenceWallpaper.value
        val model = _selectedModel.value
        val ratio = _selectedAspectRatio.value
        val size = _selectedResolution.value

        if (!imageService.hasApiKey()) {
            _errorMessage.value = "Gemini API key is required to generate new images. Please set your key in Settings."
            _showApiKeyDialog.value = true
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _generatingSlots.value = setOf(1, 2, 3, 4)
            _errorMessage.value = null
            _infoMessage.value = if (refWallpaper != null) "Remixing 4 variations from reference image..." else "Generating 4 variations for \"$vibe\"..."

            val refBase64 = refWallpaper?.let { wall ->
                wall.base64Data ?: wall.bitmap?.let { WallpaperUtils.bitmapToBase64(it) }
                ?: wall.drawableResId?.let {
                    val bmp = WallpaperUtils.drawableToBitmap(getApplication(), it)
                    WallpaperUtils.bitmapToBase64(bmp)
                }
            }

            val variationModifiers = listOf(
                "cinematic wide composition, dramatic atmospheric lighting, high fidelity details",
                "dynamic perspective, vivid deep contrast and neon highlights, artistic depth",
                "intricate focus on aesthetic atmosphere, soft ambient glow, subtle reflections",
                "stylized creative interpretation, dreamlike color saturation, iconic wallpaper framing"
            )

            val updatedSlots = _currentBatch.value.toMutableList()
            while (updatedSlots.size < 4) {
                updatedSlots.add(Wallpaper(prompt = vibe, variationNumber = updatedSlots.size + 1))
            }

            var successCount = 0
            var firstError: String? = null

            val jobs = (1..4).map { variationIndex ->
                async {
                    val promptVariation = if (refWallpaper != null) {
                        "Remix variation $variationIndex using reference image: $vibe. Enhance with ${variationModifiers[variationIndex - 1]}."
                    } else {
                        "Phone wallpaper in 9:16 vertical orientation: $vibe, ${variationModifiers[variationIndex - 1]}."
                    }

                    val result = imageService.generateSingleImage(
                        prompt = promptVariation,
                        referenceImageBase64 = refBase64,
                        model = model,
                        aspectRatio = ratio,
                        imageSize = size
                    )

                    result.fold(
                        onSuccess = { (bitmap, base64) ->
                            val newWallpaper = Wallpaper(
                                prompt = vibe,
                                bitmap = bitmap,
                                base64Data = base64,
                                aspectRatio = ratio,
                                imageSize = size,
                                modelUsed = model,
                                variationNumber = variationIndex,
                                remixedFromId = refWallpaper?.id
                            )
                            synchronized(updatedSlots) {
                                if (variationIndex - 1 < updatedSlots.size) {
                                    updatedSlots[variationIndex - 1] = newWallpaper
                                } else {
                                    updatedSlots.add(newWallpaper)
                                }
                                _currentBatch.value = updatedSlots.toList()
                                _generatingSlots.value = _generatingSlots.value - variationIndex
                                successCount++
                            }

                            // Sync to Firestore if authenticated
                            if (Firebase.auth.currentUser != null) {
                                launch {
                                    repository.saveWallpaper(newWallpaper)
                                }
                            }
                        },
                        onFailure = { err ->
                            synchronized(updatedSlots) {
                                _generatingSlots.value = _generatingSlots.value - variationIndex
                                if (firstError == null) {
                                    firstError = err.message
                                }
                            }
                        }
                    )
                }
            }

            jobs.awaitAll()
            _isGenerating.value = false
            _generatingSlots.value = emptySet()

            if (successCount > 0) {
                _infoMessage.value = "Generated $successCount new wallpaper variations!"
            } else if (firstError != null) {
                _errorMessage.value = "Generation failed: $firstError"
            }
        }
    }
}
