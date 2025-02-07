package pro.schacher.gpsrekorder.shared.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import dev.sargunv.maplibrecompose.compose.CameraState
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.rememberStyleState
import dev.sargunv.maplibrecompose.core.CameraMoveReason
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.OrnamentSettings
import gps_rekorder.shared.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import pro.schacher.gpsrekorder.shared.model.toPosition
import pro.schacher.gpsrekorder.shared.screen.map.MapScreenViewModel.State
import kotlin.time.Duration.Companion.seconds


private const val STYLE_DARK_URL = "files/styles/ark.json"
private const val STYLE_LIGHT_URL = "files/styles/light.json"

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapScreenViewModel = koinInject()
) {
    val state = viewModel.state.collectAsState()

    MapScreen(
        modifier = modifier,
        state = state.value,
        styleUrl = Res.getUri(
            if (MaterialTheme.colors.isLight) {
                STYLE_LIGHT_URL
            } else {
                STYLE_DARK_URL
            }
        ),
        onStartClick = viewModel::onRecordClick,
        onLocationButtonClick = viewModel::onLocationButtonClicked,
        omMapGesture = viewModel::onMapMoved,
        onSessionClick = viewModel::onSessionClicked,
        onCloseSessionClick = viewModel::onCloseSessionClicked
    )
}

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: State,
    styleUrl: String,
    onStartClick: () -> Unit,
    onLocationButtonClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    onCloseSessionClick: () -> Unit,
    omMapGesture: () -> Unit
) {
    Box(
        modifier.fillMaxSize()
    ) {
        Map(
            state = state,
            styleUrl = styleUrl,
            onMapGesture = omMapGesture,
            onSessionClick = onSessionClick
        )

        if (state.selectedSession != null) {
            LazyRow {
            }
            Card(
                modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.Black
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Id: ${state.selectedSession.id}",
                        color = Color.White,
                    )

                    Text(
                        "Points: ${state.selectedSession.path.size}",
                        color = Color.White,
                    )

                    Button(
                        onClick = onCloseSessionClick,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Close")
                    }
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
            val position = state.location?.latLng?.toPosition() ?: return@launch
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
            isScaleBarEnabled = false,
            isCompassEnabled = false,
        ),
        maximumFps = 60,
        cameraState = cameraState,
        styleUri = styleUrl
    ) {
        MapContent(state, cameraState, onSessionClick)
    }
}

@Composable
private fun MapContent(state: State, cameraState: CameraState, onSessionClick: (String) -> Unit) {
    SessionLayer(state.allSessions, onSessionClick)
    if (state.selectedSession != null) {
        SelectedSessionLayer(state.selectedSession)
    }
    RecordingLayer(state.path)
    LocationLayer(state.location, state.recording, cameraState)
}