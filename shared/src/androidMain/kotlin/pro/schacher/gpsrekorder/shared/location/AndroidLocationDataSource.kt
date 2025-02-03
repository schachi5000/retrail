package pro.schacher.gpsrekorder.shared.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.update
import pro.schacher.gpsrekorder.shared.hasPermission
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Length
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.ms

 class AndroidLocationDataSource(private val context: Context) : LocationDataSource(),
    LocationListener {

    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override var active: Boolean = false

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        if (!this.context.hasPermission(ACCESS_FINE_LOCATION)) {
            return
        }

        if (!this.active) {
            try {
                this.locationManager.getProvider(LocationManager.GPS_PROVIDER)?.let {
                    locationManager.requestLocationUpdates(it.name, 0, 1f, this)
                }
            } catch (e: Exception) {
                this.active = false
                throw e
            }

            this.active = true
        }
    }

    override fun stopLocationUpdates() {
        this.locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: AndroidLocation) {
        _state.update {
            location.toLocation()
        }
    }
}

typealias AndroidLocation = android.location.Location

fun AndroidLocation.toLocation(): Location = Location(
    provider = this.provider ?: "unknown",
    latLng = LatLng(this.latitude, this.longitude),
    altitude = Length.fromMeters(this.altitude).takeIf { this.hasAltitude() },
    heading = this.bearing.takeIf { this.hasBearing() }?.toDouble(),
    speed = this.speed.toDouble().takeIf { this.hasSpeed() && it >= 0.0 }?.ms,
    accuracy = Length.fromMeters(this.accuracy.toDouble()).takeIf { this.hasAccuracy() },
)