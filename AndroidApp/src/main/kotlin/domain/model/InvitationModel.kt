package cs.uwaterloo.domain.model

import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IInvitationDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Invitation
import cs.uwaterloo.domain.entity.Role

/**
 * Model class that coordinates invitation-related operations.
 * Orchestrates between domain entities (business logic) and DAOs (data persistence).
 */
class InvitationModel(
    private val invitationDao: IInvitationDao,
    private val eventDao: IEventDao,
    private val userDao: IUserDao,
    private val attendeeDao: IAttendeeDao
) {
    suspend fun sendInvitation(eventId: String, inviterId: String, inviteeId: String): Invitation {
        eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event with ID $eventId not found")

        userDao.getById(inviterId)
            ?: throw IllegalArgumentException("Inviter with ID $inviterId not found")

        userDao.getById(inviteeId)
            ?: throw IllegalArgumentException("Invitee with ID $inviteeId not found")

        val invitation = Invitation(
            eventId = eventId,
            inviterId = inviterId,
            inviteeId = inviteeId
        )

        return invitationDao.create(invitation)
    }

    suspend fun getInvitationsByUser(userId: String): List<Invitation> {
        return invitationDao.getByInviteeId(userId)
    }

    suspend fun getInvitationsByEvent(eventId: String): List<Invitation> {
        return invitationDao.getByEventId(eventId)
    }

    suspend fun acceptInvitation(eventId: String, inviteeId: String) {
        val invitations = invitationDao.getByEventId(eventId)
        invitations.find { it.inviteeId == inviteeId }
            ?: throw IllegalArgumentException("Invitation not found for user $inviteeId to event $eventId")

        val user = userDao.getById(inviteeId)
            ?: throw IllegalArgumentException("User with ID $inviteeId not found")

        val attendee = Attendee(
            userId = user.id,
            name = user.name,
            role = Role.GUEST
        )
        attendeeDao.add(attendee, eventId)

        invitationDao.delete(eventId, inviteeId)
    }

    suspend fun rejectInvitation(eventId: String, inviteeId: String) {
        val invitations = invitationDao.getByEventId(eventId)
        invitations.find { it.inviteeId == inviteeId }
            ?: throw IllegalArgumentException("Invitation not found for user $inviteeId to event $eventId")

        invitationDao.updateStatus(eventId, inviteeId, "REJECTED")
    }

    suspend fun deleteInvitation(eventId: String, inviteeId: String) {
        invitationDao.delete(eventId, inviteeId)
    }

    suspend fun sendInvitationWithValidation(
        eventId: String,
        inviterId: String,
        inviteeId: String
    ): Invitation {
        eventDao.getById(eventId)
            ?: throw IllegalArgumentException("Event not found")

        val invitee = userDao.getById(inviteeId)
            ?: throw IllegalArgumentException("User not found")

        // invitee is not already an attendee
        val attendees = attendeeDao.getByEventId(eventId)
        if (attendees.any { it.userId == inviteeId }) {
            throw IllegalArgumentException("${invitee.name} is already an attendee")
        }

        // inviter has permission (HOST or CO_HOST)
        val inviterAttendee = attendees.find { it.userId == inviterId }
        if (inviterAttendee == null ||
            (inviterAttendee.role != Role.HOST && inviterAttendee.role != Role.CO_HOST)
        ) {
            throw IllegalArgumentException("Only hosts and co-hosts can invite")
        }

        // if invitation already exists
        val existingInvitations = invitationDao.getByEventId(eventId)
        if (existingInvitations.any { it.inviteeId == inviteeId }) {
            throw IllegalArgumentException("${invitee.name} has already been invited")
        }

        return sendInvitation(eventId, inviterId, inviteeId)
    }

    suspend fun getUsersForInvitation(eventId: String): List<cs.uwaterloo.domain.entity.User> {
        val allUsers = userDao.getAllUsers()
        val attendees = attendeeDao.getByEventId(eventId)
        val pendingInvitations = invitationDao.getByEventId(eventId)

        val attendeeUserIds = attendees.map { it.userId }.toSet()
        val invitedUserIds = pendingInvitations.map { it.inviteeId }.toSet()

        return allUsers.filter { user ->
            user.id !in attendeeUserIds && user.id !in invitedUserIds
        }
    }

    suspend fun getEventById(eventId: String): cs.uwaterloo.domain.entity.Event? {
        return eventDao.getById(eventId)
    }

    suspend fun getUserById(userId: String): cs.uwaterloo.domain.entity.User? {
        return userDao.getById(userId)
    }
}
