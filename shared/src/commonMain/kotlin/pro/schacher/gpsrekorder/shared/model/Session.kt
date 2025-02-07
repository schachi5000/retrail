package pro.schacher.gpsrekorder.shared.model

import kotlinx.datetime.Clock

data class Session(
    val id: String,
    val path: List<Location> = emptyList(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)