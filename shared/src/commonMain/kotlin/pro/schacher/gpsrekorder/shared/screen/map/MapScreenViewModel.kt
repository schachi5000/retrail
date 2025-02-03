package pro.schacher.gpsrekorder.shared.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.model.LatLng

class MapScreenViewModel(private val locationDataSource: LocationDataSource) : ViewModel() {

    private val _state = MutableStateFlow(State())

    val state = _state.asStateFlow()

    init {
        this.viewModelScope.launch {
            locationDataSource.state.collect { location ->
                _state.update {
                    it.copy(
                        location = location?.latLng,
                        path = if (_state.value.active) {
                            it.path + listOfNotNull(location?.latLng)
                        } else {
                            it.path
                        }
                    )
                }
            }
        }

        this.locationDataSource.startLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        this.locationDataSource.stopLocationUpdates()
    }

    fun onRecordClick() {
        this._state.update {
            if (it.active) {
                it.copy(
                    active = false,
                    path = emptyList()
                )
            } else {
                it.copy(
                    active = true,
                    path = listOfNotNull(locationDataSource.location?.latLng)
                )
            }
        }
    }

    data class State(
        val location: LatLng? = null,
        val path: List<LatLng> = emptyList(),
        val active: Boolean = false
    )
}