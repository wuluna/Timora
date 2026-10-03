package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IMutableCategoryDao
import cs.uwaterloo.domain.entity.MutableCategory

/**
 * Mock implementation of IMutableCategoryDao for testing.
 */
class MockMutableCategoryDao : IMutableCategoryDao {
    private val mutableCategories =
        mutableMapOf<String, MutableList<MutableCategory>>() // eventId -> List<MutableCategory>

    fun clear() {
        mutableCategories.clear()
    }

    override suspend fun create(category: MutableCategory, eventId: String): MutableCategory {
        val eventCategories = mutableCategories.getOrPut(eventId) { mutableListOf() }
        eventCategories.add(category)
        return category
    }

    override suspend fun update(eventId: String, categoryName: String, category: MutableCategory) {
        val eventCategories = mutableCategories[eventId]
            ?: throw IllegalArgumentException("Event with ID $eventId does not exist")

        val index = eventCategories.indexOfFirst { it.name == categoryName }
        if (index == -1) {
            throw IllegalArgumentException("Mutable category with name $categoryName does not exist in event $eventId")
        }

        eventCategories[index] = category
    }

    override suspend fun getByEventId(eventId: String): List<MutableCategory> {
        return mutableCategories[eventId]?.toList() ?: emptyList()
    }

    override suspend fun delete(categoryId: String) {
        mutableCategories.values.forEach { list ->
            list.removeIf { it.name == categoryId }
        }
    }
}
