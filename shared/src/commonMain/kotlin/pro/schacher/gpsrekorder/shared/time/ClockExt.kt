package pro.schacher.gpsrekorder.shared.time

import kotlinx.datetime.Clock
import kotlin.time.Duration


expect val Clock.System.elapsedTimeSinceStart: Duration



