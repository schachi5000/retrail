package pro.schacher.gpsrekorder.shared.location

import kotlinx.coroutines.flow.StateFlow
import pro.schacher.gpsrekorder.shared.model.Location

interface LocationDataSource {

    val state : StateFlow<Location?>

    val location : Location?

    val active : Boolean

    fun startLocationUpdates()

    fun stopLocationUpdates()
}

