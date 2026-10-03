package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IAnnouncementDao
import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IImmutableCategoryDao
import cs.uwaterloo.data.dao.IMutableCategoryDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.domain.entity.Event

/**
 * Mock implementation of IEventDao for testing.
 */
class MockEventDao(
    private val userDao: IUserDao,
    private val attendeeDao: IAttendeeDao,
    private val announcementDao: IAnnouncementDao,
    private val mutableCategoryDao: IMutableCategoryDao,
    private val immutableCategoryDao: IImmutableCategoryDao
) : IEventDao {
    private val events = mutableMapOf<String, Event>()

    fun clear() {
        events.clear()
    }

    override suspend fun create(event: Event): Event {
        if (events.containsKey(event.id)) {
            throw IllegalArgumentException("Event with ID ${event.id} already exists")
        }
        events[event.id] = event
        return event
    }

    override suspend fun getById(id: String): Event? {
        return events[id]
    }

    override suspend fun update(event: Event): Event {
        if (!events.containsKey(event.id)) {
            throw IllegalArgumentException("Event with ID ${event.id} does not exist")
        }
        events[event.id] = event
        return event
    }

    override suspend fun delete(id: String) {
        events.remove(id)
    }

    override suspend fun getByHostId(hostId: String): List<Event> {
        return events.values.filter { it.host.id == hostId }
    }

    override suspend fun getByAttendeeId(userId: String): List<Event> {
        // Get event IDs that the user is attending
        val eventIds = if (attendeeDao is MockAttendeeDao) {
            attendeeDao.getEventIdsByUserId(userId)
        } else {
            emptyList()
        }

        return eventIds.mapNotNull { events[it] }
    }
}
