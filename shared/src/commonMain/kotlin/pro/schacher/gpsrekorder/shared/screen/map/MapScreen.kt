package pro.schacher.gpsrekorder.shared.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.ClickResult
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraMoveReason
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.OrnamentSettings
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.interpolate
import dev.sargunv.maplibrecompose.expressions.dsl.linear
import dev.sargunv.maplibrecompose.expressions.dsl.zoom
import dev.sargunv.maplibrecompose.expressions.value.CirclePitchAlignment
import dev.sargunv.maplibrecompose.expressions.value.LineCap
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
import pro.schacher.gpsrekorder.shared.repository.Session
import pro.schacher.gpsrekorder.shared.screen.map.MapScreenViewModel.State
import kotlin.time.Duration.Companion.seconds


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
        onStartClick = viewModel::onRecordClick,
        onLocationButtonClick = viewModel::onLocationButtonClicked,
        omMapGesture = viewModel::onMapMoved,
        onSessionClick = viewModel::onSessionClicked
    )
}

@OptIn(ExperimentalResourceApi::class, ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: State,
    onStartClick: () -> Unit,
    onLocationButtonClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    omMapGesture: () -> Unit
) {
    Box(
        modifier.fillMaxSize()
    ) {
        Map(
            state = state,
            styleUrl = Res.getUri(STYLE_URL),
            onMapGesture = omMapGesture,
            onSessionClick = onSessionClick
        )

        if (state.activeSession != null) {

            Card(
                modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.Black
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Points: ${state.activeSession.path.size}",
                        color = Color.White,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            state.location?.let {
                FloatingActionButton(
                    onClick = onLocationButtonClick,
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
                if (state.recording) {
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
private fun Map(
    state: State,
    styleUrl: String,
    onMapGesture: () -> Unit = {},
    onSessionClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = state.initialCameraPosition.toPosition(),
            zoom = 15.0
        ),
    )

    LaunchedEffect(cameraState.moveReason) {
        if (cameraState.moveReason == CameraMoveReason.GESTURE) {
            onMapGesture()
        }
    }
    if (state.cameraTrackingActive) {
        scope.launch {
            val position = state.location?.toPosition() ?: return@launch
            cameraState.animateTo(
                CameraPosition(
                    target = position,
                    zoom = 15.0,
                    bearing = 0.0
                ),
                duration = 2.seconds
            )
        }

    }
    MaplibreMap(
        modifier = Modifier.fillMaxSize(),
        ornamentSettings = OrnamentSettings(
            isLogoEnabled = false,
            isScaleBarEnabled = false
        ),
        cameraState = cameraState,
        styleUri = styleUrl
    ) {
        MapContent(state, onSessionClick)
    }
}

@Composable
private fun MapContent(state: State, onSessionClick: (String) -> Unit) {
    SessionLayer(state.allSessions, onSessionClick)
    RecordingLayer(state.path)
    LocationLayer(state.location)
}

private val recordingLocationColor = Color(0xFFff0e3f)
private val recordingStrokeColor = Color(0XFFdc002d)

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
        color = const(recordingLocationColor),
        radius = const(6.dp),
        strokeColor = const(recordingStrokeColor),
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
fun RecordingLayer(path: List<LatLng>) {
    val pathSource = rememberGeoJsonSource(
        id = "path",
        data = FeatureCollection(features = emptyList())
    )

    LineLayer(
        id = "path-line-layer",
        source = pathSource,
        color = const(recordingStrokeColor),
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
        color = const(recordingLocationColor),
        radius = const(4.dp),
        strokeColor = const(recordingStrokeColor),
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

private val sessionLocationColor = Color(0xFF00DDFF)
private val sessionStrokeColor = Color(0XFF008D9B)

@Composable
fun SessionLayer(sessions: List<Session>, onSessionClick: (String) -> Unit) {
    val pathSource = rememberGeoJsonSource(
        id = "sessions",
        data = FeatureCollection(features = emptyList())
    )

    val featureClickHandler = { it: List<Feature> ->
        it.find { it.properties.containsKey("sessionId") }
            ?.getStringProperty("sessionId")
            ?.let {
                onSessionClick(it)
                ClickResult.Consume
            }
            ?: ClickResult.Pass
    }

    LineLayer(
        id = "session-line-layer",
        source = pathSource,
        color = const(sessionStrokeColor),
        width = interpolate(
            type = linear(),
            input = zoom(),
            7 to const(2.dp),
            15 to const(4.dp),
        ),
        cap = const(LineCap.Round),
        onClick = featureClickHandler
    )

    CircleLayer(
        id = "session-dot-layer",
        source = pathSource,
        color = const(sessionLocationColor),
        radius = const(4.dp),
        strokeColor = const(sessionStrokeColor),
        strokeWidth = const(1.5.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
        minZoom = 15f,
        onClick = featureClickHandler
    )

    pathSource.setData(
        FeatureCollection(
            sessions.map { session ->
                Feature(LineString(session.path.map { it.toPosition() })).also {
                    it.setStringProperty("sessionId", session.id)
                }
            }
        )
    )
}