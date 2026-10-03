@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IImmutableCategoryDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.ImmutableCategory
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IImmutableCategoryDao.
 */
class ImmutableCategoryDao(private val db: IDb) : IImmutableCategoryDao {

    @Serializable
    private data class ImmutableCategoryDTO(
        val id: String? = null,
        val event_id: String,
        val name: String,
        val items: Map<String, List<String>> = emptyMap()
    )

    override suspend fun create(category: ImmutableCategory, eventId: String): ImmutableCategory {
        val itemsMap = category.items.mapValues { it.value.toList() }
        val dto = ImmutableCategoryDTO(
            event_id = eventId,
            name = category.name,
            items = itemsMap
        )
        db.client.from("immutable_categories").insert(dto)
        return category
    }

    override suspend fun update(
        eventId: String,
        categoryName: String,
        category: ImmutableCategory
    ) {
        val itemsMap = category.items.mapValues { it.value.toList() }

        db.client.from("immutable_categories").update(
            mapOf("items" to itemsMap)
        ) {
            filter {
                eq("event_id", eventId)
                eq("name", categoryName)
            }
        }
    }

    override suspend fun getByEventId(eventId: String): List<ImmutableCategory> {
        val categoryDtos = db.client.from("immutable_categories")
            .select {
                filter { eq("event_id", eventId) }
            }
            .decodeList<ImmutableCategoryDTO>()

        return categoryDtos.map { dto ->
            val itemsMap = dto.items.mapValues { it.value.toMutableSet() }.toMutableMap()
            ImmutableCategory(
                name = dto.name,
                items = itemsMap
            )
        }
    }

    override suspend fun delete(categoryId: String) {
        db.client.from("immutable_categories").delete {
            filter { eq("id", categoryId) }
        }
    }
}
