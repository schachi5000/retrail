package pro.schacher.gpsrekorder.shared.location

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLLocationAccuracyBest
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location

class IOSLocationDataSource : LocationDataSource() {

    private val locationManager = CLLocationManager()

    override var active: Boolean = false

    private var locationPermissionStatusCancellableContinuation: CancellableContinuation<Any>? = null

    private var locationResultContinuation:(CancellableContinuation<Result<CLLocation>>)? = null

    init {
        this.locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }

    override fun startLocationUpdates() {
        val location = locationManager.location?.toLocation()

    }

    override fun stopLocationUpdates() {
        Logger.d { "Stopping location updates" }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toLocation(): Location {
    val latLng = this.coordinate().useContents {
        LatLng(this.latitude, this.longitude)
    }

    return Location(latLng)
}