package pro.schacher.gpsrekorder.shared.location

import dev.icerock.moko.geo.ExtendedLocation
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.ios.PermissionsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import platform.CoreLocation.kCLLocationAccuracyBest
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.meters
import pro.schacher.gpsrekorder.shared.model.ms

class IOSLocationDataSource : LocationDataSource() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var updateJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private val locationTracker = LocationTracker(PermissionsController(), kCLLocationAccuracyBest)

    override val active: Boolean
        get() = this.updateJob?.isActive ?: false

    init {
        this.scope.launch {
            requestLocationPermission()
        }
    }

    private suspend fun requestLocationPermission() {
        if (!this.locationTracker.permissionsController.isPermissionGranted(Permission.LOCATION)) {
            this.locationTracker.permissionsController.providePermission(Permission.LOCATION)
        }
    }

    override fun startLocationUpdates() {
        this.updateJob = scope.launch {
            locationTracker.startTracking()
            locationTracker.getExtendedLocationsFlow().collect { extendedLocation ->
                _state.update {
                    extendedLocation.toLocation()
                }
            }
        }
    }

    override fun stopLocationUpdates() {
        this.updateJob?.cancel()
        this.locationTracker.stopTracking()
    }
}

private fun ExtendedLocation.toLocation(): Location = Location(
    latLng = LatLng(this.location.coordinates.latitude, this.location.coordinates.longitude),
    accuracy = this.location.coordinatesAccuracyMeters.meters,
    altitude = this.altitude.altitudeMeters.meters,
    speed = this.speed.speedMps.ms,
    heading = this.azimuth.azimuthDegrees,
)