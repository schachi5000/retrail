package pro.schacher.gpsrekorder.shared.screen.map

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sargunv.maplibrecompose.compose.CameraState
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.core.CameraMoveReason
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.OrnamentSettings
import gps_rekorder.shared.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import pro.schacher.gpsrekorder.shared.AppLogger
import pro.schacher.gpsrekorder.shared.model.Session
import pro.schacher.gpsrekorder.shared.model.toPosition
import pro.schacher.gpsrekorder.shared.screen.map.MapScreenViewModel.State
import pro.schacher.gpsrekorder.shared.utils.AnimatedValueVisibility
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

@OptIn(ExperimentalAnimationApi::class)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomStart),
        ) {
            Column(modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)) {
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
            }

            RecordButton(
                modifier = Modifier.navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter),
                state = state,
                onClick = onStartClick
            )
        }

        AnimatedValueVisibility(state.selectedSession) {
            Column(
                modifier = Modifier.statusBarsPadding()
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                SessionCard(it, onDeleteSessionClick, onCloseSessionClick)
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: Session,
    onDeleteClick: (String) -> Unit,
    onCloseClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onDeleteClick(session.id) },
                ) {
                    Text("Delete")
                }

                Button(
                    onClick = { onCloseClick() },
                ) {
                    Text("Close")
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

@Composable
private fun RecordButton(modifier: Modifier = Modifier, state: State, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = if (state.recording) {
            Color.Red
        } else {
            MaterialTheme.colors.secondary
        }
    )

    FloatingActionButton(
        modifier = modifier
            .wrapContentSize(),
        backgroundColor = color,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
    ) {
        AnimatedContent(targetState = state.recording) {
            val rotation by rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                )
            )

            Row {
                if (it) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(
                            modifier = Modifier.size(36.dp).align(Alignment.Center)
                                .rotate(rotation)
                        ) {
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f,
                                style = Stroke(
                                    width = 6.dp.value,
                                    pathEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(10f, 10f),
                                        phase = 10f
                                    )
                                )
                            )
                        }

                        Canvas(
                            modifier = Modifier.size(16.dp)
                        ) {
                            drawRoundRect(
                                color = Color.White,
                                size = size,
                                cornerRadius = CornerRadius(10.dp.value),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Canvas(
                            modifier = Modifier.size(16.dp)
                        ) {
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f,
                                style = Stroke(
                                    width = 7.dp.value,
                                )
                            )
                        }

                        Text(
                            modifier = Modifier.padding(start = 12.dp),
                            text = "Record",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}