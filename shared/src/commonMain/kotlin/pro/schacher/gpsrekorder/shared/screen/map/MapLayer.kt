package pro.schacher.gpsrekorder.shared.screen.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.CameraState
import dev.sargunv.maplibrecompose.compose.ClickResult
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.expressions.dsl.asNumber
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.dp
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.interpolate
import dev.sargunv.maplibrecompose.expressions.dsl.linear
import dev.sargunv.maplibrecompose.expressions.dsl.zoom
import dev.sargunv.maplibrecompose.expressions.value.CirclePitchAlignment
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session
import pro.schacher.gpsrekorder.shared.model.toPosition
import pro.schacher.gpsrekorder.shared.utils.dpPerMeterAtTarget

private val recordingLocationColor = Color(0xFFff0e3f)
private val recordingStrokeColor = Color(0XFFdc002d)

private val sessionPointColor = Color(0xffffffff)
private val sessionStrokeColor = Color(0Xff000000)

private val selectedSessionPointColor = Color(0xFF00DDFF)
private val selectedSessionStrokeColor = Color(0XFF008D9B)

private const val MIN_ZOOM = 10f

private val circleRadius = interpolate(
    type = linear(),
    input = zoom(),
    7 to const(1.dp),
    18 to const(4.dp),
)

private val circleStrokeWidth = interpolate(
    type = linear(),
    input = zoom(),
    7 to const(1.dp),
    18 to const(3.dp),
)

private val lineWidth = interpolate(
    type = linear(),
    input = zoom(),
    7 to const(2.dp),
    15 to const(4.dp),
)

@Composable
fun LocationLayer(location: Location?, recording: Boolean, cameraState: CameraState) {
    val metersPerDp = cameraState.dpPerMeterAtTarget
    val targetRadius = location?.accuracy?.let { (metersPerDp * it.inMeters) } ?: 0.0
    val radius by animateDpAsState(targetRadius.dp)

    val locationFeature = location?.let {
        Feature(Point(coordinates = it.latLng.toPosition())).also { feature ->
            feature.setNumberProperty("radius", radius.value.toDouble())
        }
    }?.let { listOf(it) } ?: emptyList<Feature>()

    val locationSource = rememberGeoJsonSource(
        id = "location",
        data = FeatureCollection(locationFeature)
    )

    CircleLayer(
        id = "location-accuracy-layer",
        source = locationSource,
        color = const(
            if (recording) {
                recordingLocationColor
            } else {
                MaterialTheme.colors.primary
            }.copy(alpha = 0.4f)
        ),
        radius = feature.get("radius").asNumber().dp,
        strokeColor = const(
            if (recording) recordingStrokeColor else MaterialTheme.colors.primary
        ),
        strokeWidth = const(2.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )

    CircleLayer(
        id = "location-layer",
        source = locationSource,
        color = const(if (recording) recordingLocationColor else Color.White),
        radius = const(5.dp),
        strokeColor = const(if (recording) recordingStrokeColor else MaterialTheme.colors.primary),
        strokeWidth = const(3.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )
}

@Composable
fun RecordingLayer(path: List<Location>) {
    val featureCollection = FeatureCollection(
        features = if (path.size >= 2) {
            val positions = path.map { it.latLng.toPosition() }
            listOf(Feature(LineString(positions))) + positions.map { Feature(Point(it)) }
        } else {
            emptyList()
        }
    )

    val pathSource = rememberGeoJsonSource(
        id = "path",
        data = featureCollection
    )

    LineLayer(
        id = "path-line-layer",
        source = pathSource,
        color = const(recordingStrokeColor),
        width = lineWidth,
        cap = const(LineCap.Round),
    )

    CircleLayer(
        id = "path-dot-layer",
        source = pathSource,
        color = const(recordingLocationColor),
        strokeColor = const(recordingStrokeColor),
        radius = circleRadius,
        strokeWidth = circleStrokeWidth,
        pitchAlignment = const(CirclePitchAlignment.Map),
        minZoom = 10f
    )
}


@Composable
fun SessionLayer(
    sessions: List<Session>,
    onSessionClick: (String) -> Unit
) {
    val pathSource = rememberGeoJsonSource(
        id = "sessions",
        data = FeatureCollection(
            sessions.map { session ->
                Feature(LineString(session.path.map { it.latLng.toPosition() })).also {
                    it.setStringProperty("sessionId", session.id)
                }
            }
        )
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
        width = lineWidth,
        cap = const(LineCap.Round),
        minZoom = MIN_ZOOM,
        onClick = featureClickHandler
    )

    CircleLayer(
        id = "session-dot-layer",
        source = pathSource,
        color = const(sessionPointColor),
        strokeColor = const(sessionStrokeColor),
        radius = circleRadius,
        strokeWidth = circleStrokeWidth,
        pitchAlignment = const(CirclePitchAlignment.Map),
        minZoom = MIN_ZOOM,
        onClick = featureClickHandler
    )
}


@Composable
fun SelectedSessionLayer(session: Session) {
    val pathSource = rememberGeoJsonSource(
        id = "selected-session",
        data = FeatureCollection(
            Feature(LineString(session.path.map { it.latLng.toPosition() })).also {
                it.setStringProperty("sessionId", session.id)
            }
        )
    )

    LineLayer(
        id = "selected-session-line-layer",
        source = pathSource,
        color = const(selectedSessionStrokeColor),
        width = lineWidth,
        minZoom = MIN_ZOOM,
        cap = const(LineCap.Round),
    )

    CircleLayer(
        id = "selected-session-dot-layer",
        source = pathSource,
        color = const(selectedSessionPointColor),
        radius = circleRadius,
        strokeColor = const(selectedSessionStrokeColor),
        strokeWidth = circleStrokeWidth,
        pitchAlignment = const(CirclePitchAlignment.Map),
        minZoom = MIN_ZOOM,
    )
}