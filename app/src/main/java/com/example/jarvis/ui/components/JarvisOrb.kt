package com.example.jarvis.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.jarvis.model.JarvisState
import com.example.jarvis.ui.theme.JarvisAmber
import com.example.jarvis.ui.theme.JarvisCyan
import com.example.jarvis.ui.theme.JarvisCyanBright
import com.example.jarvis.ui.theme.JarvisElectricBlue
import com.example.jarvis.ui.theme.JarvisPurpleHighlight
import com.example.jarvis.ui.theme.JarvisRed
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity animated JARVIS Orb component featuring spring-based state transitions
 * between IDLE, LISTENING, and SPEAKING states with cybernetic HUD rings, acoustic ripples,
 * and vocal frequency arcs.
 */
@Composable
fun JarvisOrb(
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    state: JarvisState = JarvisState.IDLE,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Outer continuous rotation
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_outer"
    )

    // Inner counter-rotation
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_inner"
    )

    // State-dependent breathing / pulsing animation
    val pulseBreathing by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 850
                    JarvisState.SPEAKING -> 550
                    JarvisState.THINKING -> 450
                    else -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_breathing"
    )

    // Dynamic acoustic wave expansion for LISTENING state
    val listeningRipple by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_ripple"
    )

    // Dynamic vocal frequency oscillations for SPEAKING state
    val speakingHarmonic by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_harmonic"
    )

    // State transition spring physics (equivalent to Framer Motion spring)
    val stateScale by animateFloatAsState(
        targetValue = when (state) {
            JarvisState.LISTENING -> 1.15f
            JarvisState.SPEAKING -> 1.10f
            JarvisState.THINKING -> 1.05f
            JarvisState.ERROR -> 0.95f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "state_scale"
    )

    val targetPrimary = when (state) {
        JarvisState.IDLE -> JarvisCyan
        JarvisState.LISTENING -> JarvisCyanBright
        JarvisState.SPEAKING -> JarvisElectricBlue
        JarvisState.THINKING -> JarvisCyan
        JarvisState.ERROR -> JarvisRed
    }

    val targetSecondary = when (state) {
        JarvisState.IDLE -> JarvisElectricBlue
        JarvisState.LISTENING -> JarvisCyan
        JarvisState.SPEAKING -> JarvisCyanBright
        JarvisState.THINKING -> JarvisElectricBlue
        JarvisState.ERROR -> JarvisAmber
    }

    val primaryColor by animateColorAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "primary_color"
    )

    val secondaryColor by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "secondary_color"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = when (state) {
            JarvisState.LISTENING -> 0.65f
            JarvisState.SPEAKING -> 0.75f
            JarvisState.THINKING -> 0.55f
            else -> 0.38f
        },
        animationSpec = tween(durationMillis = 400),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = size.toPx() * 0.40f * stateScale

            // 1. Ambient Glow Field (Cyan/Electric Blue with subtle Purple Highlights)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha * pulseBreathing),
                        secondaryColor.copy(alpha = glowAlpha * 0.5f),
                        JarvisPurpleHighlight.copy(alpha = glowAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.45f
                ),
                radius = baseRadius * 1.45f,
                center = center
            )

            // 2. State-Specific Visual Layers
            when (state) {
                JarvisState.LISTENING -> {
                    // Acoustic ripples expanding outward
                    val rippleRadius = baseRadius * listeningRipple
                    val rippleAlpha = (1f - (listeningRipple - 0.5f) / 0.85f).coerceIn(0f, 1f) * 0.7f
                    drawCircle(
                        color = JarvisCyanBright.copy(alpha = rippleAlpha),
                        radius = rippleRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    val innerRipple = baseRadius * ((listeningRipple + 0.4f) % 0.85f + 0.5f)
                    val innerAlpha = (1f - (innerRipple / baseRadius - 0.5f)).coerceIn(0f, 1f) * 0.45f
                    drawCircle(
                        color = primaryColor.copy(alpha = innerAlpha),
                        radius = innerRipple,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                JarvisState.SPEAKING -> {
                    // Harmonic Soundwave Equalizer Arcs around perimeter
                    val arcRadius = baseRadius * 1.08f
                    val arcCount = 16
                    val step = 360f / arcCount
                    for (i in 0 until arcCount) {
                        val angle = (i * step) + (rotationOuter * 0.5f)
                        val rad = Math.toRadians(angle.toDouble())
                        // Modulation based on harmonic
                        val barHeight = (4.dp.toPx() + (8.dp.toPx() * ((i % 4 + 1) / 4f) * speakingHarmonic))
                        val startR = arcRadius
                        val endR = arcRadius + barHeight

                        val start = Offset(
                            center.x + (startR * cos(rad)).toFloat(),
                            center.y + (startR * sin(rad)).toFloat()
                        )
                        val end = Offset(
                            center.x + (endR * cos(rad)).toFloat(),
                            center.y + (endR * sin(rad)).toFloat()
                        )

                        drawLine(
                            brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)),
                            start = start,
                            end = end,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                else -> {
                    // IDLE state subtle orbital node points
                    val orbitRadius = baseRadius * 1.06f
                    for (i in 0 until 6) {
                        val angle = (i * 60f) + (rotationOuter * 0.3f)
                        val rad = Math.toRadians(angle.toDouble())
                        val pt = Offset(
                            center.x + (orbitRadius * cos(rad)).toFloat(),
                            center.y + (orbitRadius * sin(rad)).toFloat()
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.5f),
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // 3. Outer Segmented Ring
            rotate(rotationOuter, pivot = center) {
                drawSegmentedRing(
                    center = center,
                    radius = baseRadius,
                    color = primaryColor.copy(alpha = 0.75f),
                    strokeWidth = 2.5.dp.toPx(),
                    segments = 6,
                    gapDegrees = 20f
                )
                drawTickMarks(
                    center = center,
                    radius = baseRadius - 8.dp.toPx(),
                    color = primaryColor.copy(alpha = 0.45f),
                    count = 24
                )
            }

            // 4. Counter-rotating Middle Ring with HUD Reticles
            rotate(rotationInner, pivot = center) {
                drawSegmentedRing(
                    center = center,
                    radius = baseRadius * 0.75f,
                    color = secondaryColor.copy(alpha = 0.85f),
                    strokeWidth = 2.dp.toPx(),
                    segments = 4,
                    gapDegrees = 35f
                )
                // Diagonal crosshairs / HUD reticles
                val armLen = 14.dp.toPx()
                for (angle in listOf(45f, 135f, 225f, 315f)) {
                    val rad = Math.toRadians(angle.toDouble())
                    val r1 = baseRadius * 0.75f - armLen
                    val r2 = baseRadius * 0.75f + armLen
                    drawLine(
                        color = primaryColor.copy(alpha = 0.55f),
                        start = Offset(center.x + (r1 * cos(rad)).toFloat(), center.y + (r1 * sin(rad)).toFloat()),
                        end = Offset(center.x + (r2 * cos(rad)).toFloat(), center.y + (r2 * sin(rad)).toFloat()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // 5. Central Reactor Core (Breathing + State Pulse)
            val coreRadius = (baseRadius * 0.46f) * pulseBreathing
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        secondaryColor.copy(alpha = 0.85f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 6. Core Inner Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.95f),
                radius = coreRadius * 0.68f,
                center = center,
                style = Stroke(width = 1.8.dp.toPx())
            )

            // 7. Radiant Center Point
            drawCircle(
                color = Color.White,
                radius = 4.5.dp.toPx() * pulseBreathing,
                center = center
            )
        }
    }
}

private fun DrawScope.drawSegmentedRing(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
    segments: Int,
    gapDegrees: Float
) {
    val totalSpan = 360f - (segments * gapDegrees)
    val sweepAngle = totalSpan / segments
    var currentAngle = 0f

    val rectSize = Size(radius * 2, radius * 2)
    val topLeft = Offset(center.x - radius, center.y - radius)

    for (i in 0 until segments) {
        drawArc(
            color = color,
            startAngle = currentAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = rectSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        currentAngle += sweepAngle + gapDegrees
    }
}

private fun DrawScope.drawTickMarks(
    center: Offset,
    radius: Float,
    color: Color,
    count: Int
) {
    val step = 360f / count
    val tickLength = 5.dp.toPx()
    for (i in 0 until count) {
        val angle = Math.toRadians((i * step).toDouble())
        val startX = center.x + (radius * cos(angle)).toFloat()
        val startY = center.y + (radius * sin(angle)).toFloat()
        val endX = center.x + ((radius - tickLength) * cos(angle)).toFloat()
        val endY = center.y + ((radius - tickLength) * sin(angle)).toFloat()
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
