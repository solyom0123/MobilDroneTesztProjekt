package com.example.mobildrontesztprojekt.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobildrontesztprojekt.ui.theme.DroneAccent
import com.example.mobildrontesztprojekt.ui.theme.DroneBackground
import com.example.mobildrontesztprojekt.ui.theme.DronePrimary
import com.example.mobildrontesztprojekt.ui.theme.DroneSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2800)
        onNavigateToLogin()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotation"
    )

    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing)),
        label = "radar"
    )

    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    var textVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        textVisible = true
    }
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "textAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF050A14), DroneBackground, Color(0xFF0A1628))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Radar körök
        Canvas(modifier = Modifier.size(320.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            for (i in 1..3) {
                val progress = (radarPulse + i / 3f) % 1f
                drawCircle(
                    color = DroneAccent.copy(alpha = (1f - progress) * 0.4f),
                    radius = size.minDimension / 2 * progress,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            // Statikus körök
            for (i in 1..4) {
                drawCircle(
                    color = DronePrimary.copy(alpha = 0.2f),
                    radius = size.minDimension / 2 * (i / 4f),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ikon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Flight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .rotate(rotation),
                    tint = DroneSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "DRONTECH",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(DroneSecondary, DroneAccent)
                    )
                ),
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )

            Text(
                text = "Smart Warehouse Management",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )
        }
    }
}
