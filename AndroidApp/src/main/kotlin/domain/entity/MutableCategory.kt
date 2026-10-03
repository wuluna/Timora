@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.domain.entity

import kotlinx.serialization.Serializable

@Serializable
class MutableCategory(
    val name: String,
    // key: item name, value: person who added the item
    var items: MutableMap<String, String> = mutableMapOf(),
) {

    init {
        require(name.isNotBlank()) { "Category name cannot be blank" }
    }

    /**
     * adds an item to the category list
     */
    fun addItem(user: String, item: String) {
        items[item] = user
    }

    /**
     * removes an item from the category list only if the user removing is the same as the
     * user who originally added the item
     */
    fun removeItem(user: String, item: String) {
        if (items[item] == null) {
            throw IllegalArgumentException("Item doesn't exist in list!")
        } else if (items[item] == user) {
            items.remove(item)
        } else {
            throw IllegalArgumentException("Cannot remove item from list")
        }
    }
}
