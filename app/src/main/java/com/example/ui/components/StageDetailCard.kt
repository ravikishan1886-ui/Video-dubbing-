package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioStemConfig
import com.example.data.model.PipelineStageId
import com.example.data.model.PipelineStageState
import com.example.data.model.StageStatus
import com.example.data.model.SubtitleConfig
import com.example.data.model.SubtitleFormat
import com.example.data.model.SupportedLanguage
import com.example.data.model.TranscriptSegment
import com.example.data.model.VideoProject
import com.example.data.model.VoiceProfile
import com.example.data.sample.SampleData
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonIndigoLight
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StageDetailCard(
    stageId: PipelineStageId,
    stageState: PipelineStageState?,
    project: VideoProject,
    onSpeakSegment: (TranscriptSegment) -> Unit,
    onTargetLanguageChange: (SupportedLanguage) -> Unit,
    onVoiceChange: (VoiceProfile) -> Unit,
    onUpdateStems: ((AudioStemConfig) -> AudioStemConfig) -> Unit,
    onUpdateSubtitles: ((SubtitleConfig) -> SubtitleConfig) -> Unit,
    onExportSubtitles: (SubtitleFormat) -> Unit,
    onUpdateSegmentText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = stageState?.status ?: StageStatus.PENDING

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stage_detail_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(StudioSurfaceBorder, Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row with Stage Icon, Title, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getStageIcon(stageId),
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Stage ${stageId.stepNumber} of 10",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stageId.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                StatusChip(status = status)
            }

            if (status == StageStatus.PROCESSING) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { stageState?.progress ?: 0.5f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = NeonCyan,
                    trackColor = StudioSurfaceVariant
                )
            }

            if (!stageState?.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = StudioSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stageState!!.summary,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = StudioSurfaceBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Stage-Specific Interactive Inspector
            when (stageId) {
                PipelineStageId.EXTRACT_AUDIO -> {
                    ExtractAudioInspector(project = project)
                }
                PipelineStageId.SPEECH_RECOGNITION -> {
                    SpeechRecognitionInspector(
                        segments = project.segments,
                        onSpeakSegment = onSpeakSegment
                    )
                }
                PipelineStageId.LANGUAGE_DETECTION -> {
                    LanguageDetectionInspector(
                        sourceLanguage = project.sourceLanguage,
                        onLanguageChange = onTargetLanguageChange
                    )
                }
                PipelineStageId.AI_TRANSLATION -> {
                    AiTranslationInspector(
                        project = project,
                        onTargetLanguageChange = onTargetLanguageChange,
                        onSpeakSegment = onSpeakSegment
                    )
                }
                PipelineStageId.ADAPT_TRANSLATION -> {
                    AdaptTranslationInspector(
                        segments = project.segments,
                        onSpeakSegment = onSpeakSegment,
                        onUpdateText = onUpdateSegmentText
                    )
                }
                PipelineStageId.VOICE_GENERATION -> {
                    VoiceGenerationInspector(
                        project = project,
                        onVoiceChange = onVoiceChange,
                        onSpeakSegment = onSpeakSegment
                    )
                }
                PipelineStageId.TIMING_SYNC -> {
                    TimingSyncInspector(segments = project.segments)
                }
                PipelineStageId.STEM_SEPARATION -> {
                    StemSeparationInspector(
                        stems = project.stems,
                        onUpdateStems = onUpdateStems
                    )
                }
                PipelineStageId.CREATE_SUBTITLES -> {
                    CreateSubtitlesInspector(
                        subtitles = project.subtitles,
                        segments = project.segments,
                        onUpdateSubtitles = onUpdateSubtitles,
                        onExport = onExportSubtitles
                    )
                }
                PipelineStageId.RECOMBINE -> {
                    RecombineInspector(project = project)
                }
            }

            // Metric bullets
            if (stageState?.details?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PIPELINE TELEMETRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                stageState.details.forEach { detail ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = detail,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// Stage 1 Inspector
@Composable
private fun ExtractAudioInspector(project: VideoProject) {
    Column {
        Text(
            text = "AI extracts the audio track, runs Voice Activity Detection (VAD), and separates speech from background sounds.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("Sample Rate", "48.0 kHz", "Lossless PCM", Modifier.weight(1f))
            MetricCard("Vocal Clusters", "3 Regions", "VAD Active", Modifier.weight(1f))
            MetricCard("Background Bed", "Preserved", "Music / SFX", Modifier.weight(1f))
        }
    }
}

// Stage 2 Inspector
@Composable
private fun SpeechRecognitionInspector(
    segments: List<TranscriptSegment>,
    onSpeakSegment: (TranscriptSegment) -> Unit
) {
    Column {
        Text(
            text = "Speech-to-text AI listens to original audio and produces timestamped transcription.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        segments.forEach { seg ->
            Surface(
                color = StudioSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = seg.durationFormatted,
                                color = NeonAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${seg.speaker}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "\"${seg.originalText}\"",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                    IconButton(
                        onClick = { onSpeakSegment(seg) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Listen original segment",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// Stage 3 Inspector
@Composable
private fun LanguageDetectionInspector(
    sourceLanguage: SupportedLanguage,
    onLanguageChange: (SupportedLanguage) -> Unit
) {
    Column {
        Text(
            text = "AI analyzes acoustic patterns and n-grams to detect speech languages.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = StudioSurfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sourceLanguage.flagEmoji,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Detected: ${sourceLanguage.name} (${sourceLanguage.nativeName})",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Confidence: 99.8% • Single Speaker Dialect",
                            color = NeonEmerald,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Stage 4 Inspector
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiTranslationInspector(
    project: VideoProject,
    onTargetLanguageChange: (SupportedLanguage) -> Unit,
    onSpeakSegment: (TranscriptSegment) -> Unit
) {
    Column {
        Text(
            text = "Translation model converts transcript into target language. Choose target language:",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Target language chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SampleData.languages.forEach { lang ->
                val isSelected = lang.code == project.targetLanguage.code
                Surface(
                    onClick = { onTargetLanguageChange(lang) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) NeonIndigo else StudioSurfaceVariant,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lang.flagEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = lang.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Translated Script Preview (${project.sourceLanguage.code.uppercase()} → ${project.targetLanguage.code.uppercase()}):",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(6.dp))

        project.segments.forEach { seg ->
            Surface(
                color = StudioSurfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Original: \"${seg.originalText}\"",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Translated: \"${seg.translatedText.ifBlank { "Pending translation..." }}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onSpeakSegment(seg) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play translated voice",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Stage 5 Inspector
@Composable
private fun AdaptTranslationInspector(
    segments: List<TranscriptSegment>,
    onSpeakSegment: (TranscriptSegment) -> Unit,
    onUpdateText: (String, String) -> Unit
) {
    Column {
        Text(
            text = "Adapts translation to match natural speaking pace, adjust sentence lengths for lip-sync, and maintain speaker emotion.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        segments.forEach { seg ->
            var isEditing by remember { mutableStateOf(false) }
            var editText by remember(seg.adaptedText) { mutableStateOf(seg.adaptedText.ifBlank { seg.translatedText }) }

            Surface(
                color = StudioSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cadence Sync: ${String.format("%.2f", seg.syncSpeedMultiplier)}x • ${seg.durationFormatted}",
                            color = NeonAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Text(
                                text = if (isEditing) "Done" else "Edit",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        if (isEditing) {
                                            onUpdateText(seg.id, editText)
                                        }
                                        isEditing = !isEditing
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onSpeakSegment(seg) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Preview adapted speech",
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    if (isEditing) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary)
                        )
                    } else {
                        Text(
                            text = if (seg.adaptedText.isNotBlank()) seg.adaptedText else seg.translatedText.ifBlank { seg.originalText },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// Stage 6 Inspector
@Composable
private fun VoiceGenerationInspector(
    project: VideoProject,
    onVoiceChange: (VoiceProfile) -> Unit,
    onSpeakSegment: (TranscriptSegment) -> Unit
) {
    Column {
        Text(
            text = "AI dubbing voice generation. Select neural voice profile:",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        SampleData.voiceProfiles.forEach { voice ->
            val isSelected = voice.id == project.selectedVoice.id
            Surface(
                onClick = { onVoiceChange(voice) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) NeonIndigo.copy(alpha = 0.25f) else StudioSurfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, NeonIndigoLight) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = voice.name,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${voice.gender} • ${voice.tone} • Pitch: ${voice.pitch}x",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = {
                            project.segments.firstOrNull()?.let { onSpeakSegment(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) NeonIndigo else StudioSurfaceVariant),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Preview Voice", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// Stage 7 Inspector
@Composable
private fun TimingSyncInspector(segments: List<TranscriptSegment>) {
    Column {
        Text(
            text = "Synchronizes voice duration with video lip movement by adjusting micro-pauses and speed multipliers.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        segments.forEach { seg ->
            Surface(
                color = StudioSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = seg.durationFormatted,
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rate: ${String.format("%.2f", seg.syncSpeedMultiplier)}x",
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (seg.adaptedText.isNotBlank()) seg.adaptedText else seg.translatedText,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// Stage 8 Inspector
@Composable
private fun StemSeparationInspector(
    stems: AudioStemConfig,
    onUpdateStems: ((AudioStemConfig) -> AudioStemConfig) -> Unit
) {
    Column {
        Text(
            text = "Separates dialogue, music, and sound effects. Keeps background music/effects while replacing original speech.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Stem Slider 1: Dubbed Voice
        StemSliderRow(
            label = "Translated AI Dubbed Voice",
            value = stems.dubbedVoiceVolume,
            color = NeonCyan,
            onValueChange = { v -> onUpdateStems { it.copy(dubbedVoiceVolume = v) } }
        )

        // Stem Slider 2: Original Voice (Muted / Ducked)
        StemSliderRow(
            label = "Original Voice (Ducked/Muted)",
            value = stems.dialogueVolume,
            color = TextMuted,
            onValueChange = { v -> onUpdateStems { it.copy(dialogueVolume = v) } }
        )

        // Stem Slider 3: Background Music
        StemSliderRow(
            label = "Background Music Bed",
            value = stems.musicVolume,
            color = NeonIndigoLight,
            onValueChange = { v -> onUpdateStems { it.copy(musicVolume = v) } }
        )

        // Stem Slider 4: Sound Effects (SFX)
        StemSliderRow(
            label = "Ambient Sound Effects (SFX)",
            value = stems.sfxVolume,
            color = NeonAmber,
            onValueChange = { v -> onUpdateStems { it.copy(sfxVolume = v) } }
        )

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Auto-Ducking (-18dB when speaking)",
                fontSize = 12.sp,
                color = TextPrimary
            )
            Switch(
                checked = stems.duckingEnabled,
                onCheckedChange = { chk -> onUpdateStems { it.copy(duckingEnabled = chk) } },
                colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonIndigo)
            )
        }
    }
}

@Composable
private fun StemSliderRow(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextPrimary)
            Text(text = "${(value * 100).toInt()}%", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

// Stage 9 Inspector
@Composable
private fun CreateSubtitlesInspector(
    subtitles: SubtitleConfig,
    segments: List<TranscriptSegment>,
    onUpdateSubtitles: ((SubtitleConfig) -> SubtitleConfig) -> Unit,
    onExport: (SubtitleFormat) -> Unit
) {
    Column {
        Text(
            text = "Convert translated script into .SRT, .VTT, or burned-in subtitles on the video frame.",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onExport(SubtitleFormat.SRT) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export .SRT", fontSize = 11.sp)
            }
            Button(
                onClick = { onExport(SubtitleFormat.VTT) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export .VTT", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Burned-in Video Overlay Subtitles",
                fontSize = 12.sp,
                color = TextPrimary
            )
            Switch(
                checked = subtitles.enabled,
                onCheckedChange = { chk -> onUpdateSubtitles { it.copy(enabled = chk) } },
                colors = SwitchDefaults.colors(checkedThumbColor = NeonEmerald)
            )
        }
    }
}

// Stage 10 Inspector
@Composable
private fun RecombineInspector(project: VideoProject) {
    Column {
        Text(
            text = "Original Video + Synced Translated Voice + Music/SFX + Subtitles → Final Translated Video",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = StudioSurfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Translation Package Ready",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Language: ${project.sourceLanguage.name} → ${project.targetLanguage.name} ${project.targetLanguage.flagEmoji}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "• Voice: ${project.selectedVoice.name} (${project.selectedVoice.gender})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "• Audio Stems: Voice (100%), Music (45%), SFX (65%)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "• Subtitles: Burned-in overlay & .SRT/.VTT tracks included",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        color = StudioSurfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, fontSize = 9.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 9.sp, color = NeonCyan)
        }
    }
}

@Composable
private fun StatusChip(status: StageStatus) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = when (status) {
            StageStatus.COMPLETED -> NeonEmerald.copy(alpha = 0.2f)
            StageStatus.PROCESSING -> NeonAmber.copy(alpha = 0.2f)
            StageStatus.ERROR -> Color(0xFFEF4444).copy(alpha = 0.2f)
            StageStatus.PENDING -> StudioSurfaceBorder.copy(alpha = 0.5f)
        }
    ) {
        Text(
            text = when (status) {
                StageStatus.COMPLETED -> "COMPLETED"
                StageStatus.PROCESSING -> "RUNNING"
                StageStatus.ERROR -> "FAILED"
                StageStatus.PENDING -> "PENDING"
            },
            color = when (status) {
                StageStatus.COMPLETED -> NeonEmerald
                StageStatus.PROCESSING -> NeonAmber
                StageStatus.ERROR -> Color(0xFFEF4444)
                StageStatus.PENDING -> TextMuted
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getStageIcon(stageId: PipelineStageId): androidx.compose.ui.graphics.vector.ImageVector {
    return when (stageId) {
        PipelineStageId.EXTRACT_AUDIO -> Icons.Default.Audiotrack
        PipelineStageId.SPEECH_RECOGNITION -> Icons.Default.Mic
        PipelineStageId.LANGUAGE_DETECTION -> Icons.Default.Language
        PipelineStageId.AI_TRANSLATION -> Icons.Default.Translate
        PipelineStageId.ADAPT_TRANSLATION -> Icons.Default.AutoFixHigh
        PipelineStageId.VOICE_GENERATION -> Icons.Default.RecordVoiceOver
        PipelineStageId.TIMING_SYNC -> Icons.Default.Sync
        PipelineStageId.STEM_SEPARATION -> Icons.Default.Tune
        PipelineStageId.CREATE_SUBTITLES -> Icons.Default.Subtitles
        PipelineStageId.RECOMBINE -> Icons.Default.Videocam
    }
}
