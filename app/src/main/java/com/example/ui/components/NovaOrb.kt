package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaViolet
import com.example.viewmodel.AssistantState

@Composable
fun NovaOrb(
    state: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nova_orb_transition")

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = when (state) {
            AssistantState.LISTENING -> 0.95f
            AssistantState.SPEAKING -> 0.92f
            AssistantState.THINKING -> 0.96f
            AssistantState.IDLE -> 0.98f
        },
        targetValue = when (state) {
            AssistantState.LISTENING -> 1.28f
            AssistantState.SPEAKING -> 1.18f
            AssistantState.THINKING -> 1.08f
            AssistantState.IDLE -> 1.04f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.LISTENING -> 700
                    AssistantState.SPEAKING -> 900
                    AssistantState.THINKING -> 1100
                    AssistantState.IDLE -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val coreColors = when (state) {
        AssistantState.LISTENING -> listOf(NovaMint, NovaCyan, NovaViolet)
        AssistantState.SPEAKING -> listOf(NovaViolet, NovaPink, NovaCyan)
        AssistantState.THINKING -> listOf(NovaCyan, NovaPink, NovaViolet)
        AssistantState.IDLE -> listOf(NovaCyan, NovaViolet, NovaBackground)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size * 1.5f)
            .clickable(onClick = onClick)
            .testTag("nova_animated_orb")
    ) {
        // Outer glowing aura ring
        if (state != AssistantState.IDLE) {
            Box(
                modifier = Modifier
                    .size(size * 1.4f)
                    .scale(scalePulse * 1.15f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                coreColors.first().copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Mid reactive halo
        Box(
            modifier = Modifier
                .size(size * 1.15f)
                .scale(scalePulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            coreColors[1].copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Core celestial orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .rotate(if (state == AssistantState.THINKING) rotation else 0f)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(coreColors)
                )
                .border(1.5.dp, coreColors.first(), CircleShape)
        ) {
            Icon(
                imageVector = when (state) {
                    AssistantState.LISTENING -> Icons.Default.Mic
                    AssistantState.SPEAKING -> Icons.Default.VolumeUp
                    AssistantState.THINKING -> Icons.Default.Psychology
                    AssistantState.IDLE -> Icons.Default.GraphicEq
                },
                contentDescription = "Nova Assistant Orb",
                tint = NovaBackground,
                modifier = Modifier.size(size * 0.42f)
            )
        }
    }
}

/**
 * Animated audio waveform visualizer bars for listening and speaking states
 */
@Composable
fun AudioWaveVisualizer(
    isActive: Boolean,
    color: Color = NovaMint,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = if (isActive) 24f else 4f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = if (isActive) 28f else 6f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = if (isActive) 32f else 4f,
        animationSpec = infiniteRepeatable(tween(290, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = if (isActive) 26f else 8f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = if (isActive) 20f else 4f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(36.dp)
    ) {
        val heights = listOf(h1, h2, h3, h4, h5)
        for (h in heights) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
