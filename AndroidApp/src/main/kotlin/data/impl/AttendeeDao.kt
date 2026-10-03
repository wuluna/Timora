@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Role
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IAttendeeDao.
 */
class AttendeeDao(private val db: IDb) : IAttendeeDao {

    @Serializable
    private data class AttendeeDTO(
        val id: String? = null,
        val user_id: String,
        val event_id: String,
        val name: String,
        val role: String,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    override suspend fun add(attendee: Attendee, eventId: String) {
        val dto = AttendeeDTO(
            user_id = attendee.userId,
            event_id = eventId,
            name = attendee.name,
            role = attendee.role.toString()
        )
        db.client.from("attendees").insert(dto)
    }

    override suspend fun remove(userId: String, eventId: String) {
        db.client.from("attendees").delete {
            filter {
                eq("user_id", userId)
                eq("event_id", eventId)
            }
        }
    }

    override suspend fun getByEventId(eventId: String): List<Attendee> {
        val attendeeDtos = db.client.from("attendees")
            .select {
                filter { eq("event_id", eventId) }
            }
            .decodeList<AttendeeDTO>()

        return attendeeDtos.map { dto ->
            Attendee(
                userId = dto.user_id,
                name = dto.name,
                role = Role.valueOf(dto.role)
            )
        }
    }

    override suspend fun updateRole(userId: String, eventId: String, role: Role) {
        db.client.from("attendees").update(
            mapOf("role" to role.toString())
        ) {
            filter {
                eq("user_id", userId)
                eq("event_id", eventId)
            }
        }
    }

    /**
     * Helper method to get event IDs that a user is attending.
     * Used by EventDao to implement getByAttendeeId.
     */
    suspend fun getEventIdsByUserId(userId: String): List<String> {
        val attendeeDtos = db.client.from("attendees")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<AttendeeDTO>()

        return attendeeDtos.map { it.event_id }
    }
}
