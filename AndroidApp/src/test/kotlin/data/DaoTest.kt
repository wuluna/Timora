package cs.uwaterloo.data

import cs.uwaterloo.data.dao.IAnnouncementDao
import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IImmutableCategoryDao
import cs.uwaterloo.data.dao.IInvitationDao
import cs.uwaterloo.data.dao.IMutableCategoryDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.data.mock.MockDb
import cs.uwaterloo.domain.entity.Announcement
import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.domain.entity.Invitation
import cs.uwaterloo.domain.entity.MutableCategory
import cs.uwaterloo.domain.entity.Role
import cs.uwaterloo.domain.entity.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for DAO pattern using Mock implementations.
 * These tests validate the DAO contracts without touching a real database.
 */
class DaoTest {

    private lateinit var mockDb: MockDb
    private lateinit var userDao: IUserDao
    private lateinit var eventDao: IEventDao
    private lateinit var attendeeDao: IAttendeeDao
    private lateinit var invitationDao: IInvitationDao
    private lateinit var announcementDao: IAnnouncementDao
    private lateinit var mutableCategoryDao: IMutableCategoryDao
    private lateinit var immutableCategoryDao: IImmutableCategoryDao

    @BeforeEach
    fun setup() {
        mockDb = MockDb()
        userDao = mockDb.userDao
        eventDao = mockDb.eventDao
        attendeeDao = mockDb.attendeeDao
        invitationDao = mockDb.invitationDao
        announcementDao = mockDb.announcementDao
        mutableCategoryDao = mockDb.mutableCategoryDao
        immutableCategoryDao = mockDb.immutableCategoryDao
        mockDb.clear()
    }

    // User Tests

    @Test
    fun `create should successfully create a new user`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )

            val created = userDao.create(user)

            assertEquals(user.id, created.id)
            assertEquals(user.email, created.email)
            assertEquals(user.name, created.name)
        }
    }

    @Test
    fun `create should throw exception when user already exists`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )

            userDao.create(user)

            assertThrows<IllegalArgumentException> {
                runBlocking { userDao.create(user) }
            }
        }
    }

    @Test
    fun `getById should return user when exists`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(user)

            val retrieved = userDao.getById("user1")

            assertNotNull(retrieved)
            assertEquals("user1", retrieved.id)
            assertEquals("test@example.com", retrieved.email)
        }
    }

    @Test
    fun `getById should return null when user does not exist`() {
        runBlocking {
            val retrieved = userDao.getById("nonexistent")

            assertNull(retrieved)
        }
    }

    @Test
    fun `getByEmail should return user when exists`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(user)

            val retrieved = userDao.getByEmail("test@example.com")

            assertNotNull(retrieved)
            assertEquals("user1", retrieved.id)
        }
    }

    @Test
    fun `update should update existing user`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(user)

            user.updateBio("New bio")
            val updated = userDao.update(user)

            assertEquals("New bio", updated.bio)
        }
    }

    @Test
    fun `update should throw exception when user does not exist`() {
        runBlocking {
            val user = User(
                id = "nonexistent",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )

            assertThrows<IllegalArgumentException> {
                runBlocking { userDao.update(user) }
            }
        }
    }

    // Event Tests

    @Test
    fun `create event should successfully create a new event`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )

            val created = eventDao.create(event)

            assertEquals(event.id, created.id)
            assertEquals(event.title, created.title)
            assertEquals(host.id, created.host.id)
        }
    }

    @Test
    fun `getById should return event when exists`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val retrieved = eventDao.getById("event1")

            assertNotNull(retrieved)
            assertEquals("event1", retrieved.id)
            assertEquals("Test Event", retrieved.title)
        }
    }

    // Attendee Tests

    @Test
    fun `add should successfully add attendee to event`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val attendee = Attendee(
                userId = "user1",
                name = "Attendee Name",
                role = Role.GUEST
            )

            attendeeDao.add(attendee, "event1")

            val attendees = attendeeDao.getByEventId("event1")
            assertEquals(1, attendees.size)
            assertEquals("user1", attendees[0].userId)
        }
    }

    @Test
    fun `add should throw exception when user already attending`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val attendee = Attendee(
                userId = "user1",
                name = "Attendee Name",
                role = Role.GUEST
            )
            attendeeDao.add(attendee, "event1")

            assertThrows<IllegalArgumentException> {
                runBlocking { attendeeDao.add(attendee, "event1") }
            }
        }
    }

    @Test
    fun `getByAttendeeId should return events user is attending`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event1 = Event(
                id = "event1",
                title = "Event 1",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Location 1"
            )
            val event2 = Event(
                id = "event2",
                title = "Event 2",
                host = host,
                dateTime = "2025-12-02T10:00:00Z",
                location = "Location 2"
            )
            eventDao.create(event1)
            eventDao.create(event2)

            val attendee1 = Attendee(userId = "user1", name = "User 1", role = Role.GUEST)
            val attendee2 = Attendee(userId = "user1", name = "User 1", role = Role.GUEST)
            attendeeDao.add(attendee1, "event1")
            attendeeDao.add(attendee2, "event2")

            val attendedEvents = eventDao.getByAttendeeId("user1")

            assertEquals(2, attendedEvents.size)
            assertTrue(attendedEvents.any { it.id == "event1" })
            assertTrue(attendedEvents.any { it.id == "event2" })
        }
    }

    // Invitation Tests

    @Test
    fun `create invitation should successfully create invitation`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val invitation = Invitation(
                eventId = "event1",
                inviterId = "host1",
                inviteeId = "user1"
            )

            val created = invitationDao.create(invitation)

            assertEquals("event1", created.eventId)
            assertEquals("host1", created.inviterId)
            assertEquals("user1", created.inviteeId)
        }
    }

    @Test
    fun `create invitation should throw exception when duplicate invitation`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val invitation = Invitation(
                eventId = "event1",
                inviterId = "host1",
                inviteeId = "user1"
            )
            invitationDao.create(invitation)

            assertThrows<IllegalArgumentException> {
                runBlocking { invitationDao.create(invitation) }
            }
        }
    }

    @Test
    fun `getByInviteeId should return all invitations for user`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event1 = Event(
                id = "event1",
                title = "Event 1",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Location 1"
            )
            val event2 = Event(
                id = "event2",
                title = "Event 2",
                host = host,
                dateTime = "2025-12-02T10:00:00Z",
                location = "Location 2"
            )
            eventDao.create(event1)
            eventDao.create(event2)

            invitationDao.create(Invitation("event1", "host1", "user1"))
            invitationDao.create(Invitation("event2", "host1", "user1"))

            val invitations = invitationDao.getByInviteeId("user1")

            assertEquals(2, invitations.size)
            assertTrue(invitations.any { it.eventId == "event1" })
            assertTrue(invitations.any { it.eventId == "event2" })
        }
    }

    @Test
    fun `delete invitation should remove invitation`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            invitationDao.create(Invitation("event1", "host1", "user1"))

            invitationDao.delete("event1", "user1")

            val invitations = invitationDao.getByEventId("event1")
            assertTrue(invitations.isEmpty())
        }
    }

    // Announcement Tests

    @Test
    fun `create announcement should successfully create announcement`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val announcement = Announcement(
                text = "Important announcement",
                userId = "host1",
                createdAt = "2025-12-01T10:00:00Z",
                updatedAt = "2025-12-01T10:00:00Z"
            )

            val created = announcementDao.create(announcement, "event1")

            assertEquals("Important announcement", created.text)
            assertEquals("host1", created.userId)
        }
    }

    @Test
    fun `getByEventId should return announcements newest first`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            announcementDao.create(
                Announcement(
                    "First",
                    "host1",
                    "2025-12-01T10:00:00Z",
                    "2025-12-01T10:00:00Z"
                ), "event1"
            )
            announcementDao.create(
                Announcement(
                    "Second",
                    "host1",
                    "2025-12-01T10:01:00Z",
                    "2025-12-01T10:01:00Z"
                ), "event1"
            )
            announcementDao.create(
                Announcement(
                    "Third",
                    "host1",
                    "2025-12-01T10:02:00Z",
                    "2025-12-01T10:02:00Z"
                ), "event1"
            )

            val announcements = announcementDao.getByEventId("event1")

            assertEquals(3, announcements.size)
            assertEquals("Third", announcements[0].text) // Newest first
        }
    }

    // Category Tests

    @Test
    fun `create mutable category should successfully create category`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            val category = MutableCategory(name = "Bring Items")

            val created = mutableCategoryDao.create(category, "event1")

            assertEquals("Bring Items", created.name)
        }
    }

    @Test
    fun `getByEventId should return all mutable categories for event`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userDao.create(host)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            eventDao.create(event)

            mutableCategoryDao.create(MutableCategory("Category 1"), "event1")
            mutableCategoryDao.create(MutableCategory("Category 2"), "event1")

            val categories = mutableCategoryDao.getByEventId("event1")

            assertEquals(2, categories.size)
            assertTrue(categories.any { it.name == "Category 1" })
            assertTrue(categories.any { it.name == "Category 2" })
        }
    }
}
