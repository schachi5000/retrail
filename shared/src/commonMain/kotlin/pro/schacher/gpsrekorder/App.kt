package pro.schacher.gpsrekorder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sargunv.maplibrecompose.compose.MaplibreMap

@Composable
fun App() {
    Box(
        Modifier.fillMaxSize()
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
