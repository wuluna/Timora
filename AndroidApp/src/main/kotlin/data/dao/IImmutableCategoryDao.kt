package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.ImmutableCategory

/**
 * Data Access Object interface for ImmutableCategory operations.
 */
interface IImmutableCategoryDao {
    suspend fun create(category: ImmutableCategory, eventId: String): ImmutableCategory

    suspend fun update(eventId: String, categoryName: String, category: ImmutableCategory)

    suspend fun getByEventId(eventId: String): List<ImmutableCategory>

    suspend fun delete(categoryId: String)
}
