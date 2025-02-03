package pro.schacher.gpsrekorder.shared.screen.map

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
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.toPosition

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxSize()
    ) {
        val latLng by remember {
            mutableStateOf(
                LatLng(52.372661437130255, 9.739450741409224)
            )
        }

        val cameraState = rememberCameraState(
            firstPosition = CameraPosition(
                target = latLng.toPosition(),
                zoom = 15.0
            ),
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
        color = const(Color(0xFF00DDFF)),
    )
}
