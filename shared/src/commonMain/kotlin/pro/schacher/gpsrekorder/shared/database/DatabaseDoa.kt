package pro.schacher.gpsrekorder.shared.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import database.AppDatabase
import database.LocationEntry
import database.SessionEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import pro.schacher.gpsrekorder.shared.model.LatLng
import pro.schacher.gpsrekorder.shared.model.Location
import pro.schacher.gpsrekorder.shared.model.Session
import pro.schacher.gpsrekorder.shared.model.meters
import pro.schacher.gpsrekorder.shared.model.ms

class DatabaseDoa(private val appDatabase: AppDatabase) {

    private val dbQuery = this.appDatabase.appDatabaseQueries

    fun getAllSessions(): Flow<List<Session>> {
        val locations = this.dbQuery.getAllLocations()
            .asFlow()
            .mapToList(Dispatchers.IO)

        val sessions = this.dbQuery.getAllSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)

        return combine(sessions, locations) { sessionEntries, locationEntries ->
            val groupedLocations =
                locationEntries.map { location -> location.sessionId to location.toLocation() }
                    .groupBy({ it.first }, { it.second })

            sessionEntries.map { session ->
                session.toSession(groupedLocations[session.id] ?: emptyList())
            }
        }
    }

    suspend fun createSessions(session: Session) {
        withContext(Dispatchers.IO) {
            val storedSession = dbQuery.getSessionById(session.id).executeAsOneOrNull()
            if (storedSession == null) {
                dbQuery.createSession(
                    session.id,
                    session.id,
                    Clock.System.now().toEpochMilliseconds()
                )
            }

            session.path.forEach {
                addLocationToSession(session.id, it)
            }
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

private fun SessionEntry.toSession(path: List<Location>): Session = Session(
    id = this.id,
    createdAt = this.createdAt,
    path = path
)

private fun LocationEntry.toLocation(): Location = Location(
    latLng = LatLng(this.latitude, this.longitude),
    speed = this.speedMetersPerSecond?.ms,
    accuracy = this.accuracyMeters?.meters,
    heading = this.bearingDegrees,
    timestamp = this.timestamp,
    provider = this.provider
)