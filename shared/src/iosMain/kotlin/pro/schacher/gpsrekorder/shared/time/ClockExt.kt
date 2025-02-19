package pro.schacher.gpsrekorder.shared.time

import kotlinx.datetime.Clock
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSTimeIntervalSince1970
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

actual val Clock.System.elapsedTimeSinceStart: Duration
    get() = (NSProcessInfo.processInfo.systemUptime).seconds



