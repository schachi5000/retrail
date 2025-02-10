package pro.schacher.gpsrekorder.shared.database

import database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import pro.schacher.gpsrekorder.shared.AppLogger
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session
import pro.schacher.gpsrekorder.shared.model.meters
import pro.schacher.gpsrekorder.shared.model.ms

class DatabaseDoa(private val appDatabase: AppDatabase) {

    private val dbQuery = this.appDatabase.appDatabaseQueries

    suspend fun getSessions(): List<Session> {
        val sessions = withContext(Dispatchers.IO) {
            try {
                dbQuery.getAllSessions().executeAsList().map {
                    Session(
                        id = it.id,
                        path = emptyList()
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(e)
                emptyList()
            }
        }

        val finalSessions = sessions.map {
            val locations =
                dbQuery.getLocationsForSessionId(it.id).executeAsList().map { location ->
                    Location(
                        latLng = LatLng(location.latitude, location.longitude),
                        speed = location.speedMetersPerSecond?.ms,
                        accuracy = location.accuracyMeters?.meters,
                        heading = location.bearingDegrees,
                        timestamp = location.timestamp,
                        provider = location.provider
                    )
                }

            Session(it.id, locations)
        }

        return finalSessions
    }

    suspend fun createSessions(session: Session) {
        val storedSession = dbQuery.getSessionById(session.id).executeAsOneOrNull()
        if (storedSession == null) {
            this.dbQuery.createSession(
                session.id,
                session.id,
                Clock.System.now().toEpochMilliseconds()
            )
        }

        session.path.forEach {
            addLocationToSession(session.id, it)
        }
    }

    suspend fun addLocationToSession(sessionId: String, location: Location) {
        withContext(Dispatchers.IO) {
            val session =
                dbQuery.getSessionById(sessionId).executeAsOneOrNull() ?: return@withContext

            dbQuery.addLocation(
                sessionId = session.id,
                latitude = location.latLng.latitude,
                longitude = location.latLng.longitude,
                timestamp = location.timestamp,
                provider = location.provider,
                accuracyMeters = location.accuracy?.inMeters,
                bearingDegrees = location.heading,
                speedMetersPerSecond = location.speed?.inMs
            )
        }
    }

    suspend fun deleteSession(sessionId: String) {
        withContext(Dispatchers.IO) {
            dbQuery.deleteSessionById(sessionId)
        }
    }
}