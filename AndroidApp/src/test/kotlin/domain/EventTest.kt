package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.domain.entity.ImmutableCategory
import cs.uwaterloo.domain.entity.MutableCategory
import cs.uwaterloo.domain.entity.Role
import cs.uwaterloo.domain.entity.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EventTest {

    @Test
    fun `test event creation with valid data`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )

        assertEquals(event.id, "event123")
        assertEquals(event.title, "title")
        assertEquals(event.host, user)
        assertEquals(event.location, "Location")
        assertEquals(event.dateTime, "2025-10-09T16:14:59Z")
        assertEquals(event.description, "")
        assertEquals(event.attendeeList, emptySet())
        assertEquals(event.mutableCategoryList, emptySet())
        assertEquals(event.immutableCategoryList, emptySet())
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }

    @Test
    fun `test updateDateTime updates dateTime`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        val newDateTime = LocalDateTime.now().toString()
        event.updateDateTime(newDateTime)
        assertEquals(newDateTime, event.dateTime)
    }

    // Test updateLocation
    @Test
    fun `test updateLocation updates location`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        val newLocation = "Harbin's House"
        event.updateLocation(newLocation)
        assertEquals(newLocation, event.location)
    }

    // Test addAttendee
    @Test
    fun `test addAttendee adds attendee successfully`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        event.addAttendee(user, Role.INVITEE)
        assertTrue(event.attendeeList.contains(Attendee(user.id, user.name, Role.INVITEE)))
        assertEquals(1, event.attendeeList.size)
    }

    // Test addMutableCategory
    @Test
    fun `test addMutableCategory adds category successfully`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        val category = MutableCategory("category")
        event.addMutableCategory(category)
        assertTrue(event.mutableCategoryList.contains(category))
        assertEquals(1, event.mutableCategoryList.size)
    }

    // Test addImmutableCategory
    @Test
    fun `test addImmutableCategory adds category successfully`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        val category = ImmutableCategory("category")
        event.addImmutableCategory(category)
        assertTrue(event.immutableCategoryList.contains(category))
        assertEquals(1, event.immutableCategoryList.size)
    }

    // Test addAnnouncement with host
    @Test
    fun `test addAnnouncement adds announcement successfully when user is host`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        event.addAnnouncement(user, "Important announcement")
        assertEquals(1, event.announcementList.size)
        assertEquals("Important announcement", event.announcementList[0].text)
        assertEquals(user.id, event.announcementList[0].userId)
    }

    // Test addAnnouncement with non-host
    @Test
    fun `test addAnnouncement throws exception when user is not host`() {
        val host = User(
            id = "host123",
            email = "host@example.com",
            name = "Host",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val nonHost = User(
            id = "user456",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = host,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        val exception = assertThrows<IllegalArgumentException> {
            event.addAnnouncement(nonHost, "Unauthorized announcement")
        }
        assertEquals("Only the host can make announcements ", exception.message)
    }

    // Test addAnnouncement with blank text
    @Test
    fun `test addAnnouncement throws exception when text is blank`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )
        val event = Event(
            id = "event123",
            title = "title",
            host = user,
            location = "Location",
            dateTime = "2025-10-09T16:14:59Z"
        )
        val exception = assertThrows<IllegalArgumentException> {
            event.addAnnouncement(user, "")
        }
        assertEquals("Announcement cannot be blank", exception.message)
    }
}