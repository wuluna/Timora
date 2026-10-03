package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.Announcement

/**
 * Data Access Object interface for Announcement operations.
 */
interface IAnnouncementDao {
    suspend fun create(announcement: Announcement, eventId: String): Announcement

    suspend fun update(announcementId: String, newText: String)

    suspend fun getByEventId(eventId: String): List<Announcement>

    suspend fun delete(announcementId: String)
}
