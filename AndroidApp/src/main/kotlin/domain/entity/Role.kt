@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable

/**
 * Defines the roles a user can have within an event.
 */
@Serializable
enum class Role {
    HOST, CO_HOST, GUEST, INVITEE
}
