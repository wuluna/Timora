package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.Invitation

/**
 * Data Access Object interface for Invitation operations.
 */
interface IInvitationDao {
    suspend fun create(invitation: Invitation): Invitation

    suspend fun updateStatus(eventId: String, inviteeId: String, status: String)

    suspend fun getByInviteeId(inviteeId: String): List<Invitation>

    suspend fun getByEventId(eventId: String): List<Invitation>

    suspend fun delete(eventId: String, inviteeId: String)
}
