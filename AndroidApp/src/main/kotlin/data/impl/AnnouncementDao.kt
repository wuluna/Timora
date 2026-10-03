@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IAnnouncementDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.Announcement
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IAnnouncementDao.
 */
class AnnouncementDao(private val db: IDb) : IAnnouncementDao {

    @Serializable
    private data class AnnouncementDTO(
        val id: String? = null,
        val event_id: String,
        val user_id: String,
        val text: String,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    override suspend fun create(announcement: Announcement, eventId: String): Announcement {
        val dto = AnnouncementDTO(
            event_id = eventId,
            user_id = announcement.userId,
            text = announcement.text
        )
        db.client.from("announcements").insert(dto)
        return announcement
    }

    override suspend fun update(announcementId: String, newText: String) {
        db.client.from("announcements").update(
            mapOf("text" to newText)
        ) {
            filter { eq("id", announcementId) }
        }
    }

    override suspend fun getByEventId(eventId: String): List<Announcement> {
        val announcementDtos = db.client.from("announcements")
            .select {
                filter { eq("event_id", eventId) }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<AnnouncementDTO>()

        return announcementDtos.map { dto ->
            Announcement(
                text = dto.text,
                userId = dto.user_id,
                createdAt = dto.created_at
                    ?: throw IllegalStateException("Missing created_at from database"),
                updatedAt = dto.updated_at
                    ?: throw IllegalStateException("Missing updated_at from database")
            )
        }
    }

    override suspend fun delete(announcementId: String) {
        db.client.from("announcements").delete {
            filter { eq("id", announcementId) }
        }
    }
}
