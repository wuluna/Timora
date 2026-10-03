package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.MutableCategory

/**
 * Data Access Object interface for MutableCategory operations.
 */
interface IMutableCategoryDao {
    suspend fun create(category: MutableCategory, eventId: String): MutableCategory

    suspend fun update(eventId: String, categoryName: String, category: MutableCategory)

    suspend fun getByEventId(eventId: String): List<MutableCategory>

    suspend fun delete(categoryId: String)
}
