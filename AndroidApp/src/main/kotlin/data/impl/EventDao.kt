@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IAnnouncementDao
import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IImmutableCategoryDao
import cs.uwaterloo.data.dao.IMutableCategoryDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.Event
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IEventDao.
 */
class EventDao(
    private val db: IDb,
    private val userDao: IUserDao,
    private val attendeeDao: IAttendeeDao,
    private val announcementDao: IAnnouncementDao,
    private val mutableCategoryDao: IMutableCategoryDao,
    private val immutableCategoryDao: IImmutableCategoryDao
) : IEventDao {

    @Serializable
    private data class EventDTO(
        val id: String,
        val title: String,
        val host_id: String,
        val date_time: String,
        val location: String,
        val description: String = "",
        val created_at: String? = null,
        val updated_at: String? = null
    )

    override suspend fun create(event: Event): Event {
        val dto = EventDTO(
            id = event.id,
            title = event.title,
            host_id = event.host.id,
            date_time = event.dateTime,
            location = event.location,
            description = event.description
        )
        db.client.from("events").insert(dto)
        return event
    }

    override suspend fun getById(id: String): Event? {
        val eventDto = db.client.from("events")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<EventDTO>() ?: return null

        val host = userDao.getById(eventDto.host_id) ?: return null

        val attendees = attendeeDao.getByEventId(id).toMutableSet()
        val announcements = announcementDao.getByEventId(id).toMutableList()
        val mutableCategories = mutableCategoryDao.getByEventId(id).toMutableSet()
        val immutableCategories = immutableCategoryDao.getByEventId(id).toMutableSet()

        return Event(
            id = eventDto.id,
            title = eventDto.title,
            host = host,
            dateTime = eventDto.date_time,
            location = eventDto.location,
            description = eventDto.description,
            attendeeList = attendees,
            announcementList = announcements,
            mutableCategoryList = mutableCategories,
            immutableCategoryList = immutableCategories
        )
    }

    override suspend fun update(event: Event): Event {
        val dto = EventDTO(
            id = event.id,
            title = event.title,
            host_id = event.host.id,
            date_time = event.dateTime,
            location = event.location,
            description = event.description
        )
        db.client.from("events").update(dto) {
            filter { eq("id", event.id) }
        }
        return event
    }

    override suspend fun delete(id: String) {
        db.client.from("events").delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun getByHostId(hostId: String): List<Event> {
        val eventDtos = db.client.from("events")
            .select {
                filter { eq("host_id", hostId) }
            }
            .decodeList<EventDTO>()

        return eventDtos.mapNotNull { getById(it.id) }
    }

    override suspend fun getByAttendeeId(userId: String): List<Event> {
        // Get event IDs that the user is attending
        val eventIds = (attendeeDao as AttendeeDao).getEventIdsByUserId(userId)

        // Fetch each event
        return eventIds.mapNotNull { eventId -> getById(eventId) }
    }
}
