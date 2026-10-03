package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IInvitationDao
import cs.uwaterloo.domain.entity.Invitation

/**
 * Mock implementation of IInvitationDao for testing.
 */
class MockInvitationDao : IInvitationDao {
    private val invitations =
        mutableMapOf<String, MutableMap<String, Invitation>>() // eventId -> (inviteeId -> Invitation)

    fun clear() {
        invitations.clear()
    }

    override suspend fun create(invitation: Invitation): Invitation {
        val eventInvitations = invitations.getOrPut(invitation.eventId) { mutableMapOf() }
        if (eventInvitations.containsKey(invitation.inviteeId)) {
            throw IllegalArgumentException("Invitation already exists for user ${invitation.inviteeId} to event ${invitation.eventId}")
        }
        eventInvitations[invitation.inviteeId] = invitation
        return invitation
    }

    override suspend fun updateStatus(eventId: String, inviteeId: String, status: String) {
        val eventInvitations = invitations[eventId]
            ?: throw IllegalArgumentException("Event with ID $eventId does not exist")
        eventInvitations[inviteeId]
            ?: throw IllegalArgumentException("Invitation does not exist for user $inviteeId to event $eventId")

        // Note: Invitation is immutable, so in a real scenario we'd recreate it
        // For now, this is a placeholder
    }

    override suspend fun getByInviteeId(inviteeId: String): List<Invitation> {
        return invitations.values.flatMap { it.values }.filter { it.inviteeId == inviteeId }
    }

    override suspend fun getByEventId(eventId: String): List<Invitation> {
        return invitations[eventId]?.values?.toList() ?: emptyList()
    }

    override suspend fun delete(eventId: String, inviteeId: String) {
        invitations[eventId]?.remove(inviteeId)
    }
}
