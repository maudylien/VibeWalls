package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.ui.auth.AuthManager
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SupportedAspectRatios
import com.example.data.model.SupportedImageSizes
import com.example.data.model.SupportedModels
import com.example.data.model.VibePresets
import com.example.data.model.Wallpaper
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.FullscreenWallpaperViewer
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.WallpaperViewModel

@Composable
fun HomeScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val vibeInput by viewModel.vibeInput.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val selectedResolution by viewModel.selectedResolution.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatingSlots by viewModel.generatingSlots.collectAsState()
    val currentBatch by viewModel.currentBatch.collectAsState()
    val remixRef by viewModel.remixReferenceWallpaper.collectAsState()
    val fullscreenWallpaper by viewModel.selectedFullscreenWallpaper.collectAsState()
    val savedWallpapers by viewModel.savedWallpapers.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(infoMessage, errorMessage) {
        val msg = infoMessage ?: errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // --- Top App Bar ---
    TopHeaderBar(
        hasApiKey = hasApiKey,
        currentUser = currentUser,
        onOpenApiKeyDialog = { viewModel.setShowApiKeyDialog(true) },
        onSignOut = {
            AuthManager.signOut(context, credentialManager, {}, coroutineScope)
        }
    )

            // --- Navigation Tabs ---
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = NeonPurple,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (activeTab == 0) NeonPurple else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Generate (4)",
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 0) TextPrimary else TextSecondary
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_generator")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (activeTab == 1) NeonCyan else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Saved (${savedWallpapers.size})",
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 1) TextPrimary else TextSecondary
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_saved")
                )
            }

            if (activeTab == 0) {
                // Generator View
                GeneratorTabContent(
                    vibeInput = vibeInput,
                    onVibeChanged = { viewModel.setVibeInput(it) },
                    selectedModel = selectedModel,
                    onModelChanged = { viewModel.setSelectedModel(it) },
                    selectedAspectRatio = selectedAspectRatio,
                    onAspectRatioChanged = { viewModel.setSelectedAspectRatio(it) },
                    selectedResolution = selectedResolution,
                    onResolutionChanged = { viewModel.setSelectedResolution(it) },
                    isGenerating = isGenerating,
                    generatingSlots = generatingSlots,
                    currentBatch = currentBatch,
                    remixRef = remixRef,
                    onClearRemixRef = { viewModel.clearRemixReference() },
                    onGenerate = { viewModel.generateVariations() },
                    onWallpaperClick = { viewModel.openFullscreen(it) },
                    onDownloadClick = { viewModel.downloadWallpaper(it) },
                    onRemixClick = { viewModel.setRemixReference(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            } else {
                // Saved Gallery View
                SavedGalleryTabContent(
                    savedWallpapers = savedWallpapers,
                    onWallpaperClick = { viewModel.openFullscreen(it) },
                    onDownloadClick = { viewModel.downloadWallpaper(it) },
                    onRemixClick = { viewModel.setRemixReference(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }
        }

        // --- Snackbar Host ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )

        // --- Fullscreen Wallpaper Viewer Dialog ---
        fullscreenWallpaper?.let { wall ->
            FullscreenWallpaperViewer(
                wallpaper = wall,
                onClose = { viewModel.closeFullscreen() },
                onDownload = { viewModel.downloadWallpaper(it) },
                onRemix = { viewModel.setRemixReference(it) },
                onApply = { viewModel.applyWallpaper(it) },
                onShare = { viewModel.shareWallpaper(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) }
            )
        }

        // --- API Key Dialog ---
        if (showApiKeyDialog) {
            ApiKeyDialog(
                currentHasKey = hasApiKey,
                onDismiss = { viewModel.setShowApiKeyDialog(false) },
                onSaveKey = { viewModel.updateCustomApiKey(it) }
            )
        }
    }
}

@Composable
private fun TopHeaderBar(
    hasApiKey: Boolean,
    currentUser: FirebaseUser?,
    onOpenApiKeyDialog: () -> Unit,
    onSignOut: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonPurple, NeonCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VibeWalls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (currentUser?.email != null) currentUser.email!! else "9:16 AI Wallpapers",
                    fontSize = 11.sp,
                    color = if (currentUser?.email != null) NeonCyan else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // API Key Status Chip
            Surface(
                onClick = onOpenApiKeyDialog,
                shape = RoundedCornerShape(16.dp),
                color = if (hasApiKey) Color(0x2200E5FF) else Color(0x22FFAB00),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasApiKey) NeonCyan.copy(alpha = 0.6f) else Color(0xFFFFAB00)
                ),
                modifier = Modifier.testTag("api_key_status_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (hasApiKey) NeonCyan else Color(0xFFFFAB00),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasApiKey) "Gemini Ready" else "API Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasApiKey) NeonCyan else Color(0xFFFFAB00)
                    )
                }
            }

            if (currentUser != null) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneratorTabContent(
    vibeInput: String,
    onVibeChanged: (String) -> Unit,
    selectedModel: String,
    onModelChanged: (String) -> Unit,
    selectedAspectRatio: String,
    onAspectRatioChanged: (String) -> Unit,
    selectedResolution: String,
    onResolutionChanged: (String) -> Unit,
    isGenerating: Boolean,
    generatingSlots: Set<Int>,
    currentBatch: List<Wallpaper>,
    remixRef: Wallpaper?,
    onClearRemixRef: () -> Unit,
    onGenerate: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onDownloadClick: (Wallpaper) -> Unit,
    onRemixClick: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit
) {
    var showAdvancedSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- Remix Reference Banner (if active) ---
        AnimatedVisibility(visible = remixRef != null) {
            remixRef?.let { ref ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("remix_reference_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail of reference
                        Box(
                            modifier = Modifier
                                .size(50.dp, 75.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        ) {
                            if (ref.bitmap != null) {
                                Image(
                                    bitmap = ref.bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (ref.drawableResId != null) {
                                Image(
                                    painter = painterResource(id = ref.drawableResId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Remixing from Reference",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                            Text(
                                text = "Next 4 variations will adapt style & layout from this image.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }

                        IconButton(
                            onClick = onClearRemixRef,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear reference",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Prompt / Vibe Input Card ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (remixRef != null) "Describe Remix Adjustments" else "Describe Your Vibe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vibeInput,
                    onValueChange = onVibeChanged,
                    placeholder = {
                        Text(
                            "e.g. rainy cyberpunk lo-fi with neon lights...",
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if (vibeInput.isNotBlank()) {
                            IconButton(onClick = { onVibeChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonPurple
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vibe_input_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Curated Vibe Pills Row
                Text(
                    text = "Quick Vibes:",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VibePresets.presets.forEach { preset ->
                        Surface(
                            onClick = { onVibeChanged(preset.prompt) },
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
                            modifier = Modifier.testTag("vibe_preset_${preset.title.replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = preset.emoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = preset.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Advanced Settings (Aspect Ratio, Size, Model)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedSettings = !showAdvancedSettings }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Settings: Ratio ($selectedAspectRatio) • Size ($selectedResolution)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonPurple
                        )
                    }
                    Text(
                        text = if (showAdvancedSettings) "Hide ▲" else "Customize ▼",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Advanced Settings Panel
                AnimatedVisibility(visible = showAdvancedSettings) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        // Aspect Ratio Selector
                        Text(
                            text = "Aspect Ratio (9:16 recommended for phones):",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SupportedAspectRatios.forEach { ratioItem ->
                                val isSelected = ratioItem.ratioValue == selectedAspectRatio
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onAspectRatioChanged(ratioItem.ratioValue) },
                                    label = {
                                        Text(
                                            text = ratioItem.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = Color.Black,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("ratio_chip_${ratioItem.label.replace(":", "_")}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Image Resolution Selector (1K, 2K, 4K)
                        Text(
                            text = "Image Resolution:",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupportedImageSizes.forEach { sizeOption ->
                                val isSelected = sizeOption == selectedResolution
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onResolutionChanged(sizeOption) },
                                    label = {
                                        Text(
                                            text = sizeOption,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonPurple,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("size_chip_$sizeOption")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Model Selector
                        Text(
                            text = "Generation Model:",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SupportedModels.forEach { modelOption ->
                                val isSelected = modelOption.id == selectedModel
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onModelChanged(modelOption.id) },
                                    label = {
                                        Text(
                                            text = modelOption.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6B11FF),
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("model_chip_${modelOption.id.replace("-", "_")}")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Generate Button
                Button(
                    onClick = onGenerate,
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (remixRef != null) NeonCyan else NeonPurple,
                        contentColor = if (remixRef != null) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_batch_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = if (remixRef != null) Color.Black else Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Creating 4 Variations...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (remixRef != null) "Remix 4 Variations" else "Generate 4 Variations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4 Variations Grid Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "4 Variations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "9:16 Phone Wallpaper • Tap for Full Screen & Actions",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Grid for the 4 variations
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Variation #1
                Box(modifier = Modifier.weight(1f)) {
                    val wall1 = currentBatch.getOrNull(0)
                    WallpaperVariationCard(
                        wallpaper = wall1,
                        slotNumber = 1,
                        isLoading = generatingSlots.contains(1),
                        onClick = { wall1?.let(onWallpaperClick) },
                        onDownload = { wall1?.let(onDownloadClick) },
                        onRemix = { wall1?.let(onRemixClick) },
                        onToggleFavorite = { wall1?.let(onToggleFavorite) }
                    )
                }

                // Variation #2
                Box(modifier = Modifier.weight(1f)) {
                    val wall2 = currentBatch.getOrNull(1)
                    WallpaperVariationCard(
                        wallpaper = wall2,
                        slotNumber = 2,
                        isLoading = generatingSlots.contains(2),
                        onClick = { wall2?.let(onWallpaperClick) },
                        onDownload = { wall2?.let(onDownloadClick) },
                        onRemix = { wall2?.let(onRemixClick) },
                        onToggleFavorite = { wall2?.let(onToggleFavorite) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Variation #3
                Box(modifier = Modifier.weight(1f)) {
                    val wall3 = currentBatch.getOrNull(2)
                    WallpaperVariationCard(
                        wallpaper = wall3,
                        slotNumber = 3,
                        isLoading = generatingSlots.contains(3),
                        onClick = { wall3?.let(onWallpaperClick) },
                        onDownload = { wall3?.let(onDownloadClick) },
                        onRemix = { wall3?.let(onRemixClick) },
                        onToggleFavorite = { wall3?.let(onToggleFavorite) }
                    )
                }

                // Variation #4
                Box(modifier = Modifier.weight(1f)) {
                    val wall4 = currentBatch.getOrNull(3)
                    WallpaperVariationCard(
                        wallpaper = wall4,
                        slotNumber = 4,
                        isLoading = generatingSlots.contains(4),
                        onClick = { wall4?.let(onWallpaperClick) },
                        onDownload = { wall4?.let(onDownloadClick) },
                        onRemix = { wall4?.let(onRemixClick) },
                        onToggleFavorite = { wall4?.let(onToggleFavorite) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WallpaperVariationCard(
    wallpaper: Wallpaper?,
    slotNumber: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onRemix: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clickable(enabled = !isLoading && wallpaper != null, onClick = onClick)
            .testTag("wallpaper_slot_$slotNumber")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper Image Display
            if (wallpaper != null && !isLoading) {
                if (wallpaper.bitmap != null) {
                    Image(
                        bitmap = wallpaper.bitmap.asImageBitmap(),
                        contentDescription = "Wallpaper variation $slotNumber",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (wallpaper.drawableResId != null) {
                    Image(
                        painter = painterResource(id = wallpaper.drawableResId),
                        contentDescription = "Wallpaper variation $slotNumber",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!wallpaper.base64Data.isNullOrBlank()) {
                    AsyncImage(
                        model = "data:image/jpeg;base64,${wallpaper.base64Data}",
                        contentDescription = "Wallpaper variation $slotNumber",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Loading State Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xDD12101F)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = NeonPurple,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Variation #$slotNumber",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x99000000),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "#$slotNumber",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                if (wallpaper != null && !isLoading) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (wallpaper.isFavorite) NeonPink else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Bottom Quick Action Buttons
            if (wallpaper != null && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Download Icon
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x55FFFFFF))
                                .testTag("card_download_$slotNumber")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Remix Icon
                        IconButton(
                            onClick = onRemix,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.85f))
                                .testTag("card_remix_$slotNumber")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Remix",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Fullscreen Icon
                        IconButton(
                            onClick = onClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x55FFFFFF))
                                .testTag("card_fullscreen_$slotNumber")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "View fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedGalleryTabContent(
    savedWallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onDownloadClick: (Wallpaper) -> Unit,
    onRemixClick: (Wallpaper) -> Unit,
    onToggleFavorite: (Wallpaper) -> Unit
) {
    if (savedWallpapers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No Saved Wallpapers Yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate wallpapers and tap the heart or download button to save them here for offline access.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("saved_wallpapers_grid")
        ) {
            itemsIndexed(savedWallpapers) { index, wallpaper ->
                WallpaperVariationCard(
                    wallpaper = wallpaper,
                    slotNumber = index + 1,
                    isLoading = false,
                    onClick = { onWallpaperClick(wallpaper) },
                    onDownload = { onDownloadClick(wallpaper) },
                    onRemix = { onRemixClick(wallpaper) },
                    onToggleFavorite = { onToggleFavorite(wallpaper) }
                )
            }
        }
    }
}
