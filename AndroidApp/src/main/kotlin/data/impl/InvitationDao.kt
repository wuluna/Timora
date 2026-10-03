@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IInvitationDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.Invitation
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IInvitationDao.
 */
class InvitationDao(private val db: IDb) : IInvitationDao {

    @Serializable
    private data class InvitationDTO(
        val id: String? = null,
        val event_id: String,
        val inviter_id: String,
        val invitee_id: String,
        val status: String = "PENDING",
        val sent_at: String? = null
    )

    override suspend fun create(invitation: Invitation): Invitation {
        val dto = InvitationDTO(
            event_id = invitation.eventId,
            inviter_id = invitation.inviterId,
            invitee_id = invitation.inviteeId,
            status = "PENDING"
        )
        db.client.from("invitations").insert(dto)
        return invitation
    }

    override suspend fun updateStatus(eventId: String, inviteeId: String, status: String) {
        db.client.from("invitations").update(
            mapOf("status" to status)
        ) {
            filter {
                eq("event_id", eventId)
                eq("invitee_id", inviteeId)
            }
        }
    }

    override suspend fun getByInviteeId(inviteeId: String): List<Invitation> {
        val invitationDtos = db.client.from("invitations")
            .select {
                filter { eq("invitee_id", inviteeId) }
            }
            .decodeList<InvitationDTO>()

        return invitationDtos.map { dto ->
            Invitation(
                eventId = dto.event_id,
                inviterId = dto.inviter_id,
                inviteeId = dto.invitee_id,
                sentAt = dto.sent_at ?: ""
            )
        }
    }

    override suspend fun getByEventId(eventId: String): List<Invitation> {
        val invitationDtos = db.client.from("invitations")
            .select {
                filter { eq("event_id", eventId) }
            }
            .decodeList<InvitationDTO>()

        return invitationDtos.map { dto ->
            Invitation(
                eventId = dto.event_id,
                inviterId = dto.inviter_id,
                inviteeId = dto.invitee_id,
                sentAt = dto.sent_at ?: ""
            )
        }
    }

    override suspend fun delete(eventId: String, inviteeId: String) {
        db.client.from("invitations").delete {
            filter {
                eq("event_id", eventId)
                eq("invitee_id", inviteeId)
            }
        }
    }
}
