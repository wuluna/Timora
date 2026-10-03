package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.Announcement
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AnnouncementTest {

    @Test
    fun `test announcement creation with valid data`() {
        val announcement = Announcement(
            text = "this is an announcement",
            userId = "user1",
            createdAt = "2025-12-01T10:00:00Z",
            updatedAt = "2025-12-01T10:00:00Z"
        )

        assertEquals("user1", announcement.userId)
        assertEquals("this is an announcement", announcement.text)
        assertNotNull(announcement.createdAt)
        assertNotNull(announcement.updatedAt)
    }

    @Test
    fun `test announcement text update with valid newText`() {
        val announcement = Announcement(
            text = "this is an announcement",
            userId = "user1",
            createdAt = "2025-12-01T10:00:00Z",
            updatedAt = "2025-12-01T10:00:00Z"
        )

        val newText = "new announcement"

        announcement.updateText(newText)

        assertEquals(newText, announcement.text)
    }
}