package cs.uwaterloo.domain.model

import cs.uwaterloo.data.dao.IAnnouncementDao
import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IImmutableCategoryDao
import cs.uwaterloo.data.dao.IMutableCategoryDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.domain.entity.Announcement
import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.domain.entity.ImmutableCategory
import cs.uwaterloo.domain.entity.MutableCategory
import cs.uwaterloo.domain.entity.Role
import java.time.Instant

/**
 * Model class that coordinates event-related operations.
 * Orchestrates between domain entities (business logic) and DAOs (data persistence).
 */
class EventModel(
    private val eventDao: IEventDao,
    private val attendeeDao: IAttendeeDao,
    private val announcementDao: IAnnouncementDao,
    private val mutableCategoryDao: IMutableCategoryDao,
    private val immutableCategoryDao: IImmutableCategoryDao,
    private val userDao: IUserDao
) {

    suspend fun getEventById(id: String): Event? {
        return eventDao.getById(id)
    }

    suspend fun createEvent(event: Event): Event {
        event.addAttendee(event.host, Role.HOST)

        val created = eventDao.create(event)

        attendeeDao.add(
            Attendee(event.host.id, event.host.name, Role.HOST),
            event.id
        )

        return created
    }

    suspend fun updateEvent(event: Event): Event {
        return eventDao.update(event)
    }

    suspend fun getEventsByAttendee(userId: String): List<Event> {
        return eventDao.getByAttendeeId(userId)
    }

    suspend fun getAllUserEvents(userId: String): List<Event> {
        return getEventsByAttendee(userId)
    }

    suspend fun addAnnouncementToEvent(eventId: String, userId: String, text: String) {
        val event = eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event with ID $eventId not found")
        val user = userDao.getById(userId)
            ?: throw IllegalArgumentException("User with ID $userId not found")

        event.addAnnouncement(user, text)

        announcementDao.create(
            Announcement(
                text = text,
                userId = userId,
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            ),
            eventId
        )
    }

    suspend fun addMutableCategoryToEvent(eventId: String, category: MutableCategory) {
        val event = eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event with ID $eventId not found")

        event.addMutableCategory(category)

        mutableCategoryDao.create(category, eventId)
    }

    suspend fun addImmutableCategoryToEvent(eventId: String, category: ImmutableCategory) {
        val event = eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event with ID $eventId not found")

        event.addImmutableCategory(category)

        immutableCategoryDao.create(category, eventId)
    }

    suspend fun updateMutableCategoryItems(
        eventId: String,
        categoryName: String,
        category: MutableCategory
    ) {
        val event = eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event with ID $eventId not found")

        val existingCategory = event.mutableCategoryList.find { it.name == categoryName }
        if (existingCategory != null) {
            event.mutableCategoryList.remove(existingCategory)
            event.mutableCategoryList.add(category)
        }

        mutableCategoryDao.update(eventId, categoryName, category)
    }

    suspend fun updateImmutableCategoryItems(
        eventId: String,
        categoryName: String,
        category: ImmutableCategory
    ) {
        val event = eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event with ID $eventId not found")

        val existingCategory = event.immutableCategoryList.find { it.name == categoryName }
        if (existingCategory != null) {
            event.immutableCategoryList.remove(existingCategory)
            event.immutableCategoryList.add(category)
        }

        immutableCategoryDao.update(eventId, categoryName, category)
    }
}
