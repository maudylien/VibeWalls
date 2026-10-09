package com.example

import com.example.data.model.SupportedAspectRatios
import com.example.data.model.SupportedImageSizes
import com.example.data.model.SupportedModels
import com.example.data.model.VibePresets
import com.example.data.model.Wallpaper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVibePresetsExist() {
        assertTrue(VibePresets.presets.isNotEmpty())
        val cyberpunk = VibePresets.presets.firstOrNull { it.title.contains("Cyberpunk") }
        assertNotNull(cyberpunk)
        assertTrue(cyberpunk!!.prompt.contains("cyberpunk"))
    }

    @Test
    fun testSupportedAspectRatios() {
        val phoneRatio = SupportedAspectRatios.firstOrNull { it.ratioValue == "9:16" }
        assertNotNull(phoneRatio)
        assertEquals("9:16", phoneRatio?.ratioValue)
    }

    @Test
    fun testSupportedImageSizes() {
        assertTrue(SupportedImageSizes.contains("1K"))
        assertTrue(SupportedImageSizes.contains("2K"))
        assertTrue(SupportedImageSizes.contains("4K"))
    }

    @Test
    fun testSupportedModels() {
        val nanoBanana = SupportedModels.firstOrNull { it.id == "gemini-nano-banana-2.1" }
        val proImage = SupportedModels.firstOrNull { it.id == "gemini-3-pro-image-preview" }
        assertNotNull(nanoBanana)
        assertNotNull(proImage)
    }

    @Test
    fun testWallpaperModel() {
        val wallpaper = Wallpaper(
            prompt = "rainy cyberpunk lo-fi",
            aspectRatio = "9:16",
            imageSize = "2K",
            variationNumber = 1
        )
        assertEquals("rainy cyberpunk lo-fi", wallpaper.prompt)
        assertEquals("9:16", wallpaper.aspectRatio)
        assertEquals(1, wallpaper.variationNumber)
    }
}
