package pro.schacher.gpsrekorder.shared.time

import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds


actual val Clock.System.elapsedTimeSinceStart: Duration
    get() = System.nanoTime().nanoseconds



