package pro.schacher.gpsrekorder

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import pro.schacher.gpsrekorder.shared.design.theme.AppTheme
import pro.schacher.gpsrekorder.shared.screen.map.MapScreen

@Composable
fun App() {
    KoinApplication(application = {
    }) {
        AppTheme {
            MapScreen()
        }
    }
}

