package pro.schacher.gpsrekorder.shared.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider.AVAILABLE
import android.location.provider.ProviderProperties
import androidx.activity.ComponentActivity
import co.touchlab.kermit.Logger
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import pro.schacher.gpsrekorder.shared.AppLogger
import pro.schacher.gpsrekorder.shared.hasPermission
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Length
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session
import pro.schacher.gpsrekorder.shared.model.metersPerSecond

@SuppressLint("NewApi")
class AndroidLocationDataSource(private val componentActivity: ComponentActivity) :
    LocationDataSource(), LocationListener {

    private companion object {
        const val TEST_PROVIDER_NAME = LocationManager.GPS_PROVIDER
    }

    private val context: Context = componentActivity.applicationContext

    private val permissionsController = PermissionsController(this.context)

    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override var active: Boolean = false

    init {
        this.permissionsController.bind(this.componentActivity)

        this.locationManager.addTestProvider(
            TEST_PROVIDER_NAME,
            ProviderProperties.Builder()
                .setHasAltitudeSupport(true)
                .setHasSpeedSupport(true)
                .setHasBearingSupport(true)
                .setPowerUsage(ProviderProperties.POWER_USAGE_HIGH)
                .setAccuracy(ProviderProperties.ACCURACY_FINE)
                .build()
        )

        this.locationManager.setTestProviderEnabled(TEST_PROVIDER_NAME, true)
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
        AppLogger.d("AndroidLocationDataSource") { "Location changed: $location" }
        _state.update {
            location.toLocation()
        }
    }

    override fun updateTestLocation(location: Location) {
        AppLogger.d("AndroidLocationDataSource") { "Update test location: $location" }
        this.locationManager.setTestProviderEnabled(TEST_PROVIDER_NAME, true)
        this.locationManager.setTestProviderLocation(
            TEST_PROVIDER_NAME,
            location.toAndroidLocation(TEST_PROVIDER_NAME)
        )
    }
}

typealias AndroidLocation = android.location.Location

fun AndroidLocation.toLocation(): Location = Location(
    provider = this.provider ?: "unknown",
    latLng = LatLng(this.latitude, this.longitude),
    altitude = Length.fromMeters(this.altitude).takeIf { this.hasAltitude() },
    heading = this.bearing.takeIf { this.hasBearing() }?.toDouble(),
    speed = this.speed.toDouble().takeIf { this.hasSpeed() && it >= 0.0 }?.metersPerSecond,
    accuracy = Length.fromMeters(this.accuracy.toDouble()).takeIf { this.hasAccuracy() },
)

fun Location.toAndroidLocation(provider: String): AndroidLocation =
    AndroidLocation(provider)
        .also {
            it.longitude = this.latLng.longitude
            it.latitude = this.latLng.latitude
            it.time = Clock.System.now().toEpochMilliseconds()
            it.elapsedRealtimeNanos = Clock.System.now().nanosecondsOfSecond.toLong()
            it.accuracy = this.accuracy?.inMeters?.toFloat() ?: 0f
            it.bearing = this.heading?.toFloat() ?: 0f
            it.speed = this.speed?.metersPerSecond?.toFloat() ?: 0f
        }