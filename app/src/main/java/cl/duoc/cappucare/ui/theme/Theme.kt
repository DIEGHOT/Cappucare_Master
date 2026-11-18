package cl.duoc.cappucare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = TealPrimario,
    onPrimary = Color.White,
    primaryContainer = TealContenedor,
    secondary = GrisAzulado,
    tertiary = VerdeTerciario,
    error = RojoError,
    errorContainer = RojoContenedor
)

private val EsquemaOscuro = darkColorScheme(
    primary = TealPrimarioOscuro,
    onPrimary = Color(0xFF00201D),
    primaryContainer = TealContenedorOscuro,
    secondary = GrisAzuladoClaro,
    tertiary = VerdeTerciarioClaro,
    error = RojoErrorClaro,
    errorContainer = RojoContenedorOscuro
)

@Composable
fun AppCappucareTheme(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (temaOscuro) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
