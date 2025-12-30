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
    error = ErrorRed,
    onError = Color.White,
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
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)
