package pro.schacher.gpsrekorder.shared.model

data class Length private constructor(private val meters: Double) : Comparable<Length> {
    companion object {
        private const val METERS_TO_KM = 0.001

        private const val METERS_TO_MILE = 0.00062137

        private const val METERS_TO_FEET = 3.28084

        const val SECONDS_TO_MILLISECONDS = 1000.0

        fun fromMeters(meters: Double): Length = Length(meters)

        fun fromKilometers(kilometers: Double) = Length(kilometers / METERS_TO_KM)

        val ZERO: Length = Length(0.0)

        fun add(first: Length, second: Length) = first + second

        fun subtract(first: Length, second: Length) = first - second
    }

    val inMeters: Double = meters

    val inKilometers: Double = meters * METERS_TO_KM

    val inFeet: Double = meters * METERS_TO_FEET

    val inMiles: Double = meters * METERS_TO_MILE

    operator fun plus(other: Length): Length =
        Length(this.meters + other.meters)

    operator fun minus(other: Length): Length =
        Length(this.meters - other.meters)

    override operator fun compareTo(other: Length): Int = this.meters.compareTo(other.meters)
}

/**
 * Convenience function to create a [Length] in meters from a [Double].
 */
inline val Double.meters: Length get() = Length.fromMeters(this)

/**
 * Convenience function to create a [Length] in meters from a [Int].
 */
inline val Int.meters: Length get() = Length.fromMeters(this.toDouble())

/**
 * Convenience function to create a [Length] in kilometers from a [Double].
 */
inline val Double.kilometers: Length get() = Length.fromKilometers(this)

/**
 * Convenience function to create a [Length] in kilometers from a [Double].
 */
inline val Int.kilometers: Length get() = Length.fromKilometers(this.toDouble())