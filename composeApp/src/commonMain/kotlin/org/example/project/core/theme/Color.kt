package org.example.project.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 1. アプリの基本となる色を16進数で定義
//    ブランドカラー、ニュートラルカラーなど
val OrangePrimary = Color(0xFFC65F20)
val DarkContainer = Color(0xFF202020)
val GraySecondary = Color(0xFF434343)
val DarkText = Color(0xFFD7D7D7)
val LightBackground = Color(0xFFFFFFFF)

val LightSurface = Color(0xFFFDFDFD)

val LightContainer = Color(0xFFF8F8F8)
val DarkBackground = Color(0xFF191919)

val DarkSurface = Color(0xFF181818)

val OnDark = Color(0xFFFCFCFC)
val ErrorRed = Color(0xFFBA1A1A)

// Accent colors for special states
val SuccessGreen = Color(0xFF4CAF50)

// 2. ライトテーマ用のカラーパレットを定義
val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = LightContainer,
    onPrimaryContainer = Color.Black,
    secondary = GraySecondary,
    onSecondary = Color.Black,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color.Black,
    surface = LightSurface,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF5C5C5C),
    outline = Color(0xFFB0B0B0),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

// 3. ダークテーマ用のカラーパレットを定義
val DarkColors = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = OnDark,
    primaryContainer = DarkContainer,
    onPrimaryContainer = OnDark,
    secondary = GraySecondary,
    onSecondary = OnDark,
    tertiary = SuccessGreen,
    onTertiary = OnDark,
    background = DarkBackground,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = Color(0xFF363636),
    onSurfaceVariant = Color(0xFFC0C0C0),
    outline = Color(0xFF6E6E6E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)
