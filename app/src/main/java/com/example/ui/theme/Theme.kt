package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = LightBluePrimary,
    secondary = LightBlueSecondary,
    tertiary = LightBlueTertiary,
    background = DarkBlueText,
    surface = DarkBlueText,
    error = RedCalled
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightBluePrimary,
    secondary = LightBlueSecondary,
    tertiary = LightBlueTertiary,
    background = LightBlueBackground,
    surface = LightBlueSurface,
    onPrimary = LightBlueSurface,
    onSecondary = LightBlueSurface,
    onBackground = DarkBlueText,
    onSurface = DarkBlueText,
    error = RedCalled,
    outline = SlateOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor by default to enforce the user's requested light blue theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> LightColorScheme // Prefer beautiful blue scheme even in dark mode for consistency
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

