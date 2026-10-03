package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.Event

/**
 * Data Access Object interface for Event operations.
 */
interface IEventDao {
    suspend fun create(event: Event): Event

    suspend fun getById(id: String): Event?

    suspend fun update(event: Event): Event

    suspend fun delete(id: String)

    suspend fun getByHostId(hostId: String): List<Event>

    suspend fun getByAttendeeId(userId: String): List<Event>
}
