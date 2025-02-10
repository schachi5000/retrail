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
import pro.schacher.gpsrekorder.shared.database.DatabaseDoa
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session

class SessionRepository(
    private val databaseDoa: DatabaseDoa,
    private val locationDataSource: LocationDataSource
) {

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

        this.scope.launch {
            _allSessions.update {
                databaseDoa.getSessions()
            }
        }
    }

    private fun onLocationUpdated(location: Location) {
        this._activeSession.update {
            it?.copy(
                path = it.path + listOf(location)
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
                listOf(it)
            } ?: emptyList()

            Session(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                path = path
            )
        }
    }

    fun saveRecording() {
        val activeSession = this.activeSession.value ?: return

        scope.launch {
            databaseDoa.createSessions(activeSession)

            _allSessions.update {
                databaseDoa.getSessions()
            }

            cancelRecording()
        }

    }

    fun cancelRecording() {
        this._activeSession.update {
            null
        }
    }

    fun deleteSession(sessionId: String) {
        this.scope.launch {
            databaseDoa.deleteSession(sessionId)

            _allSessions.update {
                databaseDoa.getSessions()
            }
        }
    }
}

