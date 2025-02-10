package pro.schacher.gpsrekorder.shared.location

import co.touchlab.kermit.Logger
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
import kotlinx.coroutines.runBlocking
import platform.CoreLocation.kCLLocationAccuracyBest
import pro.schacher.gpsrekorder.shared.AppLogger
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.meters
import pro.schacher.gpsrekorder.shared.model.ms
import kotlin.random.Random

class IOSLocationDataSource : LocationDataSource() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var updateJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private val locationTracker = LocationTracker(PermissionsController(), kCLLocationAccuracyBest)

    private val permissionsController = this.locationTracker.permissionsController

    override val active: Boolean
        get() = this.updateJob?.isActive ?: false

    override fun hasLocationPermission(): Boolean = runBlocking {
        try {
            permissionsController.isPermissionGranted(Permission.LOCATION)
        } catch (e: Exception) {
            Logger.e(e) { "Failed to check location permission" }
            false
        }
    }

    override suspend fun requestLocationPermission(): Boolean {
        if (!this.permissionsController.isPermissionGranted(Permission.LOCATION)) {
            this.permissionsController.providePermission(Permission.LOCATION)
        }

        return this.hasLocationPermission()
    }

    override fun startLocationUpdates() {
        AppLogger.d { "Starting location updates" }
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
        AppLogger.d { "Stopping location updates" }
        this.updateJob?.cancel()
        this.locationTracker.stopTracking()
    }
}

private fun ExtendedLocation.toLocation(): Location = Location(
    latLng = LatLng(this.location.coordinates.latitude, this.location.coordinates.longitude),
    accuracy = this.location.coordinatesAccuracyMeters.takeIf { it >= 0.0 }?.meters,
    altitude = this.altitude.altitudeMeters.takeIf { it >= 0.0 }?.meters,
    speed = this.speed.speedMps.takeIf { it >= 0.0 }?.ms,
    heading = this.azimuth.azimuthDegrees.takeIf { it >= 0.0 },
)