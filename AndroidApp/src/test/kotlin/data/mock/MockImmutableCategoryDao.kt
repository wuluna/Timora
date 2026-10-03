package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IImmutableCategoryDao
import cs.uwaterloo.domain.entity.ImmutableCategory

/**
 * Mock implementation of IImmutableCategoryDao for testing.
 */
class MockImmutableCategoryDao : IImmutableCategoryDao {
    private val immutableCategories =
        mutableMapOf<String, MutableList<ImmutableCategory>>() // eventId -> List<ImmutableCategory>

    fun clear() {
        immutableCategories.clear()
    }

    override suspend fun create(category: ImmutableCategory, eventId: String): ImmutableCategory {
        val eventCategories = immutableCategories.getOrPut(eventId) { mutableListOf() }
        eventCategories.add(category)
        return category
    }

    override suspend fun update(
        eventId: String,
        categoryName: String,
        category: ImmutableCategory
    ) {
        val eventCategories = immutableCategories[eventId]
            ?: throw IllegalArgumentException("Event with ID $eventId does not exist")

        val index = eventCategories.indexOfFirst { it.name == categoryName }
        if (index == -1) {
            throw IllegalArgumentException("Immutable category with name $categoryName does not exist in event $eventId")
        }

        eventCategories[index] = category
    }

    override suspend fun getByEventId(eventId: String): List<ImmutableCategory> {
        return immutableCategories[eventId]?.toList() ?: emptyList()
    }

    override suspend fun delete(categoryId: String) {
        immutableCategories.values.forEach { list ->
            list.removeIf { it.name == categoryId }
        }
    }
}
