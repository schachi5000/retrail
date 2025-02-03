package pro.schacher.gpsrekorder.shared.model

import kotlinx.datetime.Clock

data class Location(
    val latLng: LatLng,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val provider: String = "unknown",
    val altitude: Length? = null,
    val heading: Double? = null,
    val speed: Speed? = null,
    val accuracy: Length? = null,
    val level: Int? = null
) 