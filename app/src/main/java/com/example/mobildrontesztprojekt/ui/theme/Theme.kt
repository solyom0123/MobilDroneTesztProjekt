package com.example.mobildrontesztprojekt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DroneDarkColorScheme = darkColorScheme(
    primary = DronePrimary,
    onPrimary = DroneOnPrimary,
    primaryContainer = DronePrimaryVar,
    secondary = DroneSecondary,
    onSecondary = DroneOnSecondary,
    secondaryContainer = DroneAccent,
    background = DroneBackground,
    onBackground = DroneOnBackground,
    surface = DroneSurface,
    onSurface = DroneOnSurface,
    error = DroneError
)

@Composable
fun DroneTechTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DroneDarkColorScheme,
        content = content
    )
}
