package pro.schacher.gpsrekorder.shared.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.ComponentActivity
import co.touchlab.kermit.Logger
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import pro.schacher.gpsrekorder.shared.hasPermission
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Length
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.ms

class AndroidLocationDataSource(private val componentActivity: ComponentActivity) :
    LocationDataSource(),
    LocationListener {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val context: Context = componentActivity.applicationContext

    private val permissionsController = PermissionsController(this.context)

    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override var active: Boolean = false

    init {
        this.permissionsController.bind(this.componentActivity)
    }

    override fun hasLocationPermission(): Boolean = runBlocking {
        try {
            permissionsController.isPermissionGranted(Permission.LOCATION)
        } catch (e: Exception) {
            Logger.e(e) { "Failed to check location permission" }
            false
        }
    }

    override suspend fun requestLocationPermission(): Boolean {
        return try {
            this.permissionsController.providePermission(Permission.LOCATION)
            this.hasLocationPermission()

        } catch (e: Exception) {
            Logger.e(e) { "Failed to request location permission" }
            false
        }
    }


    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        if (!this.context.hasPermission(ACCESS_FINE_LOCATION)) {
            return
        }

        if (!this.active) {
            try {
                this.locationManager.getProvider(LocationManager.GPS_PROVIDER)?.let {
                    this.locationManager.requestLocationUpdates(it.name, 0, 0f, this)
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