package pro.schacher.gpsrekorder.shared.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import pro.schacher.gpsrekorder.shared.AppLogger
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location

class SessionRepository(private val locationDataSource: LocationDataSource) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _allSessions = MutableStateFlow<List<Session>>(emptyList())

    val allSessions = this._allSessions.asStateFlow()

    private val _activeSession = MutableStateFlow<Session?>(null)

    val activeSession = this._activeSession.asStateFlow()

    val recording: Boolean
        get() = this.activeSession.value != null

    init {
        this.scope.launch {
            locationDataSource.state.collect {
                if (it != null) {
                    onLocationUpdated(it)
                }
            }
        }
    }

    private fun onLocationUpdated(location: Location) {
        this._activeSession.update {
            it?.copy(
                path = it.path + listOf(location.latLng)
            )
        }
    }

    fun startRecording() {
        if (this.recording) {
            AppLogger.d { "Already recording session" }
            return
        }

        this._activeSession.update {
            val path = locationDataSource.location?.let {
                listOf(it.latLng)
            } ?: emptyList()

            Session(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                path = path
            )
        }
    }

    fun saveRecording() {
        val activeSession = this.activeSession.value ?: return
        this._allSessions.update {
            it + listOf(activeSession)
        }

        this.cancelRecording()
    }

    fun cancelRecording() {
        this._activeSession.update {
            null
        }
    }
}

data class Session(val id: String, val path: List<LatLng> = emptyList())