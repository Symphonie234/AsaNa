package ph.asana.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AsaNaBlue = Color(0xFF1098D8)

private val LightColors = lightColorScheme(
    primary = AsaNaBlue,
    onPrimary = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = AsaNaBlue,
    onPrimary = Color.White,
)

@Composable
fun AsaNaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
