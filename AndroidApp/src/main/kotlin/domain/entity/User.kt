@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Domain model representing a user in the system.
 *
 * Name and avatarUrl come from Google and cannot be changed.
 * Only bio can be updated
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    var name: String,
    val avatarUrl: String,
    var bio: String = "",
    var hostedEventIds: Set<String> = emptySet(),
    var attendedEventIds: Set<String> = emptySet(),
    var invitationList: Set<Invitation> = emptySet(),
) {
    val createdAt: String = Instant.now().toString()
    var updatedAt: String = createdAt

    init {
        require(id.isNotBlank()) { "User ID cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.contains("@")) { "Invalid email format" }
    }

    /**
     * Updates the user's name.
     */
    fun updateName(newName: String) {
        name = newName
        updatedAt = Instant.now().toString()
    }

    /**
     * Updates the user's bio.
     */
    fun updateBio(newBio: String) {
        bio = newBio
        updatedAt = Instant.now().toString()
    }

    /**
     * Adds an event to the user's hosted events list.
     * Called when user creates a new event.
     */
    fun addHostedEvent(eventId: String) {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(!hostedEventIds.contains(eventId)) { "Event already in hosted list" }
        hostedEventIds = hostedEventIds + eventId
        updatedAt = Instant.now().toString()
    }

    /**
     * Adds an event to the user's attended events list.
     * Called when user accepts an event invitation.
     */
    fun addAttendedEvent(eventId: String) {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(!attendedEventIds.contains(eventId)) { "Event already in attended list" }
        attendedEventIds = attendedEventIds + eventId
        updatedAt = Instant.now().toString()
    }
}
