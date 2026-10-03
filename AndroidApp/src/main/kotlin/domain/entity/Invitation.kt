@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Domain model representing an invitation.
 */
@Serializable
data class Invitation(
    val eventId: String,
    val inviterId: String,
    val inviteeId: String,
    val sentAt: String = Instant.now().toString()
) {
    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(inviterId.isNotBlank()) { "Inviter cannot be blank" }
        require(inviteeId.isNotBlank()) { "Invitee cannot be blank" }
    }
}
