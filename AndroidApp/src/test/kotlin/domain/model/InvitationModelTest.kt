package cs.uwaterloo.domain.model

import cs.uwaterloo.data.mock.MockDb
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.domain.entity.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InvitationModelTest {
    private lateinit var mockDb: MockDb
    private lateinit var invitationModel: InvitationModel

    @BeforeEach
    fun setup() {
        mockDb = MockDb()
        mockDb.clear()
        invitationModel = InvitationModel(
            invitationDao = mockDb.invitationDao,
            eventDao = mockDb.eventDao,
            userDao = mockDb.userDao,
            attendeeDao = mockDb.attendeeDao
        )
    }

    @Test
    fun `sendInvitation should successfully create invitation`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            val invitee = User(
                id = "invitee1",
                email = "invitee@example.com",
                name = "Invitee User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)
            mockDb.userDao.create(invitee)
            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            mockDb.eventDao.create(event)
            val invitation = invitationModel.sendInvitation("event1", "host1", "invitee1")

            assertNotNull(invitation)
            assertEquals("event1", invitation.eventId)
            assertEquals("host1", invitation.inviterId)
            assertEquals("invitee1", invitation.inviteeId)
        }
    }

    @Test
    fun `getInvitationsByUser returns invitations`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            val invitee = User(
                id = "invitee1",
                email = "invitee@example.com",
                name = "Invitee User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)
            mockDb.userDao.create(invitee)
            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            mockDb.eventDao.create(event)
            invitationModel.sendInvitation("event1", "host1", "invitee1")
            val invitations = invitationModel.getInvitationsByUser("invitee1")

            assertEquals(1, invitations.size)
            assertEquals("event1", invitations[0].eventId)
        }
    }

    @Test
    fun `acceptInvitation adds user as attendee, removes invitation`() {
        runBlocking {
            val host = User(
                id = "host1",
                email = "host@example.com",
                name = "Host User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            val invitee = User(
                id = "invitee1",
                email = "invitee@example.com",
                name = "Invitee User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            mockDb.userDao.create(host)
            mockDb.userDao.create(invitee)

            val event = Event(
                id = "event1",
                title = "Test Event",
                host = host,
                dateTime = "2025-12-01T10:00:00Z",
                location = "Test Location"
            )
            mockDb.eventDao.create(event)

            invitationModel.sendInvitation("event1", "host1", "invitee1")
            invitationModel.acceptInvitation("event1", "invitee1")

            val attendees = mockDb.attendeeDao.getByEventId("event1")
            val invitations = mockDb.invitationDao.getByEventId("event1")

            assertEquals(1, attendees.size)
            assertEquals("invitee1", attendees[0].userId)
            assertEquals(0, invitations.size)
        }
    }
}
