package pro.schacher.gpsrekorder.shared.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primary = Color(0XFF0000000)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme())
            darkColors(
                primary = primary,
                secondary = primary
            )
        else
            lightColors(
                primary = primary,
                secondary = primary
            ),
    ) {
        content()
    }
}