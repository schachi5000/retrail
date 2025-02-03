package pro.schacher.gpsrekorder.shared.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pro.schacher.gpsrekorder.shared.model.Location

abstract class LocationDataSource {

    protected val _state = MutableStateFlow<Location?>(null)

    val state = this._state as StateFlow<Location?>

    val location: Location?
        get() = this.state.value

    abstract val active: Boolean

    abstract fun startLocationUpdates()

    abstract fun stopLocationUpdates()
}

