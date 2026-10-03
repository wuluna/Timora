package cs.uwaterloo.domain.model

import cs.uwaterloo.data.mock.MockDb
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.domain.entity.MutableCategory
import cs.uwaterloo.domain.entity.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EventModelTest {
    private lateinit var mockDb: MockDb
    private lateinit var eventModel: EventModel

    @BeforeEach
    fun setup() {
        mockDb = MockDb()
        mockDb.clear()
        eventModel = EventModel(
            eventDao = mockDb.eventDao,
            attendeeDao = mockDb.attendeeDao,
            announcementDao = mockDb.announcementDao,
            mutableCategoryDao = mockDb.mutableCategoryDao,
            immutableCategoryDao = mockDb.immutableCategoryDao,
            userDao = mockDb.userDao
        )
    }

    @Test
    fun `createEvent successful`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)
            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            val created = eventModel.createEvent(event)
            assertNotNull(created)
            assertEquals("event1", created.id)
            assertEquals("Test Event", created.title)
            val attendees = mockDb.attendeeDao.getByEventId("event1")
            assertEquals(1, attendees.size)
            assertEquals("host1", attendees[0].userId)
        }
    }

    @Test
    fun `getEventById return event if exists`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)
            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventModel.createEvent(event)
            val retrieved = eventModel.getEventById("event1")
            assertNotNull(retrieved)
            assertEquals("event1", retrieved.id)
            assertEquals("Test Event", retrieved.title)
        }
    }

    @Test
    fun `addAnnouncementToEvent successful`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)
            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventModel.createEvent(event)
            eventModel.addAnnouncementToEvent("event1", "host1", "Important announcement")

            val announcements = mockDb.announcementDao.getByEventId("event1")
            assertEquals(1, announcements.size)
            assertEquals("Important announcement", announcements[0].text)
        }
    }

    @Test
    fun `addMutableCategoryToEvent successful`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventModel.createEvent(event)

            val category = MutableCategory(name = "Bring Items")
            eventModel.addMutableCategoryToEvent("event1", category)

            val categories = mockDb.mutableCategoryDao.getByEventId("event1")
            assertEquals(1, categories.size)
            assertEquals("Bring Items", categories[0].name)
        }
    }
}
