package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PipelineStageId
import com.example.data.model.StageStatus
import com.example.data.model.VideoProject
import com.example.data.sample.SampleData
import com.example.ui.components.PipelineStepper
import com.example.ui.components.StageDetailCard
import com.example.ui.components.SubtitleExportDialog
import com.example.ui.components.VideoPlayerCard
import com.example.ui.components.YouTubeImportDialog
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PipelineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineScreen(
    viewModel: PipelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val project = uiState.currentProject
    val snackbarHostState = remember { SnackbarHostState() }

    // Android Zero-Permission Photo/Video Picker for Play Policy compliance
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val customProject = VideoProject(
                id = "custom_${System.currentTimeMillis()}",
                title = "Imported Video (${uri.lastPathSegment ?: "Device Video"})",
                description = "Custom video loaded from device for AI Translation & Dubbing.",
                durationMs = 12000L,
                thumbnailRes = project.thumbnailRes,
                videoUri = uri.toString(),
                sourceLanguage = project.sourceLanguage,
                targetLanguage = project.targetLanguage,
                selectedVoice = project.selectedVoice,
                segments = project.segments,
                stages = SampleData.createDefaultStages()
            )
            viewModel.selectProject(customProject)
        }
    }

    LaunchedEffect(uiState.statusBanner) {
        uiState.statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StudioBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(NeonIndigo, NeonCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "VideoDub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NeonIndigo.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AI PIPELINE",
                                        color = NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "10-Stage Video Translation & Dubbing",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { viewModel.openYouTubeDialog() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFF0000).copy(alpha = 0.18f),
                            contentColor = Color(0xFFFF4444)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("open_youtube_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFFF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("YouTube URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { viewModel.runFullPipeline() },
                        enabled = !project.isPipelineRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonIndigo,
                            disabledContainerColor = StudioSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("run_pipeline_button")
                    ) {
                        if (project.isPipelineRunning) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Running...", fontSize = 11.sp)
                        } else {
                            Icon(
                                imageVector = if (project.isPipelineComplete) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (project.isPipelineComplete) "Re-run" else "Run Pipeline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioBackground
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(
                top = 6.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Project Selector Chips & Import Video Button
            item {
                Text(
                    text = "SELECT VIDEO SOURCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // YouTube URL Loader Button (Highlighted)
                    item {
                        Surface(
                            onClick = { viewModel.openYouTubeDialog() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFF0000).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("select_youtube_source_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF0000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Load YouTube Video",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Paste URL to Translate",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFF8888)
                                    )
                                }
                            }
                        }
                    }

                    // Active YouTube video card if current project is YouTube
                    if (project.isYoutube) {
                        item {
                            Surface(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                color = NeonIndigo.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF4444)),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFF0000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = project.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${project.channelOrAuthor} • ${project.targetLanguage.flagEmoji} ${project.targetLanguage.name} Dub",
                                            fontSize = 10.sp,
                                            color = NeonCyan
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(SampleData.sampleProjects) { sampleProj ->
                        val isSelected = sampleProj.id == project.id
                        Surface(
                            onClick = { viewModel.selectProject(sampleProj) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonIndigo.copy(alpha = 0.3f) else StudioSurface,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan) else androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonCyan else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = sampleProj.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                    Text(
                                        text = "${sampleProj.sourceLanguage.flagEmoji} ${sampleProj.sourceLanguage.name} → ${sampleProj.targetLanguage.flagEmoji} ${sampleProj.targetLanguage.name}",
                                        fontSize = 10.sp,
                                        color = if (isSelected) NeonAmber else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // Import Video button
                    item {
                        Surface(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = StudioSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoFile,
                                    contentDescription = null,
                                    tint = NeonAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Import Device Video",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Video Player Card (Live preview, scrubber, subtitle overlay, audio channels)
            item {
                VideoPlayerCard(
                    project = project,
                    isPlaying = uiState.isPlaying,
                    currentTimeMs = uiState.currentPlaybackTimeMs,
                    activeSegment = uiState.activeSegment,
                    playbackMode = uiState.playbackMode,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onPlaybackModeChange = { viewModel.setPlaybackMode(it) }
                )
            }

            // Horizontal Stepper for all 10 stages
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSLATION PIPELINE (10 STAGES)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "Tap stage to inspect",
                        fontSize = 10.sp,
                        color = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                PipelineStepper(
                    stages = project.stages,
                    selectedStage = uiState.selectedStage,
                    onSelectStage = { viewModel.selectStage(it) }
                )
            }

            // Stage Inspector Card for the selected stage
            item {
                StageDetailCard(
                    stageId = uiState.selectedStage,
                    stageState = project.stages[uiState.selectedStage],
                    project = project,
                    onSpeakSegment = { viewModel.speakSegmentPreview(it) },
                    onTargetLanguageChange = { viewModel.setTargetLanguage(it) },
                    onVoiceChange = { viewModel.setVoiceProfile(it) },
                    onUpdateStems = { viewModel.updateAudioStems(it) },
                    onUpdateSubtitles = { viewModel.updateSubtitles(it) },
                    onExportSubtitles = { viewModel.prepareSubtitleExport(it) },
                    onUpdateSegmentText = { id, text -> viewModel.updateSegmentText(id, text) }
                )
            }
        }
    }

    // Subtitle Export Dialog
    if (uiState.showSubtitleExportDialog) {
        SubtitleExportDialog(
            format = uiState.exportedSubtitleFormat,
            text = uiState.exportedSubtitleText,
            onDismiss = { viewModel.dismissSubtitleDialog() }
        )
    }

    // YouTube Import Dialog
    if (uiState.showYouTubeDialog) {
        YouTubeImportDialog(
            selectedTargetLanguage = project.targetLanguage,
            isLoading = uiState.isYouTubeLoading,
            errorMessage = uiState.youTubeError,
            onDismiss = { viewModel.dismissYouTubeDialog() },
            onSubmitUrl = { url, autoRun ->
                viewModel.importAndTranslateYouTube(url, autoRun)
            }
        )
    }
}
