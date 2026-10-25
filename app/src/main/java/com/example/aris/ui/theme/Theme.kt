
package com.example.aris.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = VerdeAgua,
    onPrimary = AzulAris,
    secondary = AzulAris,
    onSecondary = BlancoAris,
    tertiary = VerdeAgua,
    onTertiary = AzulAris,
    background = FondoAris,
    onBackground = TextoAris,
    surface = BlancoAris,
    onSurface = TextoAris,
    surfaceVariant = AzulSuave,
    onSurfaceVariant = AzulAris
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeAguaOscuro,
    onPrimary = FondoOscuro,
    secondary = AzulAris,
    onSecondary = BlancoAris,
    tertiary = VerdeAguaOscuro,
    onTertiary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = AzulAris,
    onSurfaceVariant = TextoClaro
)

@Composable
fun ArisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema = if (darkTheme) {
        EsquemaOscuro
    } else {
        EsquemaClaro
    }

    MaterialTheme(
        colorScheme = esquema,
        typography = Typography,
        content = content
    )
}
