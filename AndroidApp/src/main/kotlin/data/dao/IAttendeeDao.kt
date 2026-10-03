package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Role

/**
 * Data Access Object interface for Attendee operations.
 */
interface IAttendeeDao {
    suspend fun add(attendee: Attendee, eventId: String)

    suspend fun remove(userId: String, eventId: String)

    suspend fun getByEventId(eventId: String): List<Attendee>

    suspend fun updateRole(userId: String, eventId: String, role: Role)
}
