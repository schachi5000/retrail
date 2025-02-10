package pro.schacher.gpsrekorder.shared.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.model.Session

class RecordingRepository(
    private val sessionRepository: SessionRepository,
    private val locationDataSource: LocationDataSource
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _activeSession = MutableStateFlow<Session?>(null)

    val activeSession = this._activeSession.asStateFlow()

    val recording: Boolean
        get() = this.activeSession.value != null

    init {
        this.scope.launch {
            locationDataSource.state.collect { location ->
                val recordingSession = activeSession.value
                if (recordingSession != null && location != null) {
                    sessionRepository.addLocationToSession(recordingSession.id, location)

                    _activeSession.update {
                        recordingSession.copy(path = recordingSession.path + location)
                    }
                }
            }
        }
    }

    fun startRecording() {
        this.scope.launch {
            _activeSession.update { sessionRepository.createSession(locationDataSource.location) }
        }
    }

    fun stopRecording() {
        this._activeSession.update { null }
    }
}