package pro.schacher.gpsrekorder.shared.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import pro.schacher.gpsrekorder.shared.database.DatabaseDoa
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session

class SessionRepository(private val databaseDoa: DatabaseDoa) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _allSessions = databaseDoa.getAllSessions().stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val allSessions = this._allSessions

    fun deleteSession(sessionId: String) {
        this.scope.launch {
            databaseDoa.deleteSession(sessionId)
        }
    }

    suspend fun createSession(location: Location?): Session {
        val session = Session(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            path = location?.let { listOf(it) } ?: emptyList()
        )

        this.databaseDoa.createSessions(session)
        return session
    }

    fun addLocationToSession(sessionId: String, location: Location) {
        this.scope.launch {
            databaseDoa.addLocationToSession(sessionId, location)
        }
    }
}

