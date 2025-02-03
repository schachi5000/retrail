package pro.schacher.gpsrekorder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.expressions.dsl.const
import gps_rekorder.shared.generated.resources.Res
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.Point
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pro.schacher.gpsrekorder.model.LatLng
import pro.schacher.gpsrekorder.model.toPosition

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    Box(
        Modifier.fillMaxSize()
    ) {
        val latLng by remember {
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
            styleUri = Res.getUri("files/style.json")
        ) {
            MapContent(latLng)
        }
    }
}

@Composable
private fun MapContent(latLng: LatLng) {
    val locationSource = rememberGeoJsonSource(
        id = "location-source",
        data = Feature(Point(latLng.toPosition()))
    )

    CircleLayer(
        id = "location-layer",
        source = locationSource,
        color = const(Color.Blue)
    )
}
