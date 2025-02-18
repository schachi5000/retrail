package pro.schacher.gpsrekorder.shared.screen.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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


private const val STYLE_DARK_URL = "files/styles/dark.json"
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
        onCloseSessionClick = viewModel::onCloseSessionClicked,
        onDeleteSessionClick = viewModel::onDeleteSessionClicked
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
    onDeleteSessionClick: (String) -> Unit,
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomStart),
            horizontalAlignment = Alignment.End
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AnimatedVisibility(
                    visible = state.location != null && !state.cameraTrackingActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
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


            val pagerState = rememberPagerState(
                initialPage = state.allSessions.indexOfFirst { it.id == state.selectedSession?.id }
                    .takeIf { it >= 0 } ?: 0,
                pageCount = { state.allSessions.size })

            if (state.allSessions.isNotEmpty()) {
                LaunchedEffect(pagerState.currentPage) {
                    onSessionClick(state.allSessions[pagerState.currentPage].id)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.wrapContentHeight(),
                contentPadding = PaddingValues(end = 32.dp)
            ) { page ->
                val session = state.allSessions.getOrNull(page) ?: return@HorizontalPager

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.Black
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Id: ${session.id}",
                            color = Color.White,
                        )

                        Text(
                            "Points: ${session.path.size}",
                            color = Color.White,
                        )

                        Button(
                            onClick = { onDeleteSessionClick(session.id) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Delete")
                        }
                    }
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

    state.selectedSession?.let {
        LaunchedEffect(state.selectedSession.id) {
            cameraState.animateTo(
                CameraPosition(
                    target = state.selectedSession.path.getOrNull(0)?.latLng?.toPosition()
                        ?: return@LaunchedEffect,
                    zoom = 15.0,
                    bearing = 0.0
                ),
                duration = 2.seconds
            )
        }
    }

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