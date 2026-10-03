package cs.uwaterloo.timora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TimoraGolden = Color(0xFFD4AF68)
private val TimoraOlive = Color(0xFF8B7E4F)
private val TimoraCream = Color(0xFFFFF4E0)
private val TimoraDarkOlive = Color(0xFF6B5F3F)
private val LightColorScheme = lightColorScheme(
    primary = TimoraGolden,
    onPrimary = Color.White,
    primaryContainer = TimoraGolden,
    onPrimaryContainer = Color.White,

    secondary = TimoraOlive,
    onSecondary = Color.White,
    secondaryContainer = TimoraCream,
    onSecondaryContainer = TimoraDarkOlive,

    tertiary = TimoraOlive,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEEE8D5),
    onTertiaryContainer = TimoraDarkOlive,

    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF3E2723),
    surface = Color.White,
    onSurface = Color(0xFF3E2723),

    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF6D4C41),
)

@Composable
fun TimoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
