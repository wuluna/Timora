package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.User
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserTest {

    @Test
    fun `test user creation with valid data`() {
        val user = User(
            id = "user123",
            email = "test@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        assertEquals("user123", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("John Doe", user.name)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
        assertEquals("", user.bio)
        assertTrue(user.hostedEventIds.isEmpty())
        assertTrue(user.attendedEventIds.isEmpty())
        assertTrue(user.invitationList.isEmpty())
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }

    @Test
    fun `test updateBio updates bio and updatedAt`() {
        val user = User(
            id = "user123",
            email = "test@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        val originalUpdatedAt = user.updatedAt
        Thread.sleep(10) // Ensure time difference

        user.updateBio("New bio content")

        assertEquals("New bio content", user.bio)
        assertTrue(user.updatedAt > originalUpdatedAt)
    }

    @Test
    fun `test addHostedEvent adds event successfully`() {
        val user = User(
            id = "user123",
            email = "test@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        user.addHostedEvent("event1")

        assertTrue(user.hostedEventIds.contains("event1"))
        assertEquals(1, user.hostedEventIds.size)
    }

    @Test
    fun `test addAttendedEvent adds event successfully`() {
        val user = User(
            id = "user123",
            email = "test@example.com",
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        user.addAttendedEvent("event1")

        assertTrue(user.attendedEventIds.contains("event1"))
        assertEquals(1, user.attendedEventIds.size)
    }
}