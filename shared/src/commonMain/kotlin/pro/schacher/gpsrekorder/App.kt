package pro.schacher.gpsrekorder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.core.CameraPosition
import io.github.dellisd.spatialk.geojson.Position
import pro.schacher.gpsrekorder.model.LatLng
import pro.schacher.gpsrekorder.model.toPosition

@Composable
fun App() {
    Box(
        Modifier.fillMaxSize()
    ) {
        var latLng by remember {
            mutableStateOf(
                LatLng(
                    52.372661437130255,
                    9.739450741409224
                )
            )
        }

        val infiniteTransition = rememberInfiniteTransition()
        val zoom by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 10000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        val cameraState = rememberCameraState()
        cameraState.position = CameraPosition(
            target = latLng.toPosition(),
            zoom = zoom.toDouble()
        )

        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
        ) {
            // Add map layers here

        }
    }
}
