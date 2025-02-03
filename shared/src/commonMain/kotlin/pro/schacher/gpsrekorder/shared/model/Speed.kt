package pro.schacher.gpsrekorder.shared.model

data class Speed private constructor(private val ms: Double) : Comparable<Speed> {

    init {
        check(ms >= 0) { "Speed is not allowed to be below zero" }
    }

    companion object {
        const val KMH_TO_MS = 1000.0 / 3600.0

        const val MS_TO_MPH = 2.237

        fun fromMs(ms: Double): Speed = Speed(ms)

        fun fromKmh(kmh: Double): Speed = Speed(kmh * KMH_TO_MS)

        val zero = 0.0.kmh

        const val SECONDS_TO_MILLISECONDS = 1000.0
    }

    val inMs: Double = this.ms

    val inKmh: Double = this.ms / KMH_TO_MS

    val inMph: Double = this.ms * MS_TO_MPH

    override operator fun compareTo(other: Speed): Int = this.ms.compareTo(other.ms)
}


/**
 * Convenience function to create a [Speed] in meters per second from a [Double].
 */
inline val Double.ms: Speed get() = Speed.fromMs(this)

/**
 * Convenience function to create a [Speed] in kilometers per hour from a [Double].
 */
inline val Double.kmh: Speed get() = Speed.fromKmh(this)

