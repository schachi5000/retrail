package pro.schacher.gpsrekorder

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pro.schacher.gpsrekorder.shared.design.theme.AppTheme
import pro.schacher.gpsrekorder.shared.location.SimulatingLocationDataSource
import pro.schacher.gpsrekorder.shared.screen.map.MapScreen
import pro.schacher.gpsrekorder.shared.screen.map.MapScreenViewModel


val viewModels = module {
    viewModel { MapScreenViewModel(SimulatingLocationDataSource()) }
}

@Composable
fun App() {
    KoinApplication(application = {
        modules(viewModels)
    }) {
        AppTheme {
            MapScreen()
        }
    }
}

