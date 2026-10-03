package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.Invitation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InvitationTest {

    @Test
    fun `test invitation creation with valid data`() {
        val invitation = Invitation(
            eventId = "event123",
            inviterId = "John Doe",
            inviteeId = "Ellen Smith",
        )

        assertEquals("event123", invitation.eventId)
        assertEquals("John Doe", invitation.inviterId)
        assertEquals("Ellen Smith", invitation.inviteeId)
        assertNotNull(invitation.sentAt)
    }
}
