package pro.schacher.gpsrekorder.shared.utils

import dev.sargunv.maplibrecompose.compose.CameraState

val CameraState.dpPerMeterAtTarget: Double
    get() = 1 / this.metersPerDpAtTarget
