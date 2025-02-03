package pro.schacher.gpsrekorder.shared.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.OrnamentSettings
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.exponential
import dev.sargunv.maplibrecompose.expressions.dsl.interpolate
import dev.sargunv.maplibrecompose.expressions.dsl.linear
import dev.sargunv.maplibrecompose.expressions.dsl.zoom
import dev.sargunv.maplibrecompose.expressions.value.CirclePitchAlignment
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.TranslateAnchor
import gps_rekorder.shared.generated.resources.Res
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.toPosition
import pro.schacher.gpsrekorder.shared.screen.map.MapScreenViewModel.State
import kotlin.time.Duration.Companion.milliseconds


private const val STYLE_URL = "files/style.json"

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapScreenViewModel = koinInject()
) {
    val state = viewModel.state.collectAsState()

    MapScreen(
        modifier = modifier,
        state = state.value,
        onStartClick = viewModel::onRecordClick
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: State,
    onStartClick: () -> Unit
) {
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
            ornamentSettings = OrnamentSettings(
                isLogoEnabled = false,
                isScaleBarEnabled = false
            ),
            cameraState = cameraState,
            styleUri = Res.getUri(STYLE_URL)
        ) {
            MapContent(state.location, state.path)
        }

        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            state.location?.let {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraState.animateTo(
                                CameraPosition(
                                    target = it.toPosition(),
                                    zoom = 15.0
                                ),
                                duration = 1000.milliseconds
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Stop"
                    )
                }
            }

            FloatingActionButton(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onStartClick,
                shape = RoundedCornerShape(16.dp),

                ) {
                if (state.active) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Stop"
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start"
                    )
                }
            }
        }
    }
}

@Composable
private fun MapContent(latLng: LatLng?, path: List<LatLng>) {
    PathLayer(path)
    LocationLayer(latLng)
}

private val locationColor = Color(0xFF00DDFF)
private val strokeColor = Color(0XFF008D9B)

@Composable
fun LocationLayer(latLng: LatLng?) {
    val locationSource = rememberGeoJsonSource(
        id = "location",
        data = FeatureCollection(
            features = emptyList()
        )
    )

    CircleLayer(
        id = "location-layer",
        source = locationSource,
        color = const(locationColor),
        radius = const(6.dp),
        strokeColor = const(strokeColor),
        strokeWidth = const(1.5.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )

    val locationFeature = latLng?.toPosition()?.let {
        Feature(Point(coordinates = it))
    }

    locationFeature?.let {
        locationSource.setData(it)
    }
}

@Composable
fun PathLayer(path: List<LatLng>) {
    val pathSource = rememberGeoJsonSource(
        id = "path",
        data = FeatureCollection(features = emptyList())
    )

    LineLayer(
        id = "path-line-layer",
        source = pathSource,
        color = const(strokeColor),
        width = interpolate(
            type = linear(),
            input = zoom(),
            7 to const(2.dp),
            15 to const(4.dp),
        ),
        cap = const(LineCap.Round),
    )

    CircleLayer(
        id = "path-dot-layer",
        source = pathSource,
        color = const(locationColor),
        radius = const(4.dp),
        strokeColor = const(strokeColor),
        strokeWidth = const(1.5.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
        minZoom = 15f
    )

    pathSource.setData(
        FeatureCollection(
            features = if (path.size >= 2) {
                val positions = path.map { it.toPosition() }
                listOf(Feature(LineString(positions))) + positions.map { Feature(Point(it)) }
            } else {
                emptyList()
            }
        )
    )
}