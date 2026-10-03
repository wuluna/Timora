@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class ImmutableCategory(
    val name: String,
    // key: item name, value: list of people who checked off the item
    var items: MutableMap<String, MutableSet<String>> = mutableMapOf(),
) {

    init {
        require(name.isNotBlank()) { "Category name cannot be blank" }
    }

    /**
     * returns the list of users who checked off a certain item
     */
    fun getCheckedUsers(item: String): MutableSet<String>? {
        if (items[item] == null) {
            throw IllegalArgumentException("Item is not in Immutable Category List")
        } else {
            return items[item]
        }
    }

    /**
     * checks off an item in the category list for a given user
     */
    fun checkItem(user: String, item: String) {
        if (items[item] == null) {
            throw IllegalArgumentException("Item is not in Immutable Category List")
        } else {
            items[item]?.add(user)
        }
    }

    /**
     * unchecks an item in the category list for a given user
     */
    fun uncheckItem(user: String, item: String) {
        if (items[item] == null) {
            throw IllegalArgumentException("Item is not in Immutable Category List")
        } else {
            items[item]?.remove(user)
        }
    }

    /**
     * adds an item to the category list only if the user is a host
     */
    fun addItem(attendee: Attendee, item: String) {
        if (attendee.role == Role.HOST || attendee.role == Role.CO_HOST) {
            if (items[item] == null) {
                items[item] = mutableSetOf()
            } else {
                throw IllegalArgumentException("Item already in list!")
            }
        } else {
            throw IllegalArgumentException("This attendee is not a host or co-host")
        }
    }

    /**
     * removes an item to the category list only if the user is a host
     */
    fun removeItem(attendee: Attendee, item: String) {
        if (attendee.role == Role.HOST || attendee.role == Role.CO_HOST) {
            if (items[item] != null) {
                items.remove(item)
            } else {
                throw IllegalArgumentException("Item doesn't exist in list!")
            }
        } else {
            throw IllegalArgumentException("This attendee is not a host or co-host")
        }
    }

}
