package pro.schacher.gpsrekorder.shared.location

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class SimulatingLocationDataSource : LocationDataSource() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var updateJob: Job? = null

    override val active: Boolean
        get() = this.updateJob?.isActive == true

    private val rng = Random.Default

    private val modifier: Double
        get() = if (rng.nextBoolean()) {
            1
        } else {
            -1
        }.let {
            (rng.nextDouble(5.0) / 10000.0) * it
        }

    override fun startLocationUpdates() {
        if (this.updateJob?.isActive == true) {
            return
        }

        Logger.d { "Starting location updates" }
        this.updateJob = this.scope.launch {
            while (isActive) {
                val lastLocation =
                    _state.value ?: Location(LatLng(52.372661437130255, 9.739450741409224))

                val updateLocation = lastLocation.copy(
                    latLng = LatLng(
                        lastLocation.latLng.latitude + modifier,
                        lastLocation.latLng.longitude + modifier
                    )
                )
                _state.value = updateLocation

                delay(1.seconds)
            }
        }
    }

    override fun stopLocationUpdates() {
        Logger.d { "Stopping location updates" }
        this.updateJob?.cancel()
    }

    override fun hasLocationPermission(): Boolean = true

    override suspend fun requestLocationPermission(): Boolean = true
}