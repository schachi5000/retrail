import androidx.compose.ui.window.ComposeUIViewController
import pro.schacher.gpsrekorder.App
import pro.schacher.gpsrekorder.shared.database.DatabaseDriverFactory
import pro.schacher.gpsrekorder.shared.location.IOSLocationDataSource

fun MainViewController() = ComposeUIViewController {
    App(IOSLocationDataSource(), DatabaseDriverFactory())
}