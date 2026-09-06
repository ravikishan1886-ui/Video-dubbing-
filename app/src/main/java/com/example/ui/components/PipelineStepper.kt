package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PipelineStageId
import com.example.data.model.PipelineStageState
import com.example.data.model.StageStatus
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PipelineStepper(
    stages: Map<PipelineStageId, PipelineStageState>,
    selectedStage: PipelineStageId,
    onSelectStage: (PipelineStageId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PipelineStageId.values()) { stageId ->
            val stageState = stages[stageId]
            val status = stageState?.status ?: StageStatus.PENDING
            val isSelected = stageId == selectedStage

            StagePill(
                stageId = stageId,
                status = status,
                isSelected = isSelected,
                onClick = { onSelectStage(stageId) }
            )
        }
    }
}

@Composable
private fun StagePill(
    stageId: PipelineStageId,
    status: StageStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> NeonCyan
            status == StageStatus.COMPLETED -> NeonEmerald.copy(alpha = 0.6f)
            status == StageStatus.PROCESSING -> NeonAmber
            else -> StudioSurfaceBorder
        },
        label = "pill_border"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> NeonIndigo.copy(alpha = 0.22f)
            status == StageStatus.COMPLETED -> StudioSurfaceVariant.copy(alpha = 0.8f)
            else -> StudioSurface
        },
        label = "pill_bg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when (status) {
                            StageStatus.COMPLETED -> NeonEmerald
                            StageStatus.PROCESSING -> NeonAmber
                            StageStatus.ERROR -> Color(0xFFEF4444)
                            StageStatus.PENDING -> StudioSurfaceBorder
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (status) {
                    StageStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    StageStatus.PROCESSING -> {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "${stageId.stepNumber}",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = stageId.shortTitle,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    maxLines = 1
                )
                Text(
                    text = when (status) {
                        StageStatus.COMPLETED -> "Done"
                        StageStatus.PROCESSING -> "Running..."
                        StageStatus.PENDING -> "Pending"
                        StageStatus.ERROR -> "Error"
                    },
                    fontSize = 9.sp,
                    color = when (status) {
                        StageStatus.COMPLETED -> NeonEmerald
                        StageStatus.PROCESSING -> NeonAmber
                        else -> TextMuted
                    }
                )
            }
        }
    }
}
