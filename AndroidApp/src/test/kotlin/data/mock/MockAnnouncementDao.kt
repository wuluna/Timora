package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IAnnouncementDao
import cs.uwaterloo.domain.entity.Announcement

/**
 * Mock implementation of IAnnouncementDao for testing.
 */
class MockAnnouncementDao : IAnnouncementDao {
    private val announcements =
        mutableMapOf<String, MutableList<Announcement>>() // eventId -> List<Announcement>

    fun clear() {
        announcements.clear()
    }

    override suspend fun create(announcement: Announcement, eventId: String): Announcement {
        val eventAnnouncements = announcements.getOrPut(eventId) { mutableListOf() }
        eventAnnouncements.add(announcement)
        return announcement
    }

    override suspend fun update(announcementId: String, newText: String) {
        var found = false
        announcements.values.forEach { list ->
            list.find { it.userId == announcementId }?.let { announcement ->
                announcement.updateText(newText)
                found = true
            }
        }
        if (!found) {
            throw IllegalArgumentException("Announcement with ID $announcementId does not exist")
        }
    }

    override suspend fun getByEventId(eventId: String): List<Announcement> {
        return announcements[eventId]?.reversed() ?: emptyList() // Newest first
    }

    override suspend fun delete(announcementId: String) {
        announcements.values.forEach { list ->
            list.removeIf { it.userId == announcementId }
        }
    }
}
