package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Role
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AttendeeTest {

    @Test
    fun `test attendee creation with valid data`() {
        val attendee = Attendee(
            userId = "testuser1",
            name = "test1",
            role = Role.GUEST
        )

        assertEquals("testuser1", attendee.userId)
        assertEquals(Role.GUEST, attendee.role)
        assertNotNull(attendee.createdAttendeeAt)
        assertNotNull(attendee.updatedAttendeeAt)

        attendee.updateRole(Role.HOST)
        assertEquals(Role.HOST, attendee.role)
    }

    @Test
    fun `test attendee role update`() {
        val attendee = Attendee(
            userId = "testuser2",
            name = "test2",
        )

        assertEquals(Role.INVITEE, attendee.role)

        attendee.updateRole(Role.HOST)

        assertEquals(Role.HOST, attendee.role)
    }
}
