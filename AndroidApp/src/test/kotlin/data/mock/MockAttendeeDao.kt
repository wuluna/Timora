package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Role

/**
 * Mock implementation of IAttendeeDao for testing.
 */
class MockAttendeeDao : IAttendeeDao {
    private val attendees =
        mutableMapOf<String, MutableMap<String, Attendee>>() // eventId -> (userId -> Attendee)

    fun clear() {
        attendees.clear()
    }

    override suspend fun add(attendee: Attendee, eventId: String) {
        val eventAttendees = attendees.getOrPut(eventId) { mutableMapOf() }
        if (eventAttendees.containsKey(attendee.userId)) {
            throw IllegalArgumentException("User ${attendee.userId} is already an attendee of event $eventId")
        }
        eventAttendees[attendee.userId] = attendee
    }

    override suspend fun remove(userId: String, eventId: String) {
        attendees[eventId]?.remove(userId)
    }

    override suspend fun getByEventId(eventId: String): List<Attendee> {
        return attendees[eventId]?.values?.toList() ?: emptyList()
    }

    override suspend fun updateRole(userId: String, eventId: String, role: Role) {
        val eventAttendees = attendees[eventId]
            ?: throw IllegalArgumentException("Event with ID $eventId does not exist")
        val attendee = eventAttendees[userId]
            ?: throw IllegalArgumentException("User $userId is not an attendee of event $eventId")
        attendee.updateRole(role)
    }

    /**
     * Helper method to get event IDs that a user is attending.
     */
    fun getEventIdsByUserId(userId: String): List<String> {
        return attendees.filter { (_, userMap) ->
            userMap.containsKey(userId)
        }.keys.toList()
    }
}
