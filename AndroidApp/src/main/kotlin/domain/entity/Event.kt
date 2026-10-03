@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Domain model representing an event.
 */
@Serializable
data class Event(
    val id: String,
    val title: String,
    val host: User,
    var dateTime: String = "",
    var location: String = "",
    var description: String = "",
    var attendeeList: MutableSet<Attendee> = mutableSetOf(),
    var announcementList: MutableList<Announcement> = mutableListOf(),
    var mutableCategoryList: MutableSet<MutableCategory> = mutableSetOf(),
    var immutableCategoryList: MutableSet<ImmutableCategory> = mutableSetOf(),
) {
    val createdAt: String = Instant.now().toString()
    var updatedAt: String = createdAt

    init {
        require(id.isNotBlank()) { "Event ID cannot be blank" }
        require(title.isNotBlank()) { "Event title cannot be blank" }
        require(host.name.isNotBlank()) { "Event host name cannot be blank" }
        require(location.isNotBlank()) { "Event location cannot be blank" }
        require(dateTime.isNotBlank()) { "Event dateTime cannot be blank" }
    }

    /**
     * Updates the date and time of the event
     */
    fun updateDateTime(newDateTime: String) {
        dateTime = newDateTime
        updatedAt = Instant.now().toString()
    }

    /**
     * Updates the location of the event
     */
    fun updateLocation(newLocation: String) {
        location = newLocation
        updatedAt = Instant.now().toString()
    }

    /**
     * Adds an attendee to the events attendee list
     */
    fun addAttendee(user: User, role: Role) {
        require(user.id.isNotBlank()) { "User cannot be blank" }
        val newAttendee = Attendee(user.id, user.name, role)
        require(!attendeeList.contains(newAttendee)) { "User already in attendee list" }
        attendeeList += newAttendee
        updatedAt = Instant.now().toString()
    }

    /**
     * Adds a category to the event
     */
    fun addMutableCategory(category: MutableCategory) {
        require(category.name.isNotBlank()) { "Category cannot be blank" }
        require(!mutableCategoryList.contains(category)) { "Category already in category list" }
        mutableCategoryList += category
        updatedAt = Instant.now().toString()
    }

    /**
     * Adds a category to the event
     */
    fun addImmutableCategory(category: ImmutableCategory) {
        require(category.name.isNotBlank()) { "Category cannot be blank" }
        require(!immutableCategoryList.contains(category)) { "Category already in category list" }
        immutableCategoryList += category
        updatedAt = Instant.now().toString()
    }

    /**
     * Adds an announcement
     */
    fun addAnnouncement(user: User, text: String) {
        require(text.isNotBlank()) { "Announcement cannot be blank" }
        require(user.id == host.id) { "Only the host can make announcements " }
        val now = Instant.now().toString()
        val announcement = Announcement(
            text = text,
            userId = user.id,
            createdAt = now,
            updatedAt = now
        )
        announcementList += announcement
        updatedAt = now
    }
}
