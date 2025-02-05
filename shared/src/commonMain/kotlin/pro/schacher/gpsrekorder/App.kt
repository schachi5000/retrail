package pro.schacher.gpsrekorder

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pro.schacher.gpsrekorder.shared.design.theme.AppTheme
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.repository.SessionRepository
import pro.schacher.gpsrekorder.shared.screen.map.MapScreen
import pro.schacher.gpsrekorder.shared.screen.map.MapScreenViewModel

val repositories = module {
    singleOf(::SessionRepository)
}

val viewModels = module {
    viewModelOf(::MapScreenViewModel)
}

@Composable
fun App(locationDataSource: LocationDataSource) {
    KoinApplication(
        application = {
            modules(
                module { single<LocationDataSource> { locationDataSource } },
                repositories,
                viewModels
            )
        }) {
        AppTheme {
            MapScreen()
        }
    }
}

