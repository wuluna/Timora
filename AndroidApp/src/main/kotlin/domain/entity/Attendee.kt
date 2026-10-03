@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Domain model representing an attendee, which is a user with an editable role.
 * Associated with Announcement, Category, and Event data classes.
 */

@Serializable
data class Attendee(
    val userId: String,
    val name: String,
    var role: Role = Role.INVITEE, // default role: invitee
) {
    val createdAttendeeAt: String = Instant.now().toString()
    var updatedAttendeeAt: String = createdAttendeeAt

    init {
        require(userId.isNotBlank()) { "userId cannot be blank" }
    }

    fun updateRole(role: Role) {
        require(role in Role.values()) { "Role $role cannot be in ${Role.values()}" }
        this.role = role
        updatedAttendeeAt = Instant.now().toString()
    }

}
