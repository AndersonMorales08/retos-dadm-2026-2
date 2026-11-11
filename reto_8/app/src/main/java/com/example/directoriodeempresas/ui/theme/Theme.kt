package com.example.directoriodeempresas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF17794F), onPrimary = Color.White,
    primaryContainer = Color(0xFFE1EFE6), onPrimaryContainer = Color(0xFF0F5238),
    secondary = Color(0xFF0F5238), onSecondary = Color.White,
    background = Color(0xFFF3F6F3), onBackground = Color(0xFF14301F),
    surface = Color.White, onSurface = Color(0xFF14301F),
    onSurfaceVariant = Color(0xFF6B7A70),
    outline = Color(0xFFC3D1C7), outlineVariant = Color(0xFFD9E3DC),
    error = Color(0xFFC03434), onError = Color.White,
    errorContainer = Color(0xFFFDE8E8), onErrorContainer = Color(0xFFC03434),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5FCB97), onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF1F3A2B), onPrimaryContainer = Color(0xFFBFEBD3),
    secondary = Color(0xFF2E8F66), onSecondary = Color.White,
    background = Color(0xFF0E1511), onBackground = Color(0xFFE2EBE5),
    surface = Color(0xFF16201A), onSurface = Color(0xFFE2EBE5),
    onSurfaceVariant = Color(0xFF9DB0A3),
    outline = Color(0xFF3B4A41), outlineVariant = Color(0xFF2A372F),
    error = Color(0xFFFF8A8A), onError = Color(0xFF4D0000),
    errorContainer = Color(0xFF3A1B1B), onErrorContainer = Color(0xFFFFB4B4),
)

private val base = Typography()
private val DirectorioDeEmpresasTypography = base.copy(
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)

private val DirectorioDeEmpresasShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun DirectorioDeEmpresasTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DirectorioDeEmpresasTypography,
        shapes = DirectorioDeEmpresasShapes,
        content = content,
    )
}
