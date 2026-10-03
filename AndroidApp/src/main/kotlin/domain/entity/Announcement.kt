@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Domain model representing an immutable announcement,
 * associated with the creation datetime and the user
 */
@Serializable
data class Announcement(
    var text: String,
    val userId: String,
    val createdAt: String,
    var updatedAt: String
) {
    init {
        require(text.isNotBlank()) { "Text cannot be blank" }
        require(userId.isNotBlank()) { "userId cannot be blank" }
    }

    /**
     * Updates the announcement text
     */
    fun updateText(newText: String) {
        require(newText.isNotBlank()) { "Text cannot be blank" }
        text = newText
        updatedAt = Instant.now().toString()
    }
}
