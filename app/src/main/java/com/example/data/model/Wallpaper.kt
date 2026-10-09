package com.example.data.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import java.util.UUID

data class Wallpaper(
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val bitmap: Bitmap? = null,
    @get:DrawableRes val drawableResId: Int? = null,
    val base64Data: String? = null,
    val aspectRatio: String = "9:16",
    val imageSize: String = "1K",
    val modelUsed: String = "gemini-nano-banana-2.1",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val variationNumber: Int = 1,
    val remixedFromId: String? = null
)

data class VibePreset(
    val title: String,
    val prompt: String,
    val emoji: String,
    val colorStart: Color,
    val colorEnd: Color
)

object VibePresets {
    val presets = listOf(
        VibePreset(
            title = "Rainy Cyberpunk",
            prompt = "rainy cyberpunk lo-fi city street with neon reflections on wet asphalt, moody glow, futuristic atmospheric vibes",
            emoji = "🌧️",
            colorStart = Color(0xFF6B11FF),
            colorEnd = Color(0xFF00E5FF)
        ),
        VibePreset(
            title = "Synthwave Sunset",
            prompt = "retro 80s synthwave neon wireframe highway driving into glowing giant magenta sun with purple grid horizon",
            emoji = "🌆",
            colorStart = Color(0xFFFF007A),
            colorEnd = Color(0xFFFF8A00)
        ),
        VibePreset(
            title = "Studio Ghibli Forest",
            prompt = "lush serene anime nature landscape with sunbeams filtering through ancient mossy trees, Studio Ghibli aesthetic, peaceful peaceful fantasy meadow",
            emoji = "🍃",
            colorStart = Color(0xFF1B5E20),
            colorEnd = Color(0xFF81C784)
        ),
        VibePreset(
            title = "Deep Space Nebula",
            prompt = "deep cosmic galaxy nebula with swirling stellar stardust, glowing cyan and violet stars, ultra high definition cosmic depth",
            emoji = "✨",
            colorStart = Color(0xFF311B92),
            colorEnd = Color(0xFF7C4DFF)
        ),
        VibePreset(
            title = "Zen Minimalist Flow",
            prompt = "minimalist smooth pastel organic 3D shapes floating in serene ambient gradient light, soothing zen wallpaper",
            emoji = "🌸",
            colorStart = Color(0xFFFF80AB),
            colorEnd = Color(0xFFFFE0B2)
        ),
        VibePreset(
            title = "Nordic Mist Forest",
            prompt = "dark nordic pine forest engulfed in morning mist and fog, minimalist atmospheric moody landscape",
            emoji = "🌲",
            colorStart = Color(0xFF263238),
            colorEnd = Color(0xFF78909C)
        ),
        VibePreset(
            title = "Retro 90s Anime",
            prompt = "aesthetic retro 90s vintage anime city rooftop under evening sunset sky with pastel clouds and telephone wires",
            emoji = "📼",
            colorStart = Color(0xFFE91E63),
            colorEnd = Color(0xFF673AB7)
        ),
        VibePreset(
            title = "3D Glassmorphism",
            prompt = "futuristic translucent chromatic glass prisms with glowing iridescent light refractions, sleek abstract modern 3D",
            emoji = "🔮",
            colorStart = Color(0xFF00B0FF),
            colorEnd = Color(0xFFD500F9)
        )
    )
}

data class AspectRatioItem(
    val label: String,
    val ratioValue: String,
    val description: String
)

val SupportedAspectRatios = listOf(
    AspectRatioItem("9:16", "9:16", "Phone Wallpaper (Optimal)"),
    AspectRatioItem("1:1", "1:1", "Square"),
    AspectRatioItem("2:3", "2:3", "Portrait Classic"),
    AspectRatioItem("3:2", "3:2", "Landscape Classic"),
    AspectRatioItem("3:4", "3:4", "Standard Portrait"),
    AspectRatioItem("4:3", "4:3", "Standard Landscape"),
    AspectRatioItem("16:9", "16:9", "Widescreen"),
    AspectRatioItem("21:9", "21:9", "Ultra-wide")
)

val SupportedImageSizes = listOf("1K", "2K", "4K")

val SupportedModels = listOf(
    ModelOption("gemini-nano-banana-2.1", "Nano Banana 2.1 (Fast & Agile)"),
    ModelOption("gemini-3-pro-image-preview", "Gemini 3 Pro (Studio Quality)")
)

data class ModelOption(
    val id: String,
    val displayName: String
)
