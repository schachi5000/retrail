package pro.schacher.gpsrekorder.shared.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session
import pro.schacher.gpsrekorder.shared.repository.RecordingRepository
import pro.schacher.gpsrekorder.shared.repository.SessionRepository

class MapScreenViewModel(
    private val sessionRepository: SessionRepository,
    private val recordingRepository: RecordingRepository,
    private val locationDataSource: LocationDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(State())

    val state = _state.asStateFlow()

    init {
        this.viewModelScope.launch {
            locationDataSource.state.collect {
                onLocationUpdated(it)
            }
        }

        this.viewModelScope.launch {
            recordingRepository.activeSession.collect { session ->
                _state.update {
                    it.copy(
                        activeSession = session,
                        selectedSession = null
                    )
                }
            }
        }

        this.viewModelScope.launch {
            sessionRepository.allSessions.collect { sessions ->
                _state.update { state ->
                    state.copy(
                        allSessions = sessions,
                        selectedSession = sessions.find { it.id == state.selectedSession?.id }
                    )
                }
            }
        }

        this.viewModelScope.launch {
            if (locationDataSource.hasLocationPermission()) {
                locationDataSource.startLocationUpdates()
            } else {
                val granted = locationDataSource.requestLocationPermission()
                if (granted) {
                    locationDataSource.startLocationUpdates()
                }
            }
        }
    }

    private fun onLocationUpdated(location: Location?) {
        _state.update {
            it.copy(location = location)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!this.recordingRepository.recording) {
            this.locationDataSource.stopLocationUpdates()
        }
    }

    fun onRecordClick() {
        if (this.recordingRepository.recording) {
            this.recordingRepository.stopRecording()
        } else {
            this.recordingRepository.startRecording()
            this._state.update {
                it.copy(cameraTrackingActive = true)
            }
        }
    }

    fun onLocationButtonClicked() {
        _state.update {
            it.copy(cameraTrackingActive = true)
        }
    }

    fun onMapMoved() {
        _state.update {
            it.copy(cameraTrackingActive = false)
        }
    }

    fun onSessionClicked(sessionId: String) {
        if (_state.value.recording) {
            return
        }

        val session = this.sessionRepository.allSessions.value.find { it.id == sessionId }
        if (session != null) {
            _state.update {
                it.copy(
                    activeSession = null,
                    selectedSession = session,
                    cameraTrackingActive = false
                )
            }
        }
    }

    fun onCloseSessionClicked() {
        _state.update {
            it.copy(selectedSession = null)
        }
    }

    fun onDeleteSessionClicked(sessionId: String) {
        this.sessionRepository.deleteSession(sessionId)
    }

    data class State(
        val location: Location? = null,
        val activeSession: Session? = null,
        val selectedSession: Session? = null,
        val allSessions: List<Session> = emptyList(),
        val initialCameraPosition: LatLng = LatLng(52.372661, 9.739450),
        val cameraTrackingActive: Boolean = true
    ) {

        val recording: Boolean
            get() = this.activeSession != null

        val path: List<Location>
            get() = this.activeSession?.path ?: emptyList()
    }
}