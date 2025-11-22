package com.example.elgoharymusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage

private val DarkColorScheme = darkColorScheme(
    primary = ModernMusicColors.Primary,
    onPrimary = ModernMusicColors.OnPrimary,
    primaryContainer = ModernMusicColors.PrimaryContainer,
    secondary = ModernMusicColors.Secondary,
    onSecondary = ModernMusicColors.OnSecondary,
    tertiary = ModernMusicColors.Tertiary,
    background = ModernMusicColors.Background,
    onBackground = ModernMusicColors.OnBackground,
    surface = ModernMusicColors.Surface,
    onSurface = ModernMusicColors.OnSurface,
    surfaceVariant = ModernMusicColors.SurfaceVariant,
    onSurfaceVariant = ModernMusicColors.OnSurfaceVariant,
    error = ModernMusicColors.Error
)

private val LightColorScheme = lightColorScheme(
    primary = ModernMusicColors.Primary,
    onPrimary = ModernMusicColors.OnPrimary,
    primaryContainer = Color(0xFFEDE9FE),
    secondary = ModernMusicColors.Secondary,
    onSecondary = ModernMusicColors.OnSecondary,
    tertiary = ModernMusicColors.Tertiary,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF334155),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF64748B),
    error = ModernMusicColors.Error
)

// English Font Family
val EnglishFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semi_bold, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

// Arabic Font Family
val ArabicFontFamily = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_medium, FontWeight.Medium),
    Font(R.font.cairo_bold, FontWeight.Bold)
)

// Create Typography based on language
fun getTypography(language: AppLanguage): Typography {
    val fontFamily = when (language) {
        AppLanguage.ENGLISH -> EnglishFontFamily
        AppLanguage.ARABIC -> ArabicFontFamily
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.15.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.25.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.4.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
}

val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp)
)

@Composable
fun ElgoharyMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    val typography = getTypography(language)

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
