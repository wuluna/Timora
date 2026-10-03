@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IMutableCategoryDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.MutableCategory
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IMutableCategoryDao.
 */
class MutableCategoryDao(private val db: IDb) : IMutableCategoryDao {

    @Serializable
    private data class MutableCategoryDTO(
        val id: String? = null,
        val event_id: String,
        val name: String,
        val items: Map<String, String> = emptyMap()
    )

    override suspend fun create(category: MutableCategory, eventId: String): MutableCategory {
        val dto = MutableCategoryDTO(
            event_id = eventId,
            name = category.name,
            items = category.items
        )
        db.client.from("mutable_categories").insert(dto)
        return category
    }

    override suspend fun update(eventId: String, categoryName: String, category: MutableCategory) {
        db.client.from("mutable_categories").update(
            mapOf("items" to category.items)
        ) {
            filter {
                eq("event_id", eventId)
                eq("name", categoryName)
            }
        }
    }

    override suspend fun getByEventId(eventId: String): List<MutableCategory> {
        val categoryDtos = db.client.from("mutable_categories")
            .select {
                filter { eq("event_id", eventId) }
            }
            .decodeList<MutableCategoryDTO>()

        return categoryDtos.map { dto ->
            MutableCategory(
                name = dto.name,
                items = dto.items.toMutableMap()
            )
        }
    }

    override suspend fun delete(categoryId: String) {
        db.client.from("mutable_categories").delete {
            filter { eq("id", categoryId) }
        }
    }
}
